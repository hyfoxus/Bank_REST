package com.gnemirko.bank_rest.service;

import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.CardStatus;
import com.gnemirko.bank_rest.entity.User;
import com.gnemirko.bank_rest.repository.CardRepository;
import com.gnemirko.bank_rest.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * ЮНИТ-тест конкурентного перевода:
 * - без БД, без SpringBootTest;
 * - CardRepository замокан, но с «реальным» поведением:
 *   findByIdForUpdate блокирует строку через ReentrantLock до завершения обеих save(..).
 */
class CardServiceConcurrencyUnitTest {

    private CardRepository cardRepository;
    private UserRepository userRepository;
    private CardServiceImpl service;

    // Примитивное «хранилище» сущностей для юнит-теста
    private final Map<Long, Card> store = new ConcurrentHashMap<>();
    private final Map<Long, ReentrantLock> rowLocks = new ConcurrentHashMap<>();

    // Для трекинга блокировок внутри ТЕКУЩЕГО потока (эмуляция "держим лок до конца транзакции")
    private final ThreadLocal<Set<ReentrantLock>> acquiredLocks = ThreadLocal.withInitial(HashSet::new);
    private final ThreadLocal<Integer> savesInThisTx = ThreadLocal.withInitial(() -> 0);

    private User owner;
    private Card from;
    private Card to;

    @BeforeEach
    void setUp() {
        cardRepository = Mockito.mock(CardRepository.class, Mockito.withSettings().lenient());
        userRepository = Mockito.mock(UserRepository.class, Mockito.withSettings().lenient());

        service = new CardServiceImpl(cardRepository, userRepository);

        owner = new User();
        owner.setId(1L);
        owner.setName("owner");

        from = new Card();
        from.setId(10L);
        from.setOwner(owner);
        from.setNumber("4111111111111111");
        from.setStatus(CardStatus.ACTIVE);
        from.setExpiryDate(Date.valueOf(LocalDate.now().plusYears(1).withDayOfMonth(1)));
        from.setBalance(new BigDecimal("100.00"));

        to = new Card();
        to.setId(20L);
        to.setOwner(owner);
        to.setNumber("5555444433331111");
        to.setStatus(CardStatus.ACTIVE);
        to.setExpiryDate(Date.valueOf(LocalDate.now().plusYears(1).withDayOfMonth(1)));
        to.setBalance(new BigDecimal("50.00"));

        store.put(from.getId(), deepCopy(from));
        store.put(to.getId(), deepCopy(to));


        when(cardRepository.findByIdForUpdate(anyLong())).thenAnswer(inv -> {
            Long id = inv.getArgument(0, Long.class);
            Card c = store.get(id);
            if (c == null) return Optional.empty();

            ReentrantLock lock = rowLocks.computeIfAbsent(id, k -> new ReentrantLock());
            lock.lock();

            acquiredLocks.get().add(lock);
            return Optional.of(c);
        });

        when(cardRepository.findById(anyLong())).thenAnswer(inv -> {
            Long id = inv.getArgument(0, Long.class);
            Card c = store.get(id);
            return Optional.ofNullable(c);
        });

        when(cardRepository.save(Mockito.any(Card.class))).thenAnswer(inv -> {
            Card passed = inv.getArgument(0, Card.class);
            store.put(passed.getId(), deepCopy(passed));

            int n = savesInThisTx.get() + 1;
            savesInThisTx.set(n);
            if (n >= 2) {
                for (ReentrantLock l : acquiredLocks.get()) {
                    try { l.unlock(); } catch (IllegalMonitorStateException ignored) {}
                }
                acquiredLocks.get().clear();
                savesInThisTx.set(0);
            }
            return passed;
        });

        Mockito.doAnswer(inv -> null).when(cardRepository).flush();
    }

    @Test
    void concurrentTransfers_doNotDoubleSpend_unitLevel() throws Exception {
        Long fromId = from.getId();
        Long toId   = to.getId();

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();

        Callable<Void> t1 = () -> {
            start.await();
            try {
                service.transfer(fromId, toId, new BigDecimal("80.00"));
                success.incrementAndGet();
            } catch (Exception e) {
                failure.incrementAndGet();
            } finally {
                done.countDown();
                cleanupThreadLocals();
            }
            return null;
        };

        Callable<Void> t2 = () -> {
            start.await();
            try {
                service.transfer(fromId, toId, new BigDecimal("30.00"));
                success.incrementAndGet();
            } catch (Exception e) {
                failure.incrementAndGet();
            } finally {
                done.countDown();
                cleanupThreadLocals();
            }
            return null;
        };

        pool.submit(t1);
        pool.submit(t2);
        start.countDown();

        assertTrue(done.await(10, TimeUnit.SECONDS), "Tasks did not finish in time");
        pool.shutdownNow();

        // Проверяем конечные балансы из «хранилища»
        Card freshFrom = store.get(fromId);
        Card freshTo   = store.get(toId);

        assertEquals(1, success.get(), "ровно один перевод должен пройти");
        assertEquals(1, failure.get(), "ровно один перевод должен упасть");


        BigDecimal fromBal = freshFrom.getBalance();
        BigDecimal toBal   = freshTo.getBalance();

        boolean variantA = eq(fromBal, "20.00") && eq(toBal, "130.00");
        boolean variantB = eq(fromBal, "70.00") && eq(toBal, "80.00");
        assertTrue(variantA || variantB, "Unexpected balances: from=" + fromBal + " to=" + toBal);
    }

    // ===== helpers =====

    private static boolean eq(BigDecimal a, String b) {
        return a.compareTo(new BigDecimal(b)) == 0;
    }

    private static Card deepCopy(Card src) {
        Card c = new Card();
        c.setId(src.getId());
        c.setOwner(src.getOwner());
        c.setNumber(src.getNumber());
        c.setStatus(src.getStatus());
        c.setExpiryDate(src.getExpiryDate());
        c.setBalance(src.getBalance() == null ? null : new BigDecimal(src.getBalance().toPlainString()));
        return c;
    }

    private void cleanupThreadLocals() {
        for (ReentrantLock l : acquiredLocks.get()) {
            try { while (l.isHeldByCurrentThread()) l.unlock(); } catch (IllegalMonitorStateException ignored) {}
        }
        acquiredLocks.remove();
        savesInThisTx.remove();
    }
}
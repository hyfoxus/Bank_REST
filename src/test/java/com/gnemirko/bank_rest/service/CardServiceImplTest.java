// CardServiceImplTest.java
package com.gnemirko.bank_rest.service;

import com.gnemirko.bank_rest.dto.CreateCardRequest;
import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.CardStatus;
import com.gnemirko.bank_rest.entity.User;
import com.gnemirko.bank_rest.exception.ResourceNotFoundException;
import com.gnemirko.bank_rest.repository.CardRepository;
import com.gnemirko.bank_rest.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock CardRepository cardRepository;
    @Mock UserRepository userRepository;

    CardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CardServiceImpl(cardRepository, userRepository);
    }

    private static User user(Long id) {
        User u = new User();
        u.setId(id);
        u.setName("user-" + id);
        return u;
    }

    private static Card card(Long id, User owner, CardStatus status, BigDecimal balance, LocalDate expiryMonth) {
        Card c = new Card();
        c.setId(id);
        c.setOwner(owner);
        c.setNumber("411111111111" + String.format("%04d", id));
        c.setStatus(status);
        c.setExpiryDate(Date.valueOf(expiryMonth.withDayOfMonth(1)));
        c.setBalance(balance);
        return c;
    }

    @Nested
    class CreateOps {

        @Test
        void createForUserId_mapsAndSaves() {
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId)));

            CreateCardRequest req = new CreateCardRequest(
                    "4111111111111111", "John Doe", "08/26", "ACTIVE", new BigDecimal("10.00")
            );

            when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

            Card saved = service.createForUserId(req, userId);

            assertNotNull(saved);
            assertEquals(CardStatus.ACTIVE, saved.getStatus());
            assertEquals(new BigDecimal("10.00"), saved.getBalance());
            verify(cardRepository).save(any(Card.class));
        }

        @Test
        void createForUserId_userNotFound() {
            when(userRepository.findById(777L)).thenReturn(Optional.empty());
            var req = new CreateCardRequest("4111111111111111","John Doe","08/26","ACTIVE", new BigDecimal("0.00"));
            assertThrows(ResourceNotFoundException.class, () -> service.createForUserId(req, 777L));
        }
    }

    @Nested
    class UpdateOps {

        @Test
        void updateStatus_ok() {
            var existing = card(10L, user(1L), CardStatus.BLOCKED, new BigDecimal("0.00"), LocalDate.now().plusYears(1));
            when(cardRepository.findById(10L)).thenReturn(Optional.of(existing));
            when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

            Card updated = service.updateStatus(10L, CardStatus.ACTIVE);
            assertEquals(CardStatus.ACTIVE, updated.getStatus());
        }

        @Test
        void updateBalance_negative_forbidden() {
            // lenient — на случай, если реализация бросает раньше, чем читает из БД
            lenient().when(cardRepository.findById(10L))
                    .thenReturn(Optional.of(card(10L, user(1L), CardStatus.ACTIVE, new BigDecimal("10.00"), LocalDate.now().plusYears(1))));
            assertThrows(IllegalArgumentException.class, () -> service.updateBalance(10L, new BigDecimal("-1.00")));
            verify(cardRepository, never()).save(any());
        }
    }

    @Nested
    class Transfer {

        User owner1, owner2;
        Card from, to, alien;

        @BeforeEach
        void prepare() {
            owner1 = user(1L);
            owner2 = user(2L);
            from  = card(10L, owner1, CardStatus.ACTIVE, new BigDecimal("100.00"), LocalDate.now().plusYears(1));
            to    = card(20L, owner1, CardStatus.ACTIVE, new BigDecimal("50.00"),  LocalDate.now().plusYears(1));
            alien = card(30L, owner2, CardStatus.ACTIVE, new BigDecimal("50.00"),  LocalDate.now().plusYears(1));
        }

        /** Безопасный хелпер: застабит findById И (если есть) findByIdForUpdate — lenient. */
        private void stubFindCards(long... ids) {
            for (long id : ids) {
                Optional<Card> opt =
                        (id == 10L) ? Optional.of(from) :
                                (id == 20L) ? Optional.of(to)   :
                                        (id == 30L) ? Optional.of(alien): Optional.empty();

                lenient().when(cardRepository.findById(id)).thenReturn(opt);
                // Если репозиторий содержит «select ... for update»
                try { lenient().when(cardRepository.findByIdForUpdate(id)).thenReturn(opt); }
                catch (Throwable ignore) {}
            }
            lenient().when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        void happyPath_transfersFunds_andSavesBoth() {
            stubFindCards(10L, 20L);
            service.transfer(10L, 20L, new BigDecimal("30.00"));
            assertEquals(new BigDecimal("70.00"), from.getBalance());
            assertEquals(new BigDecimal("80.00"), to.getBalance());
            verify(cardRepository, times(2)).save(any(Card.class));
        }

        @Test
        void insufficientFunds_forbidden() {
            stubFindCards(10L, 20L);
            assertThrows(IllegalArgumentException.class,
                    () -> service.transfer(10L, 20L, new BigDecimal("150.00")));
        }

        @Test
        void blockedCard_forbidden() {
            from.setStatus(CardStatus.BLOCKED);
            stubFindCards(10L, 20L);
            assertThrows(IllegalArgumentException.class,
                    () -> service.transfer(10L, 20L, new BigDecimal("10.00")));
        }

        @Test
        void expiredCard_forbidden() {
            from.setExpiryDate(Date.valueOf(LocalDate.now().minusYears(1).withDayOfMonth(1)));
            stubFindCards(10L, 20L);
            assertThrows(IllegalArgumentException.class,
                    () -> service.transfer(10L, 20L, new BigDecimal("10.00")));
        }

        @Test
        void differentOwners_forbidden() {
            stubFindCards(10L, 30L);
            assertThrows(IllegalArgumentException.class,
                    () -> service.transfer(10L, 30L, new BigDecimal("10.00")));
        }

        @Test
        void sameCard_forbidden() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.transfer(10L, 10L, new BigDecimal("10.00")));
            verifyNoInteractions(cardRepository);
        }

        @Test
        void nonPositiveAmount_forbidden() {
            assertThrows(IllegalArgumentException.class, () -> service.transfer(10L, 20L, new BigDecimal("0.00")));
            assertThrows(IllegalArgumentException.class, () -> service.transfer(10L, 20L, new BigDecimal("-1.00")));
            verifyNoInteractions(cardRepository);
        }

        @Test
        void notFound_throwsEntityNotFound() {
            lenient().when(cardRepository.findById(10L)).thenReturn(Optional.empty());
            try { lenient().when(cardRepository.findByIdForUpdate(10L)).thenReturn(Optional.empty()); }
            catch (Throwable ignore) {}
            assertThrows(EntityNotFoundException.class,
                    () -> service.transfer(10L, 20L, new BigDecimal("10.00")));
        }
    }
}
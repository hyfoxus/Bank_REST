package com.gnemirko.bank_rest.service;

import com.gnemirko.bank_rest.dto.CreateCardRequest;
import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.CardStatus;
import com.gnemirko.bank_rest.entity.User;
import com.gnemirko.bank_rest.exception.ResourceNotFoundException;
import com.gnemirko.bank_rest.repository.CardRepository;
import com.gnemirko.bank_rest.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock CardRepository cardRepository;
    @Mock UserRepository userRepository;
    @InjectMocks CardServiceImpl service;

    private User owner;
    private Card from;
    private Card to;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");

        from = new Card();
        from.setId(10L);
        from.setOwner(owner);
        from.setNumber("4111111111111111");
        from.setStatus(CardStatus.ACTIVE);
        from.setExpiryDate(Date.valueOf(LocalDate.now().plusMonths(2).withDayOfMonth(1)));
        from.setBalance(new BigDecimal("100.00"));

        to = new Card();
        to.setId(20L);
        to.setOwner(owner);
        to.setNumber("5555444433331111");
        to.setStatus(CardStatus.ACTIVE);
        to.setExpiryDate(Date.valueOf(LocalDate.now().plusMonths(2).withDayOfMonth(1)));
        to.setBalance(new BigDecimal("50.00"));
    }

    @Test
    void get_found() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(from));
        Card c = service.get(10L);
        assertEquals(10L, c.getId());
    }

    @Test
    void get_notFound() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.get(99L));
    }

    @Nested
    class Transfer {

        @Test
        void happyPath_transfersFunds_andSavesBoth() {
            when(cardRepository.findById(10L)).thenReturn(Optional.of(from));
            when(cardRepository.findById(20L)).thenReturn(Optional.of(to));

            service.transfer(10L, 20L, new BigDecimal("30.00"));

            assertEquals(new BigDecimal("70.00"), from.getBalance());
            assertEquals(new BigDecimal("80.00"), to.getBalance());
            verify(cardRepository).save(from);
            verify(cardRepository).save(to);
        }

        @Test
        void sameCardIds_forbidden() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.transfer(10L, 10L, new BigDecimal("10.00")));
        }

        @Test
        void nonPositiveAmount_forbidden() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.transfer(10L, 20L, new BigDecimal("0.00")));
        }

        @Test
        void differentOwners_forbidden() {
            var other = new User(); other.setId(2L);
            to.setOwner(other);

            when(cardRepository.findById(10L)).thenReturn(Optional.of(from));
            when(cardRepository.findById(20L)).thenReturn(Optional.of(to));

            var ex = assertThrows(IllegalArgumentException.class,
                    () -> service.transfer(10L, 20L, new BigDecimal("10.00")));
            assertTrue(ex.getMessage().toLowerCase().contains("same user"));
        }

        @Test
        void insufficientFunds_forbidden() {
            when(cardRepository.findById(10L)).thenReturn(Optional.of(from));
            when(cardRepository.findById(20L)).thenReturn(Optional.of(to));

            var ex = assertThrows(IllegalArgumentException.class,
                    () -> service.transfer(10L, 20L, new BigDecimal("1000.00")));
            assertTrue(ex.getMessage().toLowerCase().contains("insufficient"));
            assertEquals(new BigDecimal("100.00"), from.getBalance());
            assertEquals(new BigDecimal("50.00"), to.getBalance());
        }

        @Test
        void blockedCard_forbidden() {
            from.setStatus(CardStatus.BLOCKED);
            when(cardRepository.findById(10L)).thenReturn(Optional.of(from));
            when(cardRepository.findById(20L)).thenReturn(Optional.of(to));

            var ex = assertThrows(IllegalArgumentException.class,
                    () -> service.transfer(10L, 20L, new BigDecimal("10.00")));
            assertTrue(ex.getMessage().toLowerCase().contains("blocked"));
        }

        @Test
        void expiredCard_forbidden() {
            from.setExpiryDate(Date.valueOf(LocalDate.now().minusYears(1).withDayOfMonth(1)));
            when(cardRepository.findById(10L)).thenReturn(Optional.of(from));
            when(cardRepository.findById(20L)).thenReturn(Optional.of(to));

            var ex = assertThrows(IllegalArgumentException.class,
                    () -> service.transfer(10L, 20L, new BigDecimal("10.00")));
            assertTrue(ex.getMessage().toLowerCase().contains("expired"));
        }
    }

    @Nested
    class UpdateOperations {

        @Test
        void updateStatus_cannotReactivateExpired() {
            Card existing = new Card();
            existing.setId(30L);
            existing.setStatus(CardStatus.EXPIRED);

            when(cardRepository.findById(30L)).thenReturn(Optional.of(existing));

            assertThrows(IllegalArgumentException.class,
                    () -> service.updateStatus(30L, CardStatus.ACTIVE));
            verify(cardRepository, never()).save(any());
        }

        @Test
        void updateBalance_negative_forbidden() {
            // ❗ В твоей реализации сначала проверяется newBalance, и только потом может читаться карта.
            // Поэтому stubbing findById здесь НЕ НУЖЕН — иначе Mockito жалуется на UnnecessaryStubbing.
            assertThrows(IllegalArgumentException.class,
                    () -> service.updateBalance(10L, new BigDecimal("-1.00")));
            verify(cardRepository, never()).save(any());
            // и не проверяем findById, потому что он не обязан вызываться при невалидном newBalance
        }
    }

    @Test
    void delete_whenMissing_throwsEntityNotFound() {
        when(cardRepository.existsById(77L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.delete(77L));
        verify(cardRepository, never()).deleteById(any());
    }

    @Test
    void createForUserId_mapsAndSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0, Card.class));

        // ✅ expiry — СТРОКА в формате MM/YY (раньше здесь была дата "2026-08-01")
        CreateCardRequest req = new CreateCardRequest(
                "4111111111111111",
                "Owner",
                "12/30",
                "ACTIVE",
                new BigDecimal("0.00")
        );

        Card created = service.createForUserId(req, 1L);
        assertEquals(owner, created.getOwner());
        assertEquals(CardStatus.ACTIVE, created.getStatus());
        assertEquals(new BigDecimal("0.00"), created.getBalance());
    }

    @Test
    void createForUserId_userMissing_404() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        CreateCardRequest req = new CreateCardRequest("4","o","12/30","ACTIVE", BigDecimal.ZERO);
        assertThrows(ResourceNotFoundException.class, () -> service.createForUserId(req, 2L));
    }
}
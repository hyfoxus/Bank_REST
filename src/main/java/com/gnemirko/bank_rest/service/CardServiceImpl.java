package com.gnemirko.bank_rest.service;


import com.gnemirko.bank_rest.dto.CreateCardRequest;
import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.CardStatus;
import com.gnemirko.bank_rest.entity.User;
import com.gnemirko.bank_rest.exception.ResourceNotFoundException;
import com.gnemirko.bank_rest.repository.CardRepository;
import com.gnemirko.bank_rest.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    @Override
    @Transactional(readOnly = true)
    public Card get(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card not found: " + id));
    }

    @Override
    @Transactional
    public Card createForUserId(CreateCardRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId)); // => 404

        CardStatus status = CardStatus.valueOf(request.status().toUpperCase());
        var ym = java.time.YearMonth.parse(request.expiry(), java.time.format.DateTimeFormatter.ofPattern("MM/yy"));
        var expiryDate = java.sql.Date.valueOf(ym.atEndOfMonth());
        Card card = Card.builder()
                .owner(user)
                .number(request.cardNumber())
                .expiryDate(expiryDate)
                .status(status)
                .balance(request.balance())
                .build();
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card updateStatus(Long id, CardStatus newStatus) {
        Card card = get(id);
        if (newStatus == null) throw new IllegalArgumentException("Status is required");
        // матрица допустимых переходов, при необходимости расширь
        if (card.getStatus() == CardStatus.EXPIRED && newStatus == CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot re-activate expired card");
        }
        card.setStatus(newStatus);
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card updateBalance(Long id, BigDecimal newBalance) {
        Card card = get(id);
        if (newBalance == null || newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        // Обычно прямой set баланса делают только админ-операциями.
        // Для пользователя лучше иметь deposit/withdraw/transfer.
        card.setBalance(newBalance);
        return cardRepository.save(card);
    }
    @Override
    @Transactional
    public void delete(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new EntityNotFoundException("Card not found: " + id);
        }
        cardRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void transfer(Long fromCardId, Long toCardId, BigDecimal amount) {
        if (Objects.equals(fromCardId, toCardId)) {
            throw new IllegalArgumentException("Source and target cards must be different");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Card from = get(fromCardId);
        Card to = get(toCardId);

        // Переводы только между картами одного владельца
        if (from.getOwner() == null || to.getOwner() == null ||
                !Objects.equals(from.getOwner().getId(), to.getOwner().getId())) {
            throw new IllegalArgumentException("Transfer is only allowed between cards of the same user");
        }

        // Проверки статуса и срока действия
        validateActive(from);
        validateActive(to);
        validateNotExpired(from);
        validateNotExpired(to);

        if (from.getBalance() == null) from.setBalance(BigDecimal.ZERO);
        if (to.getBalance() == null) to.setBalance(BigDecimal.ZERO);

        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        cardRepository.save(from);
        cardRepository.save(to);
    }

    /* ===== helpers ===== */

    private void validateNotExpired(Card card) {
        if (card.getExpiryDate() == null) return;
        var ym = java.time.YearMonth.from(card.getExpiryDate().toLocalDate());
        if (java.time.LocalDate.now().isAfter(ym.atEndOfMonth())) {
            throw new IllegalArgumentException("Card is expired");
        }
    }

    private void validateActive(Card card) {
        if (card.getStatus() != null && "BLOCKED".equals(card.getStatus().name())) {
            throw new IllegalArgumentException("Card is blocked");
        }
    }
}

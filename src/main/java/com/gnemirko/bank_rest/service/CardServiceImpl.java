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

        Card card = Card.builder()
                .owner(user)
                .number(request.cardNumber())
                .expiryDate(request.expiry())
                .status(status)
                .balance(request.balance())
                .build();
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card update(Long id, Card patch) {
        final Card existing = get(id);

        // Обновляем только «редактируемые» поля. При необходимости сократи/расширь.
        if (patch.getOwner() != null)      existing.setOwner(patch.getOwner());
        if (patch.getExpiryDate() != null)  existing.setExpiryDate(patch.getExpiryDate());
        if (patch.getStatus() != null)      existing.setStatus(patch.getStatus());

        validateNotExpired(existing);
        return existing; // будет сохранён при коммите транзакции (dirty checking)
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
        if (fromCardId == null || toCardId == null) {
            throw new IllegalArgumentException("fromCardId and toCardId are required");
        }
        if (fromCardId.equals(toCardId)) {
            throw new IllegalArgumentException("fromCardId must differ from toCardId");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }



        // Загружаем обе карты в одной транзакции
        final Card from = get(fromCardId);
        final Card to   = get(toCardId);

        if (!from.getOwner().equals(to.getOwner())) {
            throw new IllegalArgumentException("Cards aren't owned by same person");
        }

        validateActive(from);
        validateActive(to);

        // Нормализуем scale=2 (если у тебя другой — поменяй здесь одно место)
        final BigDecimal amt = amount.setScale(2);

        if (from.getBalance() == null || to.getBalance() == null) {
            throw new IllegalStateException("Balance must not be null");
        }
        if (from.getBalance().compareTo(amt) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }



        from.setBalance(from.getBalance().subtract(amt));
        to.setBalance(to.getBalance().add(amt));
        // save() не обязателен — Hibernate сам зафлашит изменения на коммите.
        // Если у тебя отключён dirty checking, то раскомментируй:
        // cardRepository.save(from);
        // cardRepository.save(to);
    }

    /* ===== helpers ===== */

    private void validateNotExpired(Card card) {
        // считаем, что карта просрочена, если истёк месяц expiryDate
        final LocalDate nowMonth = LocalDate.now().withDayOfMonth(1);
        if (card.getExpiryDate() != null && card.getExpiryDate().toLocalDate().isBefore(nowMonth)){
            throw new IllegalArgumentException("Card is expired");
        }
    }

    private void validateActive(Card card) {
        if (card.getStatus() != null && "BLOCKED".equals(card.getStatus().name())) {
            throw new IllegalArgumentException("Card is blocked");
        }
    }
}

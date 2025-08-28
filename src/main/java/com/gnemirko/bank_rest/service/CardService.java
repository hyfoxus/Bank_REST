package com.gnemirko.bank_rest.service;

import com.gnemirko.bank_rest.dto.CardResponse;
import com.gnemirko.bank_rest.dto.CreateCardRequest;
import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface CardService {

    // CRUD
    Card get(Long id);
    Card createForUserId(CreateCardRequest request, Long userId);
    Card updateStatus(Long id, CardStatus newStatus);
    Card updateBalance(Long id, BigDecimal newBalance);
    void delete(Long id);
     Page<CardResponse> listAll(
            String ownerName,
            CardStatus status,
            String last4,
            Pageable pageable
    );
    // Перевод между картами
    void transfer(Long fromCardId, Long toCardId, BigDecimal amount);
}

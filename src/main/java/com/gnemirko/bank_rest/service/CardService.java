package com.gnemirko.bank_rest.service;

import com.gnemirko.bank_rest.dto.CreateCardRequest;
import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.CardStatus;

import java.math.BigDecimal;
import java.util.UUID;

public interface CardService {

    // CRUD
    Card get(Long id);
    Card createForUserId(CreateCardRequest request, Long userId);
    Card updateStatus(Long id, CardStatus newStatus);
    Card updateBalance(Long id, BigDecimal newBalance);
    void delete(Long id);

    // Перевод между картами
    void transfer(Long fromCardId, Long toCardId, BigDecimal amount);
}

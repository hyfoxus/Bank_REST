package com.gnemirko.bank_rest.service;

import com.gnemirko.bank_rest.dto.CreateCardRequest;
import com.gnemirko.bank_rest.entity.Card;

import java.math.BigDecimal;
import java.util.UUID;

public interface CardService {

    // CRUD
    Card get(Long id);                 // read
    Card createForUserId(CreateCardRequest request, Long userId);            // create
    Card update(Long id, Card patch);  // update (простое обновление полей)
    void delete(Long id);              // delete

    // Перевод между картами
    void transfer(Long fromCardId, Long toCardId, BigDecimal amount);
}

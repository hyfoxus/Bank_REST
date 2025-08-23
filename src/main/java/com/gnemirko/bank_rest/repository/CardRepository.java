package com.gnemirko.bank_rest.repository;

import com.gnemirko.bank_rest.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findAllByAccountId(Long accountId);
    Card findByCardId(Long cardId);
}

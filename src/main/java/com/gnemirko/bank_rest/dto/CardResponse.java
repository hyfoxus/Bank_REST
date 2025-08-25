package com.gnemirko.bank_rest.dto;
import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.User;

import java.math.BigDecimal;
import java.sql.Date;

public record CardResponse(
        Long id,
        String maskedNumber,
        User owner,
        Date expiry,
        String status,
        BigDecimal balance
) {
    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                mask(card.getNumber()),
                card.getOwner(),
                card.getExpiryDate(),
                card.getStatus().name(),
                card.getBalance()
        );
    }

    private static String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + last4;
    }
}

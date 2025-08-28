package com.gnemirko.bank_rest.dto;
import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;


import java.math.BigDecimal;
import java.sql.Date;

@Schema(name = "CardResponse", description = "Ответ с данными карты (номер маскирован)")
public record CardResponse(
        @Schema(description = "ID карты", example = "100") Long id,
        @Schema(description = "Маскированный номер карты", example = "**** **** **** 1234") String maskedNumber,
        @Schema(description = "Владелец карты") User owner,
        @Schema(description = "Срок действия карты", example = "2030-12-01") Date expiry,
        @Schema(description = "Статус карты", example = "ACTIVE") String status,
        @Schema(description = "Баланс карты", example = "123.45") BigDecimal balance
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

package com.gnemirko.bank_rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(name = "CreateCardRequest", description = "Запрос на создание карты пользователю")
public record CreateCardRequest(
        @Schema(description = "Номер карты (пройдёт Luhn)", example = "4111111111111111")
        @NotBlank @Size(min = 12, max = 32) String cardNumber,

        @Schema(description = "Имя владельца", example = "Alice")
        @NotBlank @Size(min = 2, max = 64) String ownerName,

        @Schema(description = "Срок действия (MM/YY)", example = "12/30")
        @NotBlank String expiry,

        @Schema(description = "Статус", example = "ACTIVE")
        @NotBlank String status,

        @Schema(description = "Начальный баланс", example = "0.00")
        @NotNull @DecimalMin("0.00") BigDecimal balance
) {}
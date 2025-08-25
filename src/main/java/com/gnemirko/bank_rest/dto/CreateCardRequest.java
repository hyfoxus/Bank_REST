package com.gnemirko.bank_rest.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.sql.Date;

public record CreateCardRequest(
        @NotBlank
        @Size(min = 12, max = 32, message = "Card number must be between 12 and 32 characters")
        String cardNumber,

        @NotBlank
        @Size(min = 2, max = 64, message = "Owner name must be between 2 and 64 characters")
        String ownerName,

        @Pattern(regexp = "^(0[1-9]|1[0-2])\\/\\d{2}$", message = "Expiry must be in MM/YY format")
        Date expiry,

        @NotBlank(message = "Status is required")
        String status,

        @NotNull
        @DecimalMin(value = "0.00", message = "Balance must be non-negative")
        BigDecimal balance

) {
}

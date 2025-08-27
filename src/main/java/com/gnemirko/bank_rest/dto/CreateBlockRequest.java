package com.gnemirko.bank_rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBlockRequest(
        @NotNull Long requestorId,

        @NotNull String state,

        @NotBlank Long cardId,

        @NotBlank @Size(min = 2, max = 64, message = "Operation name must be between 2 and 64 characters") String operation
) {
}

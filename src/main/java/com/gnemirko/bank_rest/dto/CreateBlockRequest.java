package com.gnemirko.bank_rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateBlockRequest", description = "Запрос на блокировку карты")
public record CreateBlockRequest(
        @Schema(description = "ID инициатора запроса", example = "1")
        @NotNull Long requestorId,

        @Schema(description = "Состояние/тип заявки", example = "BLOCK")
        @NotNull String state,

        @Schema(description = "ID карты", example = "200")
        @NotBlank Long cardId,

        @Schema(description = "Операция/причина", example = "Потеряна карта")
        @NotBlank @Size(min = 2, max = 64, message = "Operation name must be between 2 and 64 characters") String operation
) {
}

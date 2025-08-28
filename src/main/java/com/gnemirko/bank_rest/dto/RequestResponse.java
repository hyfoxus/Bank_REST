package com.gnemirko.bank_rest.dto;

import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.Request;
import com.gnemirko.bank_rest.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RequestResponse", description = "Ответ на пользовательскую заявку")
public record RequestResponse(
        @Schema(description = "ID заявки", example = "501") Long id,
        @Schema(description = "Инициатор заявки") User requestor,
        @Schema(description = "Состояние заявки", example = "CREATED") String state,
        @Schema(description = "Объект заявки (карта)") Card object,
        @Schema(description = "Сообщение/операция", example = "Заявка принята") String message
) {
    public static RequestResponse from(Request request) {
        return new RequestResponse(
                request.getId(),
                request.getRequestor(),
                request.getState().toString(),
                request.getObject(),
                request.getOperation()
        );
    }
}

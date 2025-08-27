package com.gnemirko.bank_rest.dto;

import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.Request;
import com.gnemirko.bank_rest.entity.User;

public record RequestResponse(Long id, User requestor, String state, Card object, String message) {
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

package com.gnemirko.bank_rest.controller;

import com.gnemirko.bank_rest.dto.CreateBlockRequest;
import com.gnemirko.bank_rest.dto.RequestResponse;
import com.gnemirko.bank_rest.entity.Request;
import com.gnemirko.bank_rest.security.Auth;
import com.gnemirko.bank_rest.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class MeRequestController {

    private final RequestService requestService;

    @PostMapping("/block")
    public RequestResponse requestBlock(@Valid @RequestBody CreateBlockRequest req) {
        Long me = Auth.currentUserId();
        CreateBlockRequest fixed = new CreateBlockRequest(me, req.state(), req.cardId(), req.operation());
        Request created = requestService.createRequest(fixed);
        return RequestResponse.from(created);
    }
}
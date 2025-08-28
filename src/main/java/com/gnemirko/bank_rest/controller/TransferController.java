package com.gnemirko.bank_rest.controller.me;

import com.gnemirko.bank_rest.service.CardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/me/transfers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class TransferController {

    private final CardService cardService;

    @PostMapping
    public void transfer(@Valid @RequestBody TransferRequest req) {
        cardService.transfer(req.fromCardId, req.toCardId, req.amount);
    }

    @Data
    public static class TransferRequest {
        @NotNull Long fromCardId;
        @NotNull Long toCardId;
        @NotNull @DecimalMin("0.01") BigDecimal amount;
    }
}
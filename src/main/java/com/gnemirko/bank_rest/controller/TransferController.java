package com.gnemirko.bank_rest.controller;

import com.gnemirko.bank_rest.security.Auth;
import com.gnemirko.bank_rest.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "User / Transfers", description = "Переводы между своими картами (роль USER)")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/me/transfers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class TransferController {

    private final CardService cardService;

    @Operation(
            summary = "Перевести деньги между своими картами",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Невалидные параметры", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к карте", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "422", description = "Бизнес-ошибка (недостаточно средств, блокировка, просрочка)", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    @PostMapping
    public void transfer(@Valid @RequestBody TransferRequest req) {
        Long userId = Auth.currentUserId();
        cardService.transfer(req.fromCardId, req.toCardId, req.amount);
    }

    @Data
    @Schema(name = "TransferRequest", description = "Запрос на перевод между картами одного пользователя")
    public static class TransferRequest {
        @Schema(description = "ID карты-источника", example = "10")
        @NotNull Long fromCardId;

        @Schema(description = "ID карты-получателя", example = "20")
        @NotNull Long toCardId;

        @Schema(description = "Сумма перевода", example = "100.00")
        @NotNull @DecimalMin("0.01") BigDecimal amount;
    }
}
package com.gnemirko.bank_rest.controller;

import com.gnemirko.bank_rest.dto.CardResponse;
import com.gnemirko.bank_rest.dto.CreateCardRequest;
import com.gnemirko.bank_rest.entity.CardStatus;
import com.gnemirko.bank_rest.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "Admin / Cards", description = "Управление картами (роль ADMIN)")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {

    private final CardService cardService;

    @Operation(summary = "Список карт (фильтрация + пагинация)")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PageCardResponse.class)))
    @GetMapping
    public Page<CardResponse> listAll(
            @Parameter(description = "Имя владельца") @RequestParam(required = false) String ownerName,
            @Parameter(description = "Статус карты") @RequestParam(required = false) CardStatus status,
            @Parameter(description = "Последние 4 цифры") @RequestParam(required = false) String last4,
            @Parameter(hidden = true) Pageable pageable
    ) {
        return cardService.listAll(ownerName, status, last4, pageable);
    }

    @Operation(summary = "Создать карту пользователю")
    @PostMapping("/{userId}")
    public CardResponse create(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Valid @RequestBody CreateCardRequest request
    ) {
        return CardResponse.from(cardService.createForUserId(request, userId));
    }

    @Operation(summary = "Обновить статус карты")
    @PatchMapping("/{id}/status")
    public CardResponse updateStatus(
            @Parameter(description = "ID карты") @PathVariable Long id,
            @Parameter(description = "Новый статус") @RequestParam CardStatus status
    ) {
        return CardResponse.from(cardService.updateStatus(id, status));
    }

    @Operation(summary = "Установить баланс карты (ADMIN)")
    @PatchMapping("/{id}/balance")
    public CardResponse updateBalance(
            @Parameter(description = "ID карты") @PathVariable Long id,
            @Parameter(description = "Новый баланс") @RequestParam @NotNull @DecimalMin("0.00") BigDecimal balance
    ) {
        return CardResponse.from(cardService.updateBalance(id, balance));
    }

    @Operation(summary = "Удалить карту")
    @DeleteMapping("/{id}")
    public void delete(@Parameter(description = "ID карты") @PathVariable Long id) {
        cardService.delete(id);
    }

    @Schema(name = "PageCardResponse")
    static class PageCardResponse {
        @ArraySchema(schema = @Schema(implementation = CardResponse.class))
        public java.util.List<CardResponse> content;
        public int number;
        public int size;
        public long totalElements;
        public int totalPages;
    }
}
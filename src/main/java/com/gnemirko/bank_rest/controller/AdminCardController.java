package com.gnemirko.bank_rest.controller.admin;

import com.gnemirko.bank_rest.dto.CardResponse;
import com.gnemirko.bank_rest.dto.CreateCardRequest;
import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.CardStatus;
import com.gnemirko.bank_rest.repository.CardRepository;
import com.gnemirko.bank_rest.service.CardService;
import com.gnemirko.bank_rest.util.CardSpecification;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminCardController {

    private final CardService cardService;

    /**
     * Список всех карт с фильтрами и пагинацией (фильтрация в БД через Specification).
     */
    @GetMapping
    public Page<CardResponse> listAll(
            @RequestParam(required = false) String ownerName,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) String last4,
            Pageable pageable
    ) {
        return cardService.listAll(ownerName, status, last4, pageable);
    }

    /**
     * Создать карту пользователю.
     */
    @PostMapping("/{userId}")
    public CardResponse create(@PathVariable Long userId, @Valid @RequestBody CreateCardRequest req) {
        Card card = cardService.createForUserId(req, userId);
        return CardResponse.from(card);
    }

    /**
     * Получить карту по id.
     */
    @GetMapping("/{id}")
    public CardResponse get(@PathVariable Long id) {
        return CardResponse.from(cardService.get(id));
    }

    /**
     * Частичное обновление: СТАТУС (использует CardService.updateStatus).
     */
    @PatchMapping("/{id}/status")
    public CardResponse updateStatus(@PathVariable Long id, @Valid @RequestBody SetStatusRequest body) {
        Card updated = cardService.updateStatus(id, body.status());
        return CardResponse.from(updated);
    }

    /**
     * Частичное обновление: БАЛАНС (админская операция; использует CardService.updateBalance).
     */
    @PatchMapping("/{id}/balance")
    public CardResponse updateBalance(@PathVariable Long id, @Valid @RequestBody SetBalanceRequest body) {
        Card updated = cardService.updateBalance(id, body.balance());
        return CardResponse.from(updated);
    }

    /**
     * Удалить карту.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        cardService.delete(id);
    }

    // ---------- DTO для PATCH-операций ----------
    public record SetStatusRequest(@NotNull CardStatus status) {}
    public record SetBalanceRequest(@NotNull @DecimalMin("0.00") BigDecimal balance) {}
}
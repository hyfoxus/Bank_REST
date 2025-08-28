package com.gnemirko.bank_rest.controller;

import com.gnemirko.bank_rest.dto.CardResponse;
import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.CardStatus;
import com.gnemirko.bank_rest.repository.CardRepository;
import com.gnemirko.bank_rest.security.Auth;
import com.gnemirko.bank_rest.util.CardSpecification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User / Cards", description = "Просмотр своих карт и деталей (роль USER)")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/me/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class MeCardController {

    private final CardRepository cardRepository;
    @Operation(
            summary = "Мои карты (фильтр + пагинация)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = AdminCardController.PageCardResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    @GetMapping
    public Page<CardResponse> myCards(
            @Parameter(description = "Статус карты") @RequestParam(required = false) CardStatus status,
            @Parameter(description = "Последние 4 цифры", example = "1234") @RequestParam(required = false) String last4,
            @Parameter(hidden = true) Pageable pageable
    ) {
        Long me = Auth.currentUserId();

        Specification<Card> spec = Specification.allOf(
                CardSpecification.hasStatus(status),
                CardSpecification.hasLast4(last4),
                (root, query, cb) -> cb.equal(root.get("owner").get("id"), me)
        );

        return cardRepository.findAll(spec, pageable).map(CardResponse::from);
    }

    @Operation(
            summary = "Детали моей карты",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CardResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Нет доступа (не ваша карта)", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    @GetMapping("/{id}")
    public CardResponse myCard(@PathVariable Long id) {
        Card c = cardRepository.findById(id)
                .orElseThrow(() -> new com.gnemirko.bank_rest.exception.ResourceNotFoundException("Card: " + id));

        Long me = Auth.currentUserId();
        if (c.getOwner() == null || !me.equals(c.getOwner().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
        return CardResponse.from(c);
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
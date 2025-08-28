package com.gnemirko.bank_rest.controller.me;

import com.gnemirko.bank_rest.dto.CardResponse;
import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.CardStatus;
import com.gnemirko.bank_rest.repository.CardRepository;
import com.gnemirko.bank_rest.security.Auth;
import com.gnemirko.bank_rest.util.CardSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class MeCardController {

    private final CardRepository cardRepository;

    @GetMapping
    public Page<CardResponse> myCards(
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) String last4,
            Pageable pageable
    ) {
        Long me = Auth.currentUserId();

        Specification<Card> spec = Specification.allOf(
                CardSpecification.hasStatus(status),
                CardSpecification.hasLast4(last4),
                (root, query, cb) -> cb.equal(root.get("owner").get("id"), me)
        );

        return cardRepository.findAll(spec, pageable).map(CardResponse::from);
    }

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
}
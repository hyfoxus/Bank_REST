package com.gnemirko.bank_rest.util;

import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.CardStatus;
import org.springframework.data.jpa.domain.Specification;

public final class CardSpecification {
    private CardSpecification() {}

    public static Specification<Card> hasOwnerName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            var owner = root.join("owner"); // INNER JOIN users
            return cb.like(cb.lower(owner.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Card> hasStatus(CardStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Card> hasLast4(String last4) {
        return (root, query, cb) -> {
            if (last4 == null || last4.isBlank()) return null;
            // number LIKE %1234
            return cb.like(root.get("number"), "%" + last4);
        };
    }
}

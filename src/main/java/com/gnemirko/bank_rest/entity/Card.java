package com.gnemirko.bank_rest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Date;


@Entity
@Data
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table (name = "card")
public class Card {
    @Id
    @GeneratedValue
    private Long id;

    private String number;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User owner;

    private Date expiryDate;

    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private CardStatus status;
}

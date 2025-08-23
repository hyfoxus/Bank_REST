package com.gnemirko.bank_rest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Entity
@Data
@Getter @Setter
@NoArgsConstructor
public class Card {
    @Id
    @GeneratedValue
    private Long id;

    private String number;

    @ManyToOne()
    @JoinColumn(name = "owner")
    private User owner;

    private Date expiryDate;

    private Float balance;


}

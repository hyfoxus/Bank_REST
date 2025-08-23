package com.gnemirko.bank_rest.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@Getter @Setter
@NoArgsConstructor

@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    private Role role;
    private String name;

}

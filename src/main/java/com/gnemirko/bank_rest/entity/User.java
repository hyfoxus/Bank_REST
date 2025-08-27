package com.gnemirko.bank_rest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private Role role;


    private String name;

    @Column(nullable = false)
    private String passwordHash;

}

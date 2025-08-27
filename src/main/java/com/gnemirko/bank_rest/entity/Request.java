package com.gnemirko.bank_rest.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@Getter @Setter
@NoArgsConstructor

public class Request {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn (name = "requestor_id")
    private User requestor;

    @Enumerated(EnumType.STRING)
    private RequestState state;

    @ManyToOne
    @JoinColumn (name = "object_id")
    private Card object;

    private String operation;
}

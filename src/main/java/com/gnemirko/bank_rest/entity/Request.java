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
    private String id;

    @ManyToOne
    @JoinColumn (name = "requestor")
    private User requestor;

    private RequestState state;

    @ManyToOne
    @JoinColumn (name = "object")
    private Card object;

    private String operation;
}

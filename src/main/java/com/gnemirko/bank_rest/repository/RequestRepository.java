package com.gnemirko.bank_rest.repository;

import com.gnemirko.bank_rest.entity.Request;
import com.gnemirko.bank_rest.entity.RequestState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByState(RequestState state);
}

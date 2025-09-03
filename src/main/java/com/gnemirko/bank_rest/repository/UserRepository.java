package com.gnemirko.bank_rest.repository;

import com.gnemirko.bank_rest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByName(String name);
    Optional<User> findByName(String name);
}

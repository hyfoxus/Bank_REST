package com.gnemirko.bank_rest.repository;

import com.gnemirko.bank_rest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}

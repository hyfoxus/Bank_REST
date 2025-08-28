package com.gnemirko.bank_rest.service;

import com.gnemirko.bank_rest.dto.CreateUserRequest;
import com.gnemirko.bank_rest.dto.UserResponse;
import com.gnemirko.bank_rest.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User createUser (CreateUserRequest request);
    User makeUserAdmin (Long id);
    Page<UserResponse> list(Pageable pageable);
    User getUser (Long id);
}

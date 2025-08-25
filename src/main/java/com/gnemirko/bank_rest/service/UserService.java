package com.gnemirko.bank_rest.service;

import com.gnemirko.bank_rest.dto.CreateUserRequest;
import com.gnemirko.bank_rest.entity.User;

public interface UserService {
    User createUser (CreateUserRequest request);
}

package com.gnemirko.bank_rest.dto;

import com.gnemirko.bank_rest.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(Long id, String username, String Role) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getRole().toString()
        );
    }
}

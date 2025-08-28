package com.gnemirko.bank_rest.dto;

import com.gnemirko.bank_rest.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserResponse", description = "Данные пользователя")
public record UserResponse(
        @Schema(description = "ID пользователя", example = "1") Long id,
        @Schema(description = "Имя пользователя", example = "alice") String username,
        @Schema(description = "Роль пользователя", example = "USER") String Role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getRole().toString()
        );
    }
}

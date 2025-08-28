package com.gnemirko.bank_rest.dto;

import com.gnemirko.bank_rest.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateUserRequest", description = "Запрос на создание пользователя")
public record CreateUserRequest(
        @Schema(description = "Имя пользователя", example = "alice")
        @NotBlank
        @Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
        String name,

        @Schema(description = "Пароль", example = "secret123")
        @NotBlank
        @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
        String password,

        @Schema(description = "Роль пользователя", example = "USER")
        @NotEmpty(message = "At least one role must be specified")
        String role
) {
}

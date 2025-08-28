package com.gnemirko.bank_rest.controller;

import com.gnemirko.bank_rest.dto.CreateUserRequest;
import com.gnemirko.bank_rest.dto.UserResponse;
import com.gnemirko.bank_rest.entity.User;
import com.gnemirko.bank_rest.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin / Users", description = "Управление пользователями (роль ADMIN)")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "Создать пользователя")
    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
        User u = userService.createUser(req);
        return UserResponse.from(u);
    }

    @Operation(summary = "Список пользователей (пагинация)")
    @GetMapping
    public Page<UserResponse> list(@Parameter(hidden = true) Pageable pageable) {
        return userService.list(pageable);
    }

    @Operation(summary = "Получить пользователя по ID")
    @GetMapping("/{id}")
    public UserResponse get(@Parameter(description = "ID пользователя") @PathVariable Long id) {
        return UserResponse.from(userService.getUser(id));
    }

    @Schema(name = "PageUserResponse")
    static class PageUserResponse {
        @ArraySchema(schema = @Schema(implementation = UserResponse.class))
        public java.util.List<UserResponse> content;
        public int number;
        public int size;
        public long totalElements;
        public int totalPages;
    }
}
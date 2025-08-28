package com.gnemirko.bank_rest.controller;

import com.gnemirko.bank_rest.dto.CreateUserRequest;
import com.gnemirko.bank_rest.dto.UserResponse;
import com.gnemirko.bank_rest.entity.User;
import com.gnemirko.bank_rest.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;


    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
        User u = userService.createUser(req);
        return UserResponse.from(u);
    }

    @GetMapping
    public Page<UserResponse> list(Pageable pageable) {
        return userService.list(pageable);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return UserResponse.from(userService.getUser(id));
    }
}
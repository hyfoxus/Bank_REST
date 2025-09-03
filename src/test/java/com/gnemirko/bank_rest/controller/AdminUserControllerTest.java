package com.gnemirko.bank_rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gnemirko.bank_rest.dto.CreateUserRequest;
import com.gnemirko.bank_rest.dto.UserResponse;
import com.gnemirko.bank_rest.entity.Role;
import com.gnemirko.bank_rest.entity.User;
import com.gnemirko.bank_rest.security.JwtAuthFilter;
import com.gnemirko.bank_rest.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminUserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
@Import(AdminUserControllerTest.TestConfig.class)
class AdminUserControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean UserService userService() { return Mockito.mock(UserService.class); }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserService userService;

    private User user(long id, String name) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setRole(Role.ROLE_USER);
        return u;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returnsUserResponse() throws Exception {
        User u = user(1L, "Alice");
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(u);

        var req = new CreateUserRequest("Alice", "secret123", "USER");

        mvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("Alice"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_returnsPageOfUsers() throws Exception {
        User u = user(2L, "Bob");
        Page<UserResponse> page = new PageImpl<>(List.of(UserResponse.from(u)), PageRequest.of(0, 20), 1);

        when(userService.list(any(Pageable.class))).thenReturn(page);

        mvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].username").value("Bob"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void get_returnsUserById() throws Exception {
        User u = user(3L, "Charlie");
        when(userService.getUser(3L)).thenReturn(u);

        mvc.perform(get("/api/admin/users/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.username").value("Charlie"));
    }
}
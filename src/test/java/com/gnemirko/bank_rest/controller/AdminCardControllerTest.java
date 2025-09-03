package com.gnemirko.bank_rest.controller;

import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.CardStatus;
import com.gnemirko.bank_rest.service.CardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты PATCH-эндпоинтов AdminCardController.
 * Контроллер принимает параметры через @RequestParam → используем .param(...).
 * Включаем фильтры и методовую безопасность, чтобы @PreAuthorize выдавал 403.
 */
@WebMvcTest(controllers = AdminCardController.class)
@AutoConfigureMockMvc(addFilters = true)
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CardService cardService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        // минимальная цепочка фильтров; все запросы требуют аутентификации
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable()) // в тестах используем .with(csrf()), можно и отключить
                    .authorizeHttpRequests(reg -> reg.anyRequest().authenticated())
                    .build();
        }
    }

    private static Card stubCard(Long id) {
        Card c = new Card();
        c.setId(id);
        c.setNumber("4111111111111111");
        c.setStatus(CardStatus.ACTIVE);
        c.setExpiryDate(Date.valueOf(LocalDate.now().plusYears(1).withDayOfMonth(1)));
        c.setBalance(new BigDecimal("100.00"));
        return c;
    }

    @Test
    @DisplayName("PATCH /api/admin/cards/{id}/status — OK (@RequestParam)")
    void patch_status_updates() throws Exception {
        Long id = 100L;
        when(cardService.updateStatus(eq(id), eq(CardStatus.ACTIVE)))
                .thenReturn(stubCard(id));

        mockMvc.perform(
                        patch("/api/admin/cards/{id}/status", id)
                                .param("status", "ACTIVE")
                                .with(csrf())
                                .with(user("admin").roles("ADMIN"))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/admin/cards/{id}/balance — OK (@RequestParam)")
    void patch_balance_updates() throws Exception {
        Long id = 100L;
        when(cardService.updateBalance(eq(id), eq(new BigDecimal("123.45"))))
                .thenReturn(stubCard(id));

        mockMvc.perform(
                        patch("/api/admin/cards/{id}/balance", id)
                                .param("balance", "123.45")
                                .with(csrf())
                                .with(user("admin").roles("ADMIN"))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH без роли ADMIN → 403 Forbidden")
    @WithMockUser(roles = "USER")
    void patch_requires_admin_role() throws Exception {
        // Важно: ничего не мокаем — запрос должен быть отклонён фильтром/PreAuthorize до вызова сервиса
        mockMvc.perform(
                patch("/api/admin/cards/{id}/status", 100L)
                        .param("status", "ACTIVE")
                        .with(csrf())
        ).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH без обязательного параметра → 400 Bad Request")
    void patch_missing_param_400() throws Exception {
        Mockito.reset(cardService);
        mockMvc.perform(
                patch("/api/admin/cards/{id}/balance", 100L)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
        ).andExpect(status().isBadRequest());
    }
}
package com.gnemirko.bank_rest.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gnemirko.bank_rest.dto.CardResponse;
import com.gnemirko.bank_rest.dto.CreateCardRequest;
import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.CardStatus;
import com.gnemirko.bank_rest.entity.User;
import com.gnemirko.bank_rest.service.CardService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Тест только контроллера, без web-фильтров; зависимости подставляем руками через TestConfig
@WebMvcTest(controllers = AdminCardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AdminCardControllerTest.TestConfig.class)
class AdminCardControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean CardService cardService() { return Mockito.mock(CardService.class); }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired CardService cardService;

    private Card sampleCard() {
        User u = new User(); u.setId(1L); u.setName("Owner");
        Card c = new Card();
        c.setId(100L);
        c.setOwner(u);
        c.setNumber("4111111111111234");
        c.setStatus(CardStatus.ACTIVE);
        c.setExpiryDate(Date.valueOf(LocalDate.now().plusYears(1).withDayOfMonth(1)));
        c.setBalance(new BigDecimal("123.45"));
        return c;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAll_withFilters_returnsMaskedNumbers() throws Exception {
        Card c = sampleCard();
        // cardService.listAll(ownerName, status, last4, pageable) → Page<CardResponse>
        Page<CardResponse> page = new PageImpl<>(List.of(CardResponse.from(c)), PageRequest.of(0, 20), 1);

        when(cardService.listAll(any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mvc.perform(get("/api/admin/cards")
                        .param("status", "ACTIVE")
                        .param("last4", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.content[0].maskedNumber", endsWith("1234")))
                .andExpect(jsonPath("$.content[0].maskedNumber", startsWith("**** ")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returnsCardResponse() throws Exception {
        Card c = sampleCard();
        when(cardService.createForUserId(any(CreateCardRequest.class), eq(1L))).thenReturn(c);

        var req = new CreateCardRequest(
                "4111111111111234",
                "Owner",
                "12/30",
                "ACTIVE",
                new BigDecimal("0.00")
        );

        mvc.perform(post("/api/admin/cards/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.maskedNumber", endsWith("1234")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void patch_status_updates() throws Exception {
        Card c = sampleCard(); c.setStatus(CardStatus.BLOCKED);
        when(cardService.updateStatus(100L, CardStatus.BLOCKED)).thenReturn(c);

        mvc.perform(patch("/api/admin/cards/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BLOCKED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void patch_balance_updates() throws Exception {
        Card c = sampleCard(); c.setBalance(new BigDecimal("999.99"));
        when(cardService.updateBalance(100L, new BigDecimal("999.99"))).thenReturn(c);

        mvc.perform(patch("/api/admin/cards/100/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"balance\":999.99}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(999.99));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_callsService() throws Exception {
        mvc.perform(delete("/api/admin/cards/100"))
                .andExpect(status().isOk());
        Mockito.verify(cardService).delete(100L);
    }
}
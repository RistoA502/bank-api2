package com.bank.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BankControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankService bankService;

    @Test
    @DisplayName("GET /api/accounts/{id} returns account data")
    void testGetAccountEndpointSuccess() throws Exception {
        Long accountId = 1L;
        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        mockAccount.addBalance("EUR", new BigDecimal("250.00"));

        when(bankService.getAccount(accountId)).thenReturn(mockAccount);

        mockMvc.perform(get("/api/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(accountId));
    }

    @Test
    @DisplayName("If we have some error!")
    void testGlobalControllerExceptionHandler() throws Exception {
        Long accountId = 1L;
        when(bankService.getAccount(accountId)).thenThrow(new IllegalArgumentException("Account missing"));

        mockMvc.perform(get("/api/accounts/" + accountId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Account missing"));
    }
}

package com.bank.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BankService bankService;

    @BeforeEach
    void setUp() {
        // Injects property value for Spring's @Value field since Spring context won't run during this Mockito unit test
        ReflectionTestUtils.setField(bankService, "externalUrlDebitMoney", "http://mock-url/debit");
    }

    @Test
    @DisplayName("Should correctly map exchange rate calculation via EUR base and set timeline object state")
    void testExchangeCurrencySuccess() {
        Long accountId = 1L;
        Account mockAccount = new Account();
        mockAccount.setId(accountId);

        mockAccount.getBalances().put("USD", new Balance(new BigDecimal("100.00")));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);

        Account result = bankService.exchangeCurrency(accountId, "USD", "GBP", new BigDecimal("50.00"));

        assertNotNull(result);

        assertEquals(0, new BigDecimal("50.00").compareTo(result.getBalance("USD")));
        assertEquals(0, new BigDecimal("39.3162").compareTo(result.getBalance("GBP")));
    }

    @Test
    @DisplayName("Should throw exception when external logging endpoint crashes during a debit trace execution block")
    void testDebitMoneyExternalServiceFailure() {
        Long accountId = 1L;

        // Setup RestTemplate mock object stub to throw a runtime error on network logging ping
        lenient().when(restTemplate.getForObject(any(String.class), eq(String.class)))
                .thenThrow(new RuntimeException("Connection Timeout"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bankService.debitMoney(accountId, "EUR", new BigDecimal("10.00"));
        });

        assertTrue(exception.getMessage().contains("Did not get connection"));
    }

    @Test
    @DisplayName("Should reject unsupported currencies based on allowed service boundaries configurations")
    void testUnsupportedCurrencyRejection() {
        assertThrows(IllegalArgumentException.class, () -> {
            bankService.addMoney(1L, "CAD", new BigDecimal("10.00"));
        });
    }
}

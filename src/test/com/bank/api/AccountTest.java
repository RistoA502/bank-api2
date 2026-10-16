package com.bank.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
    }

    @Test
    @DisplayName("Return null if no value is present")
    void testGetBalanceDefault() {
        assertEquals(BigDecimal.ZERO, account.getBalance("EUR"));
    }

    @Test
    @DisplayName("Add money to account!")
    void testAddBalance() {
        account.addBalance("eur", new BigDecimal("100.50"));
        assertEquals(new BigDecimal("100.50"), account.getBalance("EUR"));
    }

    @Test
    @DisplayName("Remove money if there is possible")
    void testSubtractBalanceSuccess() {
        account.addBalance("USD", new BigDecimal("50.00"));
        account.subtractBalance("usd", new BigDecimal("20.00"));
        assertEquals(new BigDecimal("30.00"), account.getBalance("USD"));
    }

    @Test
    @DisplayName("If you do not have not enough funds")
    void testSubtractBalanceInsufficientFunds() {
        account.addBalance("EUR", new BigDecimal("10.00"));
        assertThrows(IllegalArgumentException.class, () -> {
            account.subtractBalance("EUR", new BigDecimal("15.00"));
        });
    }
}

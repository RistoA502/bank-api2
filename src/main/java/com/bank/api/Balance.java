package com.bank.api;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Embeddable
public class Balance {

    @Column(name = "balance", nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    // Hibernate requires a default constructor
    public Balance() {
        this.lastUpdated = LocalDateTime.now();
    }

    public Balance(BigDecimal amount) {
        this.amount = amount;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    // Helper to update the amount and refresh the timestamp simultaneously
    public void updateAmount(BigDecimal newAmount) {
        this.amount = newAmount;
        this.lastUpdated = LocalDateTime.now();
    }
}

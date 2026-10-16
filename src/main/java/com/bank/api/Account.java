package com.bank.api;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "account_balances", joinColumns = @JoinColumn(name = "account_id"))
    @MapKeyColumn(name = "currency")
    // Notice we removed @Column(name = "balance") because columns are now defined inside the Balance class
    private Map<String, Balance> balances = new HashMap<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Map<String, Balance> getBalances() { return balances; }
    public void setBalances(Map<String, Balance> balances) { this.balances = balances; }

    // Updated helper logic
    public BigDecimal getBalance(String currency) {
        if (currency == null) return BigDecimal.ZERO;
        Balance bal = balances.get(currency.toUpperCase());
        return bal != null ? bal.getAmount() : BigDecimal.ZERO;
    }

    public void addBalance(String currency, BigDecimal amount) {
        String curr = currency.toUpperCase();
        BigDecimal currentAmount = getBalance(curr);

        // If the currency record doesn't exist, create it. Otherwise, update it.
        if (!balances.containsKey(curr)) {
            balances.put(curr, new Balance(currentAmount.add(amount)));
        } else {
            balances.get(curr).updateAmount(currentAmount.add(amount));
        }
    }

    public void subtractBalance(String currency, BigDecimal amount) {
        String curr = currency.toUpperCase();
        BigDecimal currentAmount = getBalance(curr);

        if (currentAmount.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds in currency: " + curr);
        }

        if (!balances.containsKey(curr)) {
            balances.put(curr, new Balance(currentAmount.subtract(amount)));
        } else {
            balances.get(curr).updateAmount(currentAmount.subtract(amount));
        }
    }
}

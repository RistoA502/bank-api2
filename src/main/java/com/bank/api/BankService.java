package com.bank.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class BankService {
    private final AccountRepository accountRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger log = LoggerFactory.getLogger(BankService.class);

    private static final Set<String> ALLOWED_CURRENCIES = Set.of("EUR", "USD", "SEK", "GBP");
    
    // Fixed Exchange Rates relative to EUR base for simplicity
    private static final Map<String, BigDecimal> TO_EUR_RATES = Map.of(
        "EUR", BigDecimal.ONE,
        "USD", new BigDecimal("0.92"),
        "SEK", new BigDecimal("0.086"),
        "GBP", new BigDecimal("1.17")
    );

    @Value("${external.url.debit.money}")
    private String externalUrlDebitMoney;

    public BankService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    private void validateCurrency(String currency) {
        if (!ALLOWED_CURRENCIES.contains(currency.toUpperCase())) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }
    }

    public Account createAccount() {
        Account account = new Account();
        for (String currency : ALLOWED_CURRENCIES) {
            account.getBalances().put(currency, new Balance(BigDecimal.ZERO));
        }
        account = accountRepository.save(account);
        log.info("Account was created "+account.getId());
        return account;
    }

    public Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + id));
    }

    public Account addMoney(Long id, String currency, BigDecimal amount) {
        log.info("Add money id="+id+" currency="+currency+ "amount="+amount);
        validateCurrency(currency);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        Account account = getAccount(id);
        account.addBalance(currency, amount);
        return accountRepository.save(account);
    }

    public Account debitMoney(Long id, String currency, BigDecimal amount) {
        log.info("Debit money id="+id+" currency="+currency+ " amount="+amount);
        validateCurrency(currency);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        // Simulate external logging call before debiting
        try {
            restTemplate.getForObject(externalUrlDebitMoney, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Did not get connection "+externalUrlDebitMoney);
        }

        Account account = getAccount(id);
        account.subtractBalance(currency, amount);
        return accountRepository.save(account);
    }

    public Account exchangeCurrency(Long id, String fromCurrency, String toCurrency, BigDecimal amount) {
        log.info("Validate currency id="+id+" fromCurrency="+fromCurrency+ " toCurrency="+toCurrency+" amount="+amount);
        validateCurrency(fromCurrency);
        validateCurrency(toCurrency);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Account account = getAccount(id);
        account.subtractBalance(fromCurrency, amount);

        // Convert to EUR first, then to target currency
        BigDecimal fromRate = TO_EUR_RATES.get(fromCurrency.toUpperCase());
        BigDecimal toRate = TO_EUR_RATES.get(toCurrency.toUpperCase());

        BigDecimal amountInEur = amount.multiply(fromRate);
        BigDecimal targetAmount = amountInEur.divide(toRate, 4, RoundingMode.HALF_UP);

        account.addBalance(toCurrency, targetAmount);
        return accountRepository.save(account);
    }
}

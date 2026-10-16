package com.bank.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount() {
        return ResponseEntity.ok(bankService.createAccount());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(bankService.getAccount(id));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Account> deposit(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String currency = (String) request.get("currency");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        return ResponseEntity.ok(bankService.addMoney(id, currency, amount));
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<Account> debit(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String currency = (String) request.get("currency");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        return ResponseEntity.ok(bankService.debitMoney(id, currency, amount));
    }

    @PostMapping("/{id}/exchange")
    public ResponseEntity<Account> exchange(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String from = (String) request.get("from");
        String to = (String) request.get("to");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        return ResponseEntity.ok(bankService.exchangeCurrency(id, from, to, amount));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}

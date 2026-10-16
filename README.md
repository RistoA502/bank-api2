# Multi-Currency Bank API

A robust Spring Boot 3 multi-currency banking REST API utilizing Java 17, JPA, and an in-memory H2 database. This system supports real-time currency exchange calculated through a EUR base currency with safe financial math (`BigDecimal`), automatic tracking of balance update timestamps inside the database, and comprehensive JUnit 5 test suites.

## 🚀 Getting Started

### Prerequisites
* Java 17 or higher
* Maven 3.6+

### Build and Run Application
Navigate to the project root directory (where `pom.xml` is located) and execute:

```bash
mvn spring-boot:run
```
The server will start locally at: `http://localhost:8080`

### Running the Test Suite
To execute the comprehensive unit, mock, and integration tests (`AccountTest`, `BankServiceTest`, `BankControllerIT`), run:

```bash
mvn clean test
```

---

## 📊 API Documentation & Endpoints

All endpoints use `POST` or `GET` methods. Input parameters must be supplied as JSON in the request body (except for the account ID, which is passed in the URL path).

### 1. Create a New Account
Initializes an account with all allowed currencies (`EUR`, `USD`, `SEK`, `GBP`) set to an initial balance of `0.00`.
* **URL:** `/api/accounts`
* **Method:** `POST`
* **Headers:** `Content-Type: application/json`
* **Request Body:** None (Empty)
* **Success Response (201 Created):**
```json
{
  "id": 1,
  "balances": {
    "EUR": { "amount": 0.00, "lastUpdated": "2026-10-16T14:45:00" },
    "USD": { "amount": 0.00, "lastUpdated": "2026-10-16T14:45:00" },
    "SEK": { "amount": 0.00, "lastUpdated": "2026-10-16T14:45:00" },
    "GBP": { "amount": 0.00, "lastUpdated": "2026-10-16T14:45:00" }
  }
}
```

### 2. Fetch Account Details
Retrieves the current multi-currency balances and exact modifications timeline logs for a specified account ID.
* **URL:** `/api/accounts/{id}` (e.g., `/api/accounts/1`)
* **Method:** `GET`
* **Success Response (200 OK):** Same structure as the account creation response body.

### 3. Deposit Money
Adds a specified positive amount to a certain currency wallet.
* **URL:** `/api/accounts/{id}/deposit` (e.g., `/api/accounts/1/deposit`)
* **Method:** `POST`
* **Request Body:**
```json
{
  "currency": "EUR",
  "amount": 250.75
}
```
* **Success Response (200 OK):** Returns the full updated account object with the newly calculated balance and a refreshed `lastUpdated` timestamp for `EUR`.

### 4. Debit / Withdraw Money
Deducts an amount from a specified wallet. Requires an active connection to an external logging endpoint before debiting. Prevents overdrafts.
* **URL:** `/api/accounts/{id}/debit` (e.g., `/api/accounts/1/debit`)
* **Method:** `POST`
* **Request Body:**
```json
{
  "currency": "USD",
  "amount": 40.00
}
```

### 5. Exchange Currency
Converts funds from a source currency to a target currency within the same account using fixed database exchange ratios relative to a EUR base.
* **URL:** `/api/accounts/{id}/exchange` (e.g., `/api/accounts/1/exchange`)
* **Method:** `POST`
* **Request Body:**
```json
{
  "from": "USD",
  "to": "GBP",
  "amount": 50.00
}
```
* **Success Response (200 OK):** Returns the updated account payload showing the decreased `USD` balance, increased `GBP` balance, and updated timestamps for both keys.

---

## 🛠️ Error Handling Strategy

If user input contains validation errors (such as a negative number or a missing currency field) or triggers business rule faults (like insufficient funds), the endpoint intercepts the failure using a `@RestControllerAdvice` wrapper and returns an **HTTP 400 Bad Request** code.

### Example Error Response:
```json
{
  "error": "Insufficient funds in currency: USD"
}
```

---

## 📝 Logging Configuration
The system logs runtime operations into rolling text files. You can find your compiled log sheets stored inside the project folder at:
`📄 logs/bank-api.log`

# Banking System POC

A **Proof of Concept (POC)** of a simplified banking system demonstrating modern full-stack development, security basics, and microservices-like architecture.

## 🏗️ Architecture Overview

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│   Banking UI    │      │  System 1       │      │  System 2       │
│   (React)       │ ───► │  Gateway API    │ ───► │  Core Banking   │
│   Port: 3000    │      │  Port: 8081     │      │  Port: 8082     │
└─────────────────┘      └─────────────────┘      └─────────────────┘
                                │                         │
                                │                         ▼
                          Card Range             ┌─────────────────┐
                          Validation             │   H2 Database   │
                          (starts with 4)        │   (In-Memory)   │
                                                 └─────────────────┘
```

## ✨ Features

- **Two-tier transaction processing** (Gateway → Core Banking)
- **Card range-based routing** (only cards starting with '4' are accepted - simulating Visa)
- **Secure PIN authentication** using SHA-256 hashing
- **Card number encryption** using AES-256 for secure storage
- **In-memory H2 database** for cards and transactions
- **Role-based web interface**:
  - **Super Admin**: Monitors all transactions across the system
  - **Customer**: Views own transaction history, balance, and initiates top-up/withdrawal transactions
- **REST APIs** for transaction processing
- **Security**: Never stores or logs plain-text PINs

## 🛠️ Technology Stack

| Layer     | Technology               | Purpose                           |
|-----------|--------------------------|-----------------------------------|
| Backend   | Java 17 + Spring Boot 3  | API & Business Logic              |
| Database  | H2 (in-memory)           | No installation needed            |
| Security  | Spring Security + SHA-256| PIN hashing & authentication      |
| Encryption| AES-256                  | Card number encryption            |
| API       | REST (JSON)              | Communication between systems     |
| Frontend  | React 18 + TypeScript    | Modern, component-based UI        |
| UI Library| Material UI              | Beautiful, responsive design      |
| Build     | Maven (Java), Vite (React)| Fast builds                      |

## 📋 Prerequisites

- **JDK 17** or higher
- **Node.js 18+** and npm
- **Maven** (or use the included Maven wrapper)

## 🚀 Quick Start

### 1. Clone/Navigate to the project

```bash
cd banking-system-poc
```

### 2. Start System 2 (Core Banking) - Port 8082

```bash
cd system2-corebank
./mvnw spring-boot:run
```

On Windows PowerShell:
```powershell
cd system2-corebank
.\mvnw.cmd spring-boot:run
```

### 3. Start System 1 (Gateway) - Port 8081

In a new terminal:
```bash
cd system1-gateway
./mvnw spring-boot:run
```

On Windows PowerShell:
```powershell
cd system1-gateway
.\mvnw.cmd spring-boot:run
```

### 4. Start the React UI - Port 3000

In a new terminal:
```bash
cd banking-ui
npm install
npm run dev
```

### 5. Access the Application

Open your browser and navigate to: **http://localhost:3000**

## 🔐 Test Credentials

### Customers
| Username | Password | Card Number        | PIN  | Initial Balance |
|----------|----------|-------------------|------|-----------------|
| cust1    | pass     | 4123456789012345  | 1234 | $1000.00        |
| cust2    | pass     | 4987654321098765  | 5678 | $2500.00        |

### Administrator
| Username | Password | Role        |
|----------|----------|-------------|
| admin    | admin    | Super Admin |

## 📡 API Endpoints

### System 1 - Gateway (Port 8081)

| Method | Endpoint          | Description                        |
|--------|-------------------|------------------------------------|
| POST   | `/transaction`    | Process a transaction              |
| GET    | `/health`         | Check gateway health               |
| GET    | `/health/system2` | Check core banking availability    |

### System 2 - Core Banking (Port 8082)

| Method | Endpoint                      | Description                    |
|--------|-------------------------------|--------------------------------|
| POST   | `/process`                    | Process transaction (internal) |
| GET    | `/card/{cardNumber}`          | Get card info                  |
| GET    | `/card/by-username/{username}`| Get card by username           |
| GET    | `/transactions/{cardNumber}`  | Get transaction history        |
| GET    | `/transactions/all`           | Get all transactions (admin)   |
| GET    | `/health`                     | Check core banking health      |
| GET    | `/h2-console`                 | H2 Database console            |

## 🧪 Testing with cURL

### Health Check
```bash
# Check Gateway
curl http://localhost:8081/health

# Check Core Banking
curl http://localhost:8082/health
```

### Process a Top-Up Transaction (Success)
```bash
curl -X POST http://localhost:8081/transaction \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4123456789012345",
    "pin": "1234",
    "amount": 100,
    "type": "topup"
  }'
```

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/transaction" -Method POST -ContentType "application/json" -Body '{"cardNumber":"4123456789012345","pin":"1234","amount":100,"type":"topup"}'
```

### Process a Withdrawal (Success)
```bash
curl -X POST http://localhost:8081/transaction \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4123456789012345",
    "pin": "1234",
    "amount": 50,
    "type": "withdraw"
  }'
```

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/transaction" -Method POST -ContentType "application/json" -Body '{"cardNumber":"4123456789012345","pin":"1234","amount":50,"type":"withdraw"}'
```

### Test Invalid Card Range (Should Fail - Card starts with 5)
```bash
curl -X POST http://localhost:8081/transaction \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "5123456789012345",
    "pin": "1234",
    "amount": 50,
    "type": "withdraw"
  }'
```
**Expected Response:** `{"success":false,"message":"Card range not supported..."}`

### Test Invalid Card (Should Fail - Card not in database)
```bash
curl -X POST http://localhost:8081/transaction \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4999999999999999",
    "pin": "1234",
    "amount": 50,
    "type": "withdraw"
  }'
```
**Expected Response:** `{"success":false,"message":"Invalid card"}`

### Test Invalid PIN (Should Fail)
```bash
curl -X POST http://localhost:8081/transaction \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4123456789012345",
    "pin": "9999",
    "amount": 50,
    "type": "withdraw"
  }'
```
**Expected Response:** `{"success":false,"message":"Invalid PIN"}`

### Test Insufficient Balance (Should Fail)
```bash
curl -X POST http://localhost:8081/transaction \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4123456789012345",
    "pin": "1234",
    "amount": 999999,
    "type": "withdraw"
  }'
```
**Expected Response:** `{"success":false,"message":"Insufficient balance"}`

### Get Card Balance
```bash
curl http://localhost:8082/card/4123456789012345
```

### Get Transaction History
```bash
curl http://localhost:8082/transactions/4123456789012345
```

### Get All Transactions (Admin View)
```bash
curl http://localhost:8082/transactions/all
```

## 📁 Project Structure

```
banking-system-poc/
├── system1-gateway/                 # Gateway API (Spring Boot)
│   ├── src/main/java/com/bank/poc/gateway/
│   │   ├── controller/
│   │   │   └── TransactionController.java
│   │   ├── config/
│   │   │   └── SecurityConfig.java
│   │   ├── dto/
│   │   │   ├── TransactionRequest.java
│   │   │   └── TransactionResponse.java
│   │   └── GatewayApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── system2-corebank/                # Core Banking (Spring Boot)
│   ├── src/main/java/com/bank/poc/core/
│   │   ├── controller/
│   │   │   └── ProcessingController.java
│   │   ├── entity/
│   │   │   ├── Card.java
│   │   │   └── Transaction.java
│   │   ├── repository/
│   │   │   ├── CardRepository.java
│   │   │   └── TransactionRepository.java
│   │   ├── service/
│   │   │   └── CardService.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   └── DataInitializer.java
│   │   ├── dto/
│   │   │   ├── TransactionRequest.java
│   │   │   ├── TransactionResponse.java
│   │   │   ├── CardInfoResponse.java
│   │   │   └── TransactionHistoryResponse.java
│   │   └── CoreBankApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── banking-ui/                      # React Frontend
│   ├── src/
│   │   ├── components/
│   │   │   └── Login.tsx
│   │   ├── pages/
│   │   │   ├── CustomerDashboard.tsx
│   │   │   └── AdminDashboard.tsx
│   │   ├── services/
│   │   │   └── api.ts
│   │   ├── types.ts
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── package.json
│   ├── vite.config.ts
│   └── tsconfig.json
│
└── README.md
```

## 🔒 Security Features

1. **PIN Hashing (SHA-256)**: All PINs are hashed using SHA-256 before storage. Plain-text PINs are never stored or logged.
2. **Card Number Encryption (AES-256)**: Card numbers are encrypted for secure storage using AES-256 encryption.
3. **No Plain-Text Logging**: Custom `toString()` methods mask sensitive data in all logs.
4. **Card Range Validation**: Only cards starting with '4' are accepted (simulating Visa cards).
5. **Input Validation**: All inputs (cardNumber, pin, amount, type) are validated before processing.
6. **CORS Configuration**: Configured for frontend communication.

## 🧪 Test Cases

### System 1 (Gateway) Test Cases

| Test ID | Description | Expected Result |
|---------|-------------|-----------------|
| TC-GW-001 | Accept card starting with '4' | Transaction proceeds to System 2 |
| TC-GW-002 | Decline card starting with '5' | "Card range not supported" |
| TC-GW-003 | Decline card starting with '3' | "Card range not supported" |
| TC-GW-004 | Decline card starting with '6' | "Card range not supported" |
| TC-GW-005 | Missing card number | "Card number is required" |
| TC-GW-006 | Empty card number | "Card number is required" |
| TC-GW-007 | Missing PIN | "PIN is required" |
| TC-GW-008 | Zero amount | "Amount must be greater than 0" |
| TC-GW-009 | Negative amount | "Amount must be greater than 0" |
| TC-GW-010 | Missing transaction type | "Transaction type is required" |
| TC-GW-011 | Invalid transaction type | "Invalid transaction type" |
| TC-GW-012 | Valid withdraw type | Transaction proceeds |
| TC-GW-013 | Valid topup type | Transaction proceeds |
| TC-GW-014 | Invalid card format (not 16 digits) | "Card number must be exactly 16 digits" |
| TC-GW-015 | Case-insensitive type (TOPUP) | Transaction proceeds |

### System 2 (Core Banking) Test Cases

| Test ID | Description | Expected Result |
|---------|-------------|-----------------|
| TC-001 | Successful withdrawal with valid card/PIN | Balance decreased, SUCCESS status |
| TC-002 | Successful top-up with valid card/PIN | Balance increased, SUCCESS status |
| TC-003 | Multiple transactions | Balance updated correctly |
| TC-004 | Invalid card number | "Invalid card" |
| TC-005 | Invalid PIN | "Invalid PIN" |
| TC-006 | Insufficient balance (withdrawal) | "Insufficient balance" |
| TC-007 | Inactive card | "Card is inactive" |
| TC-008 | PIN stored as SHA-256 hash | 64-character hash, not plain text |
| TC-009 | PIN verification | Correct PIN verifies, wrong PIN fails |
| TC-010 | Card encryption/decryption | AES-256 works correctly |
| TC-011 | Card masking | Shows only last 4 digits |
| TC-012 | Withdraw exact balance | Balance becomes 0 |
| TC-013 | Small amount (0.01) | Transaction succeeds |
| TC-014 | Large amount (1,000,000) | Transaction succeeds |

### UI Test Cases

| Test ID | Description | Expected Result |
|---------|-------------|-----------------|
| TC-UI-001 | Super Admin login | Admin dashboard displayed |
| TC-UI-002 | Customer login | Customer dashboard displayed |
| TC-UI-003 | Admin sees all transactions | All system transactions listed |
| TC-UI-004 | Customer sees own transactions | Only own transactions listed |
| TC-UI-005 | Customer sees balance | Current balance displayed |
| TC-UI-006 | Customer top-up | Balance increases after top-up |
| TC-UI-007 | Customer withdrawal | Balance decreases after withdrawal |
| TC-UI-008 | Invalid credentials | Error message displayed |

### Running Tests

```bash
# Run System 2 tests
cd system2-corebank
.\mvnw.cmd test

# Run System 1 tests
cd system1-gateway
.\mvnw.cmd test
```

## ⚠️ Important Notes

- This is a **POC/Demo** application - not suitable for production use
- The H2 database is **in-memory** - data is lost when the application restarts
- Authentication is **hardcoded** for demonstration purposes
- In production, use proper authentication (JWT, OAuth2) and persistent databases

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Kill process on port 8081 (Windows)
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Kill process on port 8082 (Windows)
netstat -ano | findstr :8082
taskkill /PID <PID> /F
```

### Maven Not Found
Use the Maven wrapper included in the project:
- Linux/Mac: `./mvnw`
- Windows: `.\mvnw.cmd`

### Node Modules Issues
```bash
cd banking-ui
rm -rf node_modules
npm install
```

## 📊 H2 Console Access

While System 2 is running, access the H2 console at:
- URL: http://localhost:8082/h2-console
- JDBC URL: `jdbc:h2:mem:corebankdb`
- Username: `sa`
- Password: (leave empty)

## ✅ Deliverables Checklist

### System 1 (Gateway API)
- [x] REST API for transactions (`POST /transaction`)
- [x] Input validation (cardNumber, pin, amount, type)
- [x] Card range routing (only cards starting with '4')
- [x] Decline unsupported card ranges with proper message

### System 2 (Core Banking API)
- [x] REST API for validation/processing (`POST /process`)
- [x] Card validation (check if card exists in database)
- [x] PIN validation using SHA-256 hash comparison
- [x] Balance check for withdrawals
- [x] Card number encryption using AES-256

### UI
- [x] Super Admin dashboard (monitors all transactions)
- [x] Customer dashboard (views own data, performs top-ups/withdrawals)
- [x] Role-based access control

### Security
- [x] PIN hashing with SHA-256
- [x] Card number encryption with AES-256
- [x] No plain-text PINs stored or logged

### Documentation
- [x] Setup instructions
- [x] API examples (curl/Postman)
- [x] UI access instructions
- [x] Test cases

### Test Cases
- [x] Successful withdrawal/top-up with valid card/PIN
- [x] Decline for invalid card
- [x] Decline for invalid PIN
- [x] Decline for insufficient balance (withdrawal)
- [x] Decline for unsupported card range
- [x] Super Admin UI shows all transactions
- [x] Customer UI shows own transactions, balance, supports top-ups

## 📝 License

This project is for educational purposes only.

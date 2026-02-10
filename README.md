# Payment Processor Microservices Platform

A comprehensive, event-driven payment processing platform built with Spring Boot microservices architecture. Features distributed transactions, real-time fraud detection, automated ledger management, and webhook delivery with comprehensive test coverage across all services.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Test Coverage](https://img.shields.io/badge/Tests-45%2B-success.svg)](/)

## Overview

A microservices-based payment processing system that handles end-to-end payment workflows including authorization, capture, settlement, refunds, and automated accounting. Built with scalability, reliability, and financial compliance in mind.

### Key Capabilities

- **Multi-Payment Support**: Credit cards and bank account transfers
- **Real-time Processing**: Immediate authorization and settlement for credit transactions
- **Intelligent Fraud Detection**: Multi-factor scoring with configurable thresholds
- **Double-Entry Ledger**: Automated accounting with settlement tracking
- **Webhook Infrastructure**: Reliable event delivery with exponential backoff retry
- **Currency Conversion**: Multi-currency support with automated conversion
- **Merchant Management**: Flexible capture modes, refund policies, and transaction limits

[API ENDPOINTS](https://ejohchisimdi-collab.github.io/PaymentProcessorV2/)


## 🏗 Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                          API Gateway (8085)                       │
│                    (Netflix Zuul - Load Balancer)                │
└────────────────────────────┬─────────────────────────────────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
    ┌─────────▼────────┐ ┌──▼──────────┐ ┌▼─────────────┐
    │   User Service   │ │   Account   │ │   Payment    │
    │     (8080)       │ │   Service   │ │   Service    │
    │                  │ │   (8081)    │ │   (8082)     │
    │ • Authentication │ │ • Vaults    │ │ • Processing │
    │ • User Mgmt      │ │ • Accounts  │ │ • Refunds    │
    │ • Settings       │ │ • Balances  │ │ • Fraud      │
    └──────────────────┘ └─────────────┘ └──────────────┘
              │              │              │
              └──────────────┼──────────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
    ┌─────────▼────────┐ ┌──▼──────────┐ ┌▼─────────────┐
    │  Ledger Service  │ │  Webhook    │ │   Eureka     │
    │     (8083)       │ │  Service    │ │   Server     │
    │                  │ │  (8084)     │ │   (8761)     │
    │ • Double Entry   │ │ • Delivery  │ │ • Discovery  │
    │ • Settlements    │ │ • Retries   │ │ • Registry   │
    │ • Accounting     │ │ • HMAC      │ │              │
    └──────────────────┘ └─────────────┘ └──────────────┘
              │              │
              └──────────────┼──────────────┐
                             │              │
                   ┌─────────▼────────┐ ┌──▼──────────┐
                   │  Apache Kafka    │ │    MySQL    │
                   │    (9092)        │ │   (3307)    │
                   │                  │ │             │
                   │ • Events         │ │ • 6 DBs     │
                   │ • Messaging      │ │ • Isolated  │
                   └──────────────────┘ └─────────────┘
```

### Communication Patterns

- **Synchronous**: REST APIs with Circuit Breakers (Resilience4j)
- **Asynchronous**: Kafka event streaming for cross-service events
- **Service Discovery**: Netflix Eureka for dynamic service registration
- **Load Balancing**: Spring Cloud Gateway with client-side load balancing

## ✨ Features

### Core Payment Processing

-  **Multiple Payment Methods**: Credit cards, bank accounts
-  **Real-time Authorization**: Instant payment validation and fund reservation
-  **Deferred Settlement**: Scheduled batch processing for bank transfers
-  **Idempotency**: Prevent duplicate transactions with idempotency keys
-  **Currency Conversion**: Automatic multi-currency support with fee calculation
-  **Platform Fees**: Configurable transaction fees with automatic calculation

### Security & Compliance

-  **JWT Authentication**: Stateless token-based authentication
-  **Role-Based Access Control**: Admin, Merchant, Customer, and Service roles
-  **Account Encryption**: AES-256-GCM encryption for sensitive account data
-  **HMAC Webhook Signing**: Cryptographic verification of webhook payloads
-  **Circuit Breakers**: Fault tolerance with automatic fallback mechanisms

### Fraud Detection Engine

- ️ **Multi-Factor Scoring**: Comprehensive fraud risk assessment
- ️ **Velocity Checks**: Transaction frequency monitoring
- ️ **Behavioral Analysis**: Historical pattern detection
- ️ **Real-time Validation**: Instant fraud scoring during authorization
- ️ **Configurable Thresholds**: Merchant-specific fraud rules

### Merchant Features

-  **Capture Modes**: Automatic or manual payment capture
-  **Refund Policies**: Full or partial refund support
-  **Transaction Limits**: Configurable per-transaction caps
-  **Currency Settings**: Multi-currency merchant accounts
-  **Webhook Endpoints**: Custom merchant notification URLs
-  **Settlement Reports**: Automated ledger and payout tracking

### Reliability & Resilience

-  **Optimistic Locking**: Prevent concurrent modification conflicts
-  **Retry Mechanisms**: Exponential backoff for transient failures
-  **Webhook Queuing**: Failed webhooks automatically retried (up to 7 times)
-  **Transaction Isolation**: ACID compliance for all financial operations
-  **Distributed Tracing**: (Coming soon) Request tracking across services
-  **Health Checks**: Automated service health monitoring

### Accounting & Ledger

- 📊 **Double-Entry Bookkeeping**: Complete audit trail for all transactions
- 📊 **Automated Settlements**: Scheduled merchant payouts
- 📊 **Split Accounting**: Platform fees, merchant earnings, tax liability
- 📊 **Maturity Tracking**: Hold periods before settlement
- 📊 **Reconciliation**: Built-in ledger verification

## 🔧 Services Overview

### User Service (Port 8080)
**Responsibilities**: Authentication, user management, merchant settings

**Key Endpoints**:
- `POST /users/register` - User registration
- `POST /users/login` - Authentication
- `POST /settings/` - Create merchant settings
- `GET /settings/{merchantId}` - Retrieve merchant configuration

**Database**: `user_service`

### Account Service (Port 8081)
**Responsibilities**: Account management, vault operations, balance tracking

**Key Endpoints**:
- `POST /merchants/bank-accounts/` - Create merchant bank account
- `POST /customers/credit-cards/` - Create customer credit card
- `GET /accounts/{token}/balance` - Check account balance
- `POST /reserve-funds` - Reserve funds for payment

**Database**: `account_service`

**Features**:
- Encrypted account storage (AES-256-GCM)
- Version-based optimistic locking
- Pending balance tracking
- Multi-currency support

### Payment Service (Port 8082)
**Responsibilities**: Payment processing, refunds, fraud detection

**Key Endpoints**:
- `POST /pay` - Process payment
- `POST /capture/{paymentId}` - Manual capture
- `POST /refunds/` - Process refund
- `GET /payments/{merchantId}` - Retrieve merchant payments

**Database**: `payment_service`

**Payment States**:
```
CREDIT_PENDING → VALIDATED → AUTHORISED → CAPTURED → SETTLED
BANK_PENDING → VALIDATED → SETTLED
```

### Ledger Service (Port 8083)
**Responsibilities**: Double-entry accounting, settlement tracking

**Key Endpoints**:
- `GET /ledgers/payments/{paymentId}` - Retrieve ledger entries
- `GET /ledger-entries/payments/{paymentId}` - Detailed entry history
- `GET /splits/payments/{paymentId}` - View payment splits
- `GET /refunds/payments/{paymentId}` - Refund accounting

**Database**: `ledger_service`

**Ledger Entry Types**:
- `EXTERNAL_PSP_RECEIVABLE` - Funds in transit
- `MERCHANT_PENDING_BALANCE` - Awaiting settlement
- `PLATFORM_FEE_REVENUE` - Platform earnings
- `MERCHANT_AVAILABLE_BALANCE` - Ready for payout
- `MERCHANT_PAYOUT_IN_TRANSIT` - Settlement in progress

### Webhook Service (Port 8084)
**Responsibilities**: Event notification, webhook delivery, retry logic

**Kafka Topics Consumed**:
- `authorization-completed`
- `payment-captured`
- `payment-settled`
- `refund-completed`

**Features**:
- HMAC-SHA256 signature verification
- Exponential backoff retry (7 attempts max)
- Automatic dead letter queue for permanent failures
- Timestamp-based replay protection

### API Gateway (Port 8085)
**Responsibilities**: Request routing, load balancing

**Routes**:
- `/user-service/**` → User Service
- `/account-service/**` → Account Service
- `/payment-service/**` → Payment Service
- `/ledger-service/**` → Ledger Service

### Eureka Server (Port 8761)
**Responsibilities**: Service discovery, health monitoring

**Dashboard**: `http://localhost:8761`

## 🛠 Tech Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.x |
| **Security** | Spring Security + JWT |
| **Database** | MySQL 8.0 |
| **ORM** | Spring Data JPA (Hibernate) |
| **Message Broker** | Apache Kafka 7.5.0 |
| **Service Discovery** | Netflix Eureka |
| **API Gateway** | Spring Cloud Gateway |
| **Resilience** | Resilience4j (Circuit Breakers) |
| **Object Mapping** | MapStruct |
| **Currency** | JavaMoney (JSR 354) |
| **Build Tool** | Maven 3.9+ |
| **Containerization** | Docker & Docker Compose |
| **Testing** | JUnit 5, Mockito, AssertJ |
| **Documentation** | Swagger/OpenAPI 3 |
| **Logging** | SLF4J + Logback |

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.9+**
- **Docker** and **Docker Compose**
- **MySQL 8.0** (if running locally)

### Quick Start with Docker (Recommended)

```bash
# Clone the repository
git clone https://github.com/ejohchisimdi-collab/PaymentProcessorV2.git
cd PaymentProcessorV2

# Start all services
docker-compose up -d

# Check service health
docker-compose ps

# View logs
docker-compose logs -f payment-service
```

**Services will be available at**:
- API Gateway: `http://localhost:8085`
- Eureka Dashboard: `http://localhost:8761`
- User Service: `http://localhost:8080`
- Account Service: `http://localhost:8081`
- Payment Service: `http://localhost:8082`
- Ledger Service: `http://localhost:8083`

### Local Development Setup

```bash
# 1. Clone repository
git clone https://github.com/ejohchisimdi-collab/PaymentProcessorV2.git
cd PaymentProcessorV2

# 2. Start MySQL
docker run -d \
  --name payment-mysql \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -p 3307:3306 \
  mysql:8.0

# 3. Set environment variables
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/payment_service?createDatabaseIfNotExist=true
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=rootpassword
export JWT_SECRET=YourSecretKeyMustBeAtLeast32CharactersLongForHS256
export JWT_EXPIRATION=86400000
export ENCODER_KEY=m8H2p7uKQzJ7e5Ck7r9q2n8c6FZxM0vQH3lW8YJ9uA4=
export USER_SERVICE_URL=http://localhost:8083
export ACCOUNT_SERVICE_URL=http://localhost:8081

# 4. Start Kafka and Zookeeper
docker-compose up -d zookeeper kafka

# 5. Build all services
./mvnw clean install -DskipTests

# 6. Start services in order
cd eureka-server && ./mvnw spring-boot:run &
cd user-service && ./mvnw spring-boot:run &
cd account-service && ./mvnw spring-boot:run &
cd payment-service && ./mvnw spring-boot:run &
cd ledger-service && ./mvnw spring-boot:run &
cd webhook-service && ./mvnw spring-boot:run &
cd api-gateway && ./mvnw spring-boot:run &
```

### Default Admin Account

```json
{
  "userName": "Admin",
  "password": "Admin",
  "email": "admin@payment.com",
  "roles": "Admin",
  "name": "System Administrator"
}
```

**⚠️ Change credentials immediately in production!**

## 📚 API Documentation

### Swagger UI

Access interactive API documentation:
- User Service: `http://localhost:8080/swagger-ui.html`
- Account Service: `http://localhost:8081/swagger-ui.html`
- Payment Service: `http://localhost:8082/swagger-ui.html`
- Ledger Service: `http://localhost:8083/swagger-ui.html`

### Authentication

All endpoints (except registration and login) require JWT authentication or api key/ merchant secret:

```bash
# 1. Register a merchant
curl -X POST http://localhost:8083/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "merchant1",
    "email": "merchant@example.com",
    "password": "SecurePass123",
    "roles": "Merchant",
    "name": "Test Merchant"
  }'

# 2. Login to get JWT token
curl -X POST http://localhost:8083/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "merchant1",
    "password": "SecurePass123"
  }'

# Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 2,
  "role": "Merchant",
  "userName": "merchant1"
}

# 3. Use token in subsequent requests
curl -X GET http://localhost:8081/bank-accounts/2 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Complete Payment Workflow

```bash
# 1. Create merchant settings
curl -X POST http://localhost:8083/settings/ \
  -H "Authorization: Bearer {MERCHANT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": 2,
    "currency": "USD",
    "merchantEndpoint": "https://merchant.example.com/webhooks",
    "maxTransactionLimit": 10000.00,
    "captureType": "AUTOMATIC",
    "refundType": "PARTIAL"
  }'

# 2. Create merchant account
curl -X POST http://localhost:8081/merchants/bank-accounts/ \
  -H "Authorization: Bearer {MERCHANT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "1234567890",
    "userId": 2,
    "currency": "USD",
    "balance": 0.00
  }'

# Response includes encrypted token:
{
  "accountNumber": "1234567890",
  "userId": 2,
  "ownerType": "MERCHANT",
  "currency": "USD",
  "balance": 0.00,
  "pendingAccount": 0.00
}

# 3. Create customer account (as customer)
curl -X POST http://localhost:8081/customers/credit-cards/ \
  -H "Authorization: Bearer {CUSTOMER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "4111111111111111",
    "userId": 3,
    "currency": "USD",
    "creditLimit": 5000.00
  }'

# 4. Process payment
curl -X POST http://localhost:8082/pay \
  -H "x-api-Key: {MERCHANT_HMAC_SECRET}" \
  -H "Idempotency-Key: unique-payment-id-12345" \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": 2,
    "merchantAccountToken": "{ENCRYPTED_MERCHANT_TOKEN}",
    "customerAccountToken": "{ENCRYPTED_CUSTOMER_TOKEN}",
    "amount": 100.00
  }'

# Response:
{
  "id": 1,
  "createdAt": "2025-01-29T10:30:00",
  "authorizationDueDate": "2025-02-05T10:30:00",
  "currency": "USD",
  "customerAccount": "{ENCRYPTED_CUSTOMER_TOKEN}",
  "merchantAccount": "{ENCRYPTED_MERCHANT_TOKEN}",
  "merchantId": 2,
  "accountType": "CREDIT",
  "amount": 100.00,
  "conversionFee": 0.00,
  "platformFee": 3.00,
  "paymentStatus": "SETTLED",
  "warnings": [],
  "amountAfterConversion": 100.00
}
```

## 💳 Payment Flows

### Credit Card Payment Flow

```
┌─────────────┐
│   Customer  │
│   Initiates │
│   Payment   │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────────────────────┐
│                  CREDIT_PENDING                         │
│  • Payment created                                      │
│  • Initial validation (currency, limits)                │
└──────┬──────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────┐
│                   VALIDATED                             │
│  • Fraud detection (score < 5)                         │
│  • Balance verification                                 │
│  • Currency conversion (if needed)                      │
└──────┬──────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────┐
│                  AUTHORISED                             │
│  • Funds reserved (amount + conversion fee)            │
│  • Authorization expires in 7 days                      │
│  • Webhook: PAYMENT_AUTHORISED                         │
└──────┬──────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────┐
│                   CAPTURED                              │
│  • Payment captured (auto or manual)                    │
│  • Ledger entry: Split created                         │
│  • Webhook: PAYMENT_CAPTURED                           │
└──────┬──────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────┐
│                   SETTLED                               │
│  • Merchant pending balance updated                     │
│  • Platform fee recorded                                │
│  • Webhook: PAYMENT_SETTLED                            │
│  • After 3 days: Available for payout                   │
└─────────────────────────────────────────────────────────┘
```

### Bank Account Payment Flow

```
┌─────────────┐
│   Customer  │
│   Initiates │
│   Payment   │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────────────────────┐
│                  BANK_PENDING                           │
│  • Payment created immediately                          │
│  • Queued for daily validation                         │
└──────┬──────────────────────────────────────────────────┘
       │
       ▼ (Scheduled daily at midnight)
┌─────────────────────────────────────────────────────────┐
│              VALIDATION & SETTLEMENT                     │
│  • Fraud detection executed                             │
│  • Balance verification                                 │
│  • If passed: Funds reserved → SETTLED                  │
│  • If failed: FAILED status                            │
│  • Webhooks: PAYMENT_CAPTURED + PAYMENT_SETTLED         │
└─────────────────────────────────────────────────────────┘
```

### Refund Flow

```
┌─────────────┐
│   Merchant  │
│   Requests  │
│   Refund    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────────────────────┐
│                    PENDING                              │
│  • Refund request created                               │
│  • Payment must be SETTLED                              │
└──────┬──────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────┐
│                   VALIDATED                             │
│  • Merchant balance check                               │
│  • Refund policy verification (PARTIAL/COMPLETE)        │
│  • Remaining refund amount validation                   │
└──────┬──────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────┐
│                  COMPLETED                              │
│  • Funds returned to customer                           │
│  • Merchant balance debited                             │
│  • Ledger refund entry created                         │
│  • Webhook: PAYMENT_REFUNDED                           │
└─────────────────────────────────────────────────────────┘
```

##  Fraud Detection

### Scoring System

| Check | Score | Description |
|-------|-------|-------------|
| **First Transaction + High Amount** | +2 | No prior transactions and amount > $10,000 USD |
| **Spending Spike** | +2 | Current transaction > 2× average of last 10 |
| **High Velocity** | +2 | Today's frequency > 1.5× last week's average |
| **Round Number** | +1 | Amount is exact multiple of $100 |
| **Dormant Reactivation** | +2 | Last transaction > 6 months ago |
| **Rapid Transactions** | +1 | 3 transactions in < 1 minute |
| **Consecutive Failures** | +2 | Last 3 payments failed |
| **Insufficient Funds** | Auto-Fail | Balance < (amount + fees) |

**Decision Rules**:
- Score ≥ 5: Payment **FAILED**
- Score < 5: Payment proceeds to **AUTHORISED**

### Example Scenarios

```java
// Scenario 1: New customer, large purchase
Customer: New account (no history)
Amount: $15,000
Score: +2 (high first transaction)
Result: Flagged for review

// Scenario 2: Spending pattern anomaly
Customer: Average spend $500
Current: $2,000
Score: +2 (spending spike)
Result: Requires additional verification

// Scenario 3: Velocity attack
Transactions: 5 payments in 30 seconds
Score: +1 (rapid transactions) + +2 (high velocity)
Result: Blocked (score = 3, flagged for investigation)
```

## 📊 Ledger System

### Double-Entry Accounting

Every transaction creates balanced ledger entries:

```
Payment Capture ($100):
┌───────────────────────────────────────┬────────────┬─────────────┐
│ Account                                │ Debit      │ Credit      │
├───────────────────────────────────────┼────────────┼─────────────┤
│ EXTERNAL_PSP_RECEIVABLE                │ $100.00    │             │
│ MERCHANT_PENDING_BALANCE               │            │ $97.00      │
│ PLATFORM_FEE_REVENUE                   │            │ $3.00       │
└───────────────────────────────────────┴────────────┴─────────────┘

After Maturity (3 days):
┌───────────────────────────────────────┬────────────┬─────────────┐
│ MERCHANT_PENDING_BALANCE               │ $97.00     │             │
│ MERCHANT_AVAILABLE_BALANCE             │            │ $97.00      │
└───────────────────────────────────────┴────────────┴─────────────┘

Payout Initiation:
┌───────────────────────────────────────┬────────────┬─────────────┐
│ MERCHANT_AVAILABLE_BALANCE             │ $97.00     │             │
│ MERCHANT_PAYOUT_IN_TRANSIT             │            │ $97.00      │
└───────────────────────────────────────┴────────────┴─────────────┘

Payout Completion:
┌───────────────────────────────────────┬────────────┬─────────────┐
│ MERCHANT_PAYOUT_IN_TRANSIT             │ $97.00     │             │
│ PLATFORM_CASH_ACCOUNT                  │            │ $97.00      │
└───────────────────────────────────────┴────────────┴─────────────┘
```

### Settlement Schedule

```
Day 0: Payment captured
       ├─ Split created
       └─ Merchant pending balance: $97.00

Day 3: Maturity reached (00:00 UTC)
       ├─ Ledger entry created
       ├─ Merchant available balance: $97.00
       └─ Payout initiated

Day 3: Payout completed
       └─ Funds transferred to merchant bank
```

## 🧪 Testing

### Run All Tests

```bash
# Run all service tests
./mvnw clean test

# Run specific service tests
cd payment-service && ./mvnw test
cd account-service && ./mvnw test
cd user-service && ./mvnw test
```

### Test Coverage

| Service | Unit Tests | Coverage |
|---------|------------|----------|
| Payment Service | 21+        | Core business logic |
| Account Service | 19+        | Vault and balance operations |
| User Service | 8+         | Authentication and settings |
| **Total** | **45+**    | **Comprehensive** |

### Sample Tests

```java
// FraudServiceTest.java
@Test
void calculateFraudTest_BigFirstTransaction() {
    // Arrange
    String customerToken = "abc";
    BigDecimal amount = BigDecimal.valueOf(100002);
    when(paymentRepository.findByCustomerAccount(customerToken))
        .thenReturn(new ArrayList<>());

    // Act
    FraudUtil fraudUtil = fraudService.calculateFraud(
        customerToken, amount, "USD"
    );

    // Assert
    assertThat(fraudUtil.getFraudScore()).isEqualTo(2);
    assertThat(fraudUtil.getWarnings().get(0))
        .contains("high transaction");
}
```

## 🚢 Deployment

### Docker Compose Production

```bash
# Build all images
docker-compose build

# Start in production mode
docker-compose -f docker-compose.yml up -d

# Scale services
docker-compose up -d --scale payment-service=3

# Monitor logs
docker-compose logs -f --tail=100

# Stop all services
docker-compose down
```



## ⚙️ Configuration

### Environment Variables

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/{service}_service
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=rootpassword

# Security
JWT_SECRET=YourSuperSecretKeyThatIsAtLeast32CharactersLong
JWT_EXPIRATION=86400000  # 24 hours in milliseconds
ENCODER_KEY=Base64EncodedAES256Key

# Service URLs
USER_SERVICE_URL=http://user-service:8083
ACCOUNT_SERVICE_URL=http://account-service:8081

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092

# Eureka
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka
```

### Application Properties

```properties
# Payment Service Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Resilience4j Circuit Breaker
resilience4j.circuitbreaker.instances.userService.slidingWindowSize=5
resilience4j.circuitbreaker.instances.userService.failureRateThreshold=50
resilience4j.circuitbreaker.instances.userService.waitDurationInOpenState=10s

# Kafka Topics
# Topics are auto-created by services
```

### Scheduled Jobs

```java
// Ledger Service - Daily at midnight
@Scheduled(cron = "0 0 0 * * *")
public void processLedger() {
    List<LedgerEntries> entries = createMaturity();
    initiateBankTransfer(entries);
}

// Payment Service - Every 5 seconds (tunable)
@Scheduled(cron = "*/5 * * * * *")
public void failPayments() {
    // Expire uncaptured authorizations
}

// Webhook Service - Hourly retry
@Scheduled(cron = "0 0 * * * *")
public void retryWebhooks() {
    // Retry failed webhook deliveries
}
```


## 📄 License

This project is licensed under the MIT License 

## ‍💻 Author

**Chisimdi Ejoh**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue)](https://www.linkedin.com/in/chisimdi-ejoh-057ba1382)
[![GitHub](https://img.shields.io/badge/GitHub-Follow-black)](https://github.com/ejohchisimdi-collab)

##  Acknowledgments

- Spring Boot team for the excellent framework
- The open-source community for invaluable tools and libraries
- Stripe and PayPal for architectural inspiration
- Martin Fowler for microservices patterns guidance

## ⚠️ Disclaimer

This is a **demonstration project** for portfolio purposes. For production use, additional measures are required:

-  PCI DSS compliance for card data handling
-  Comprehensive security audit
-  Legal compliance (GDPR, PSD2, etc.)
-  Professional penetration testing
-  Production-grade monitoring and alerting
-  Disaster recovery procedures
-  Full integration test suite
-  Performance and load testing

**Do not use in production without proper security hardening and compliance review.**

---

**Built  By Chisimdi Ejoh** | [Portfolio](https://github.com/ejohchisimdi-collab)

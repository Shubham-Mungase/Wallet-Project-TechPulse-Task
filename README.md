# Wallet Service API

A Spring Boot REST API for managing **Users, Wallets, and Transactions**.

The application provides JWT-based authentication, wallet management, money transfers, transaction history, request validation, global exception handling, transaction safety, API documentation with Swagger/OpenAPI, automated testing, JaCoCo code coverage, and SonarQube code-quality analysis.

---

## Features

* User registration and authentication
* JWT-based authentication using Spring Security
* Wallet creation and wallet balance management
* Add money to a wallet
* Transfer money between wallets
* Transaction history
* Idempotency key support for transaction requests
* Request payload validation
* Global exception handling
* Transactional wallet operations using `@Transactional`
* Pessimistic locking for concurrent wallet updates
* Optimistic locking using JPA `@Version`
* Swagger/OpenAPI documentation
* H2 database for local development
* JUnit testing
* JaCoCo code coverage with an 80% coverage gate
* SonarQube integration for static code-quality analysis

---

# Tech Stack

| Technology        | Purpose                          |
| ----------------- | -------------------------------- |
| Java 21           | Programming language             |
| Spring Boot 4.0.8 | Application framework            |
| Spring Web MVC    | REST APIs                        |
| Spring Data JPA   | Database access                  |
| Hibernate         | ORM                              |
| H2                | Development database             |
| Spring Security   | Authentication and authorization |
| JJWT              | JWT generation and validation    |
| Lombok            | Boilerplate reduction            |
| Springdoc OpenAPI | Swagger/API documentation        |
| JUnit 5           | Testing                          |
| Mockito           | Unit testing/mocking             |
| JaCoCo            | Code coverage                    |
| SonarQube         | Static code analysis             |
| Maven             | Build and dependency management  |

---

# Project Structure

```text
src/
├── main/
│   ├── java/com/wallet/
│   │   ├── WalletServiceApiApplication.java
│   │   │
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   │
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Wallet.java
│   │   │   └── Transaction.java
│   │   │
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── WalletRepository.java
│   │   │   └── TransactionRepository.java
│   │   │
│   │   ├── restcontroller/
│   │   │
│   │   ├── service/
│   │   │
│   │   ├── serviceimpl/
│   │   │
│   │   ├── security/
│   │   │   ├── JWT filter/configuration
│   │   │   └── authentication components
│   │   │
│   │   ├── exceptions/
│   │   │
│   │   └── enums/
│   │
│   └── resources/
│       └── application.yml
│
└── test/
    └── java/
        └── com/wallet/
```

The application follows a layered architecture:

```text
Client
   │
   ▼
REST Controller
   │
   ▼
Service Interface
   │
   ▼
Service Implementation
   │
   ▼
Spring Data JPA Repository
   │
   ▼
H2 Database
```

Security and exception handling are applied across the application.

---

# API Flow

## 1. User Registration

```text
Client
  │
  │ POST /auth/register
  ▼
AuthController
  │
  ▼
AuthService
  │
  ├── Validate request
  ├── Check existing user
  ├── Encode password
  ├── Create user
  └── Create wallet
  │
  ▼
Database
```

---

## 2. Login

```text
Client
  │
  │ email + password
  ▼
POST /auth/login
  │
  ▼
AuthenticationManager
  │
  ▼
Spring Security
  │
  ▼
JWT Token
  │
  ▼
Client
```

The generated JWT is then sent with subsequent protected requests:

```http
Authorization: Bearer <JWT_TOKEN>
```

---

# Wallet Transaction Flow

For a transfer request:

```text
Client
  │
  │ JWT + Idempotency-Key
  ▼
WalletController
  │
  ▼
WalletService
  │
  ├── Validate request
  ├── Authenticate sender
  ├── Check idempotency key
  ├── Find sender wallet
  ├── Find receiver wallet
  ├── Acquire database locks
  ├── Check available balance
  ├── Debit sender
  ├── Credit receiver
  ├── Create transaction record
  └── Commit transaction
  │
  ▼
Database
```

The balance update and transaction creation are executed inside a transactional boundary.

---

# Concurrency Handling

Wallet balance updates are sensitive to concurrent requests.

The application uses:

### Pessimistic locking

Wallet records are locked using:

```java
LockModeType.PESSIMISTIC_WRITE
```

This prevents multiple concurrent transactions from modifying the same wallet balance simultaneously.

### Optimistic locking

The `Wallet` entity also uses JPA's `@Version` mechanism to detect conflicting updates.

### Deterministic lock ordering

When transferring money between two wallets, the wallets are locked in a deterministic order based on their IDs.

For example:

```text
Wallet 10 → Wallet 20

Lock Wallet 10
Lock Wallet 20
Transfer
Unlock
```

A reverse transfer:

```text
Wallet 20 → Wallet 10
```

still locks:

```text
Wallet 10
Wallet 20
```

This reduces the possibility of deadlocks caused by transactions acquiring the same resources in opposite orders.

---

# Idempotency

Transfer requests support an `Idempotency-Key`.

The purpose is to prevent accidental duplicate transaction processing when a client retries a request.

Example:

```http
POST /wallet/transfer
Authorization: Bearer <JWT>
Idempotency-Key: 7f3c9e21-001
Content-Type: application/json
```

If the same idempotency key is submitted again, the application checks whether the transaction has already been processed.

The database also enforces uniqueness for the idempotency key.

This is particularly useful for payment/wallet APIs where network failures can cause clients to retry requests.

---

# Validation

Request DTOs use Jakarta Bean Validation.

Examples include:

* Required fields
* Email validation
* Password validation
* Positive transaction amounts
* Minimum transaction amount
* Request field constraints

Invalid requests are rejected before business logic is executed.

Example:

```json
{
  "amount": -100
}
```

will fail validation instead of reaching the transaction logic.

---

# Global Exception Handling

The application uses centralized exception handling through a global exception handler.

Business exceptions include cases such as:

* User not found
* Wallet not found
* Duplicate user
* Insufficient balance
* Invalid transaction
* Duplicate/idempotent transaction request

Instead of returning stack traces to clients, the API returns structured error responses with appropriate HTTP status codes.

Example:

```json
{
  "message": "Insufficient wallet balance",
  "status": 400
}
```

Unexpected server-side exceptions are handled separately without exposing internal implementation details to the API consumer.

---

# Database Model

The application contains three main entities:

```text
User
  │
  │
  ▼
Wallet
  │
  │
  ▼
Transaction
```

### User

Stores user authentication and account information.

### Wallet

Stores wallet information and current balance.

The wallet contains an optimistic locking version field to support concurrent updates.

### Transaction

Stores financial transaction information such as:

* Transaction ID
* Source wallet
* Destination wallet
* Amount
* Transaction type
* Transaction status
* Idempotency key
* Creation timestamp

---

# API Documentation

Swagger/OpenAPI is integrated into the application.

After starting the application, open:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger can be used to:

* Explore available endpoints
* View request/response models
* Authorize using JWT
* Execute API requests
* Inspect HTTP responses

---

# Running the Application

## Prerequisites

* Java 21
* Maven or Maven Wrapper
* Git

The project includes the Maven Wrapper, so Maven does not need to be installed separately.

## Clone the repository

```bash
git clone https://github.com/Shubham-Mungase/Wallet-Project-TechPulse-Task
cd Wallet-Service
```

## Run tests

```bash
./mvnw test
```

## Run tests with coverage verification

```bash
./mvnw clean verify
```

The Maven build includes a JaCoCo coverage check with a minimum coverage requirement of **80%**.

## Start the application

```bash
./mvnw spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

---

# H2 Database

H2 is used for local/development persistence.

H2 Console:

```text
http://localhost:8080/h2-console
```

The exact JDBC URL and other database configuration are defined in:

```text
src/main/resources/application.yml
```

---

# Testing

The project contains automated tests for controllers, services, validation, security and persistence-related functionality.

Run:

```bash
./mvnw test
```

For the complete build and coverage verification:

```bash
./mvnw clean verify
```

JaCoCo generates the coverage report under:

```text
target/site/jacoco/
```

The Maven build is configured to fail when the project coverage falls below the required **80% threshold**.

---

# SonarQube

SonarQube is integrated using the Maven Sonar plugin.

Start a local SonarQube server and then execute:

```bash
./mvnw clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<SONAR_TOKEN>
```

The SonarQube project configuration is defined in `pom.xml`.

SonarQube can be used to inspect:

* Bugs
* Code smells
* Vulnerabilities
* Duplicated code
* Maintainability
* Reliability
* Security
* Test coverage

# API Endpoints

## Authentication APIs

| Method | Endpoint         | Authentication | Description                           |
| ------ | ---------------- | -------------- | ------------------------------------- |
| `POST` | `/auth/register` |  Public       | Register a new user and create wallet |
| `POST` | `/auth/login`    |  Public       | Authenticate user and receive JWT     |

### Register

```http
POST /auth/register
Content-Type: application/json
```

### Login

```http
POST /auth/login
Content-Type: application/json
```

The JWT returned from login is required for protected wallet and admin APIs.

---

## Wallet APIs

| Method | Endpoint               | Authentication | Description                                         |
| ------ | ---------------------- | -------------- | --------------------------------------------------- |
| `POST` | `/wallet/add`          | ✅ JWT          | Add money to the authenticated user's wallet        |
| `POST` | `/wallet/transfer`     | ✅ JWT          | Transfer money to another user's wallet             |
| `GET`  | `/wallet`              | ✅ JWT          | Get the authenticated user's wallet                 |
| `GET`  | `/wallet/transactions` | ✅ JWT          | Get transactions of the authenticated user's wallet |

For protected APIs, send:

```http
Authorization: Bearer <JWT_TOKEN>
```

### Add Money

```http
POST /wallet/add
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

### Transfer Money

```http
POST /wallet/transfer
Authorization: Bearer <JWT_TOKEN>
Idempotency-Key: <UNIQUE_KEY>
Content-Type: application/json
```

The `Idempotency-Key` is used to safely handle repeated transaction requests.

### Get Wallet

```http
GET /wallet
Authorization: Bearer <JWT_TOKEN>
```

### Get Transactions

```http
GET /wallet/transactions
Authorization: Bearer <JWT_TOKEN>
```

---

## Admin APIs

| Method | Endpoint              | Authentication | Description                      |
| ------ | --------------------- | -------------- | -------------------------------- |
| `GET`  | `/admin/wallets`      | ✅ Admin JWT    | Retrieve wallet information      |
| `GET`  | `/admin/transactions` | ✅ Admin JWT    | Retrieve transaction information |

Admin endpoints require an authenticated user with the appropriate admin role.

### Get All Wallets

```http
GET /admin/wallets
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

### Get All Transactions

```http
GET /admin/transactions
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

---

## Complete API Flow

```text
                    AUTHENTICATION
                         │
             ┌───────────┴───────────┐
             ▼                       ▼
       POST /auth/register     POST /auth/login
                                     │
                                     ▼
                                  JWT Token
                                     │
                 ┌───────────────────┴──────────────────┐
                 │                                      │
                 ▼                                      ▼
             WALLET APIs                            ADMIN APIs
                 │                                      │
       ┌─────────┼─────────┐                    ┌───────┴────────┐
       ▼         ▼         ▼                    ▼                ▼
    POST /add  POST      GET /wallet       GET /wallets    GET /transactions
              /transfer
                           │
                           ▼
                   GET /transactions
```

---

# Configuration

Application configuration is maintained using:

```text
src/main/resources/application.yml
```

Sensitive values such as JWT secrets should be supplied through environment variables rather than committed as plaintext secrets.

Example:

```yaml
jwt:
  secret: ${JWT_SECRET}
```

Set the environment variable before running the application:

```bash
export JWT_SECRET="random-32-character"
```

---

# Design Decisions

## Layered architecture

The project separates responsibilities between controllers, services, repositories and entities.

This improves maintainability and makes individual components easier to test.

## DTOs

Request and response DTOs are used instead of exposing entities directly through the API.

## Transaction management

Wallet balance changes and transaction creation are performed within transactional service operations to maintain consistency.

## Concurrency control

Pessimistic locking is used for critical wallet balance updates, while `@Version` provides optimistic locking support.

## Idempotency

An idempotency key is used for transaction requests to handle client retries safely.

## H2

H2 was selected because the assignment can be run locally without requiring an external database server.

---

# Assumptions

* A user owns a wallet used for wallet operations.
* Transfers require an authenticated user.
* A sender cannot transfer money to the same wallet.
* The sender must have sufficient balance before a transfer is completed.
* Transaction amounts must be positive.
* Each idempotency key represents a unique transaction request.
* H2 is intended for local/development execution rather than production financial persistence.

---

# Build

Run the complete Maven verification:

```bash
./mvnw clean verify
```

This performs the project tests and executes the configured JaCoCo coverage verification.

---

# Future Production Improvements

For a production financial system, the following could additionally be considered:

* PostgreSQL/MySQL instead of H2
* Flyway or Liquibase database migrations
* External secret management
* Docker containerization
* CI/CD pipeline
* Centralized logging and monitoring
* Distributed tracing
* Rate limiting
* Refresh-token management
* Audit logging
* Production-grade observability

These are intentionally outside the core scope of this assignment.

---

# Author

**Shubham Mungase**

Java / Spring Boot Developer

# Wallet Service API

## What this is
A small Spring Boot (Java 21) Wallet Service API that manages Users, Wallets and Transactions. It provides REST endpoints, JWT-based security, uses H2 for local persistence, and includes test & quality tooling (JaCoCo + Sonar) configured in the Maven build.

## Tech stack
- Language: Java 21
- Framework: Spring Boot 4.0.8
- Persistence: Spring Data JPA (H2 at runtime)
- Security: Spring Security + jjwt (JWT)
- API docs: springdoc OpenAPI (Swagger UI)
- Build & tooling: Maven (wrapper included), JaCoCo (coverage), Sonar Maven plugin
- Notable libraries: Lombok, spring-boot-starter-webmvc, spring-boot-starter-data-jpa

## Quick file map
```
pom.xml                       # Maven build + dependencies + jacoco + sonar config
src/main/java/com/wallet/
  WalletServiceApiApplication.java   # Spring Boot entrypoint
  restcontroller/        # HTTP controllers / endpoints
  service/               # service interfaces
  serviceimpl/           # service implementations (business logic)
  repository/            # Spring Data JPA repositories
  entity/                # User.java, Wallet.java, Transaction.java (domain model)
  security/              # JWT/security configuration
  dto/                   # request/response payloads
  exceptions/            # custom exceptions and handlers
src/main/resources/      # application yml (H2, JPA settings)
src/test/                # unit / integration tests
```

## How it works (runtime shape)
- The app starts at `WalletServiceApiApplication` and exposes REST endpoints under `restcontroller`.
- Controllers call `service` interfaces implemented in `serviceimpl` where business rules and transactional boundaries reside.
- Services use Spring Data JPA repositories to persist `User`, `Wallet`, and `Transaction` entities to H2.
- Security is enforced by Spring Security with JWT tokens; API docs are available via springdoc.

## How to run (local)
Make sure Java 21 and a working internet connection (for Maven) are available.

```bash
# Run tests and enforce coverage gate (Jacoco configured to require >=80%)
./mvnw clean verify

# Start the application
./mvnw spring-boot:run
```

- H2 console (when enabled in yml) is typically at http://localhost:8080/h2-console
- OpenAPI/Swagger UI is typically at http://localhost:8080/swagger-ui/index.html

## Database
- The project uses H2 (runtime) for development. Check `src/main/resources/application*.yml` for the concrete datasource URL and JPA ddl settings.
- There is no Flyway/Liquibase configured; consider adding one for production schema management.

## Entities & relationships (summary)
- Entities: `User`, `Wallet`, `Transaction`.
- Typical relationship model used by this project:
  - User 1..* Wallet (one-to-many)
  - Wallet 1..* Transaction (one-to-many)
- See `src/main/java/com/wallet/entity` for exact mappings, cascade types and constraints.

## Concurrency & locking (notes & recommendations)
- The app relies on Spring Data JPA; transactional boundaries should be defined with `@Transactional` in service methods that update balances.
- I did not modify code here; please inspect `Wallet`/`Transaction` entities for an `@Version` field (optimistic locking) or repository usages of `@Lock(LockModeType.PESSIMISTIC_WRITE)`.
- Recommendations:
  - Add `@Version` to `Wallet` (optimistic locking) to avoid lost updates when concurrent requests update balance.
  - Or use repository methods with `PESSIMISTIC_WRITE` inside a `@Transactional` method to SELECT FOR UPDATE when applying changes.
  - Ensure a single transactional method updates the wallet balance and saves the transaction record together to avoid partial updates.

## Testing, code coverage, SonarQube
- Tests live under `src/test`. The pom.xml includes Spring Boot test starters for data-jpa, webmvc and validation slices.
- JaCoCo is configured in the POM with a check enforcing at least 80% instruction coverage (BUNDLE level) during the `verify` phase.
- Sonar Maven plugin is present and `sonar.projectKey` / `sonar.projectName` are set in `pom.xml`; CI or a developer needs to run `mvn sonar:sonar` with proper Sonar host/token settings to send reports.

## Security
- Spring Security + jjwt dependencies are included. Check `src/main/java/com/wallet/security` for filter, token provider, and configuration to see the exact endpoints and roles that are secured.

## Where to look next (useful commands)
- Run unit tests: `./mvnw test`
- Run the app: `./mvnw spring-boot:run`
- Run Sonar scan locally (requires Sonar server & token):
  `./mvnw sonar:sonar -Dsonar.host.url=<SONAR_URL> -Dsonar.login=<TOKEN>`

# Fundoo Notes Backend

A token-based notes application backend built with **Java 21** and **Spring Boot 4**.
Users register, log in with a JWT and manage their own notes: create, pin, archive and
trash them. On top of the core REST API the project uses JMS, RabbitMQ, Redis, caching,
Spring Batch, Excel import and AOP.

## Tech Stack

| Technology | Used for |
|---|---|
| Java 21, Spring Boot 4.1 | Application framework |
| Spring Web MVC | REST API |
| Spring Data JPA / Hibernate, MySQL | Persistence |
| Jakarta Validation | Request validation |
| JJWT, Spring Security Crypto (BCrypt) | Token generation and password hashing |
| ActiveMQ (JMS) | User registration events |
| RabbitMQ (AMQP) | Reminder events |
| Redis | Key-value store, OTPs and cache |
| Spring Cache | Caching the notes dashboard |
| Spring Batch | Trash cleanup job and Excel import job |
| Apache POI | Reading `.xlsx` files |
| Spring AOP (AspectJ) | Method logging and performance timing |
| SLF4J / Logback | Logging |
| JUnit 5, Mockito, MockMvc, H2 | Testing |

## Architecture

```
Client -> Controller -> DTO validation -> Service -> Repository -> MySQL
                                             |
                                             +-> JMS / RabbitMQ events (async)
                                             +-> Redis (OTP, cache)
Cross cutting: GlobalExceptionHandler | LoggingAspect | PerformanceAspect
```

```
src/main/java/com/fundoo/notes
├── aspect        logging and performance aspects
├── config        password encoder, JMS, RabbitMQ, Redis, cache and batch configuration
├── controller    REST controllers
├── dto
│   ├── request   request bodies with validation
│   └── response  response bodies and error format
├── entity        JPA entities (User, Note)
├── event         messages sent through JMS and RabbitMQ
├── exception     custom exceptions and global exception handler
├── repository    Spring Data JPA repositories
└── service       service interfaces
    └── impl      service implementations, publishers and consumers
```

## Feature Branches

Each feature was developed on its own branch from `develop` and merged back:

| Branch | Feature |
|---|---|
| `feature/UserEntityRepository` | User entity and repository |
| `feature/DTO` | Request / response DTOs with validation |
| `feature/ServiceController` | User service and register / login endpoints |
| `feature/TokenGenAndEncoding` | JWT tokens and BCrypt password hashing |
| `feature/ExceptionHandling` | Custom exceptions and global exception handler |
| `feature/NoteEntityAndRepository` | Note entity and repository |
| `feature/Note` | Notes API: create, list, pin, archive, trash |
| `feature/LoggingStatement` | Logging across services and controllers |
| `feature/JMSIntegration` | Registration events through ActiveMQ |
| `feature/RabbitMQ` | Reminder events through RabbitMQ |
| `feature/RedisImplementation` | Redis key-value API and OTP service |
| `feature/Cache` | Redis cache for the notes dashboard |
| `feature/BatchProcessing` | Batch job deleting old trashed notes |
| `feature/ExcelIntegration` | Importing notes from Excel with Spring Batch |
| `feature/AOP` | Logging and performance aspects |

## Running the Application

Requirements: Java 21, MySQL, Redis, RabbitMQ and ActiveMQ running on their default ports.
If ActiveMQ is not running, registration still works and the event failure is only logged.

```bash
./mvnw spring-boot:run
```

Database credentials can be passed as environment variables `DB_USERNAME` and
`DB_PASSWORD` (default `root` / `root`). The database `fundoo_notes` is created
automatically.

## REST API

Notes, reminders require the header `Authorization: Bearer <token>` from the login response.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users/register` | Register a user |
| POST | `/api/users/login` | Log in, returns a JWT |
| POST | `/api/notes` | Create a note |
| GET | `/api/notes` | Dashboard notes (pinned first, cached) |
| PATCH | `/api/notes/{noteId}/pin` | Pin / unpin a note |
| PATCH | `/api/notes/{noteId}/archive` | Archive / unarchive a note |
| PATCH | `/api/notes/{noteId}/trash` | Move to / restore from trash |
| POST | `/api/reminders` | Schedule a reminder (RabbitMQ) |
| POST | `/api/redis/save?key=&value=&ttlSeconds=` | Save a value in Redis |
| GET | `/api/redis/get/{key}` | Read a value from Redis |
| DELETE | `/api/redis/delete/{key}` | Delete a value from Redis |
| POST | `/api/redis/otp/generate?email=` | Generate a 6 digit OTP |
| POST | `/api/redis/otp/verify?email=&otp=` | Verify an OTP |
| POST | `/api/batch/run?retentionDays=` | Run the trash cleanup job |
| POST | `/api/batch/import/notes` | Import notes from an `.xlsx` file (form field `file`) |

Example:

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email": "navya@example.com", "password": "secret123"}'
```

```json
{"token": "eyJhbGciOiJIUzM4NCJ9...", "message": "Login successful"}
```

Errors always use the same format:

```json
{"status": 404, "error": "Not Found", "message": "Note not found with id: 99",
 "path": "/api/notes/99/pin", "timestamp": "2026-11-19T19:45:11.536"}
```

### Excel import format

First sheet, first row is the header:

| Title | Description | Owner Email | Pinned | Archived |
|---|---|---|---|---|
| Groceries | Milk and bread | navya@example.com | yes | no |

Rows with a missing title or an unknown owner are skipped and logged.

## Running the Tests

```bash
./mvnw test
```

Every class has its own JUnit test class. Tests use an H2 in-memory database and mocks,
so no MySQL, Redis or message broker is needed to run them.

## Author

Navya Raj

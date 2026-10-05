Paytm Beat — Seat Reservation at Scale

Backend service for concurrent seat reservation with PostgreSQL persistence, JWT authentication, idempotency, per-user booking limits, health/readiness checks, metrics, and a reproducible burst-test script.


Burst Testing

The repository contains:

burst.sh

The script is intended to reproduce the on-sale concurrency scenario against the live service.

Make it executable:

chmod +x burst.sh

Run against the deployed service:

./burst.sh https://paytm-beat.onrender.com

Overview

Paytm Beat models an event/show with a fixed set of assigned seats.

The service exposes JSON HTTP APIs to:

• Create a show with a fixed seat inventory.
• Authenticate users using JWT.
• Reserve one or more seats.
• Prevent the same seat from being reserved by multiple users under concurrent requests.
• Enforce a per-user seat limit.
• Make reservation requests idempotent using an idempotency key.
• Return the current state of a show and its seats.
• Expose health, readiness, and Prometheus metrics endpoints.
• Run a reproducible concurrency burst against the deployed service.

Money is represented as integer minor units (paise) and is never represented as a floating-point value.

Tech Stack

• Java 21
• Spring Boot
• Spring Web
• Spring Security
• Spring Data JPA / Hibernate
• PostgreSQL
• JWT authentication
• Maven
• Docker / Docker Compose
• Micrometer
• Prometheus metrics
• Resilience4j
• Kafka / Confluent Cloud
• Lombok
• Testcontainers for integration testing

Architecture

The application is a Spring Boot service backed by PostgreSQL.

                    Client
                      |
                      | HTTP/JSON
                      v
             +-------------------+
             |   Spring Boot     |
             |    Java 21        |
             +---------+---------+
                       |
          +------------+-------------+
          |                          |
          v                          v
   +-------------+            +-------------+
   | PostgreSQL  |            | Confluent   |
   |             |            | Cloud Kafka |
   +-------------+            +-------------+

PostgreSQL is the system of record for shows, seats, users, and reservations.

For local development, PostgreSQL can be started with Docker Compose. In the deployed environment, the application uses the managed PostgreSQL database configured through environment variables.

Kafka is an external Confluent Cloud dependency and is configured through environment variables rather than committed credentials.

API Endpoints

Authentication

Create User

POST /user/create
Content-Type: application/json

Example:

{
  "name": "admin",
  "password": "AdminPassword"
}

The password is stored using the application’s password hashing mechanism. Credentials must not be committed to source control.

Login

POST /user/login
Content-Type: application/json

Example:

{
  "name": "admin",
  "password": "AdminPassword"
}

The endpoint returns a JWT. Use that token as:

Authorization: Bearer <JWT>

Create Show

POST /shows
Authorization: Bearer <JWT>
Content-Type: application/json

Example:

{
  "name": "friday-night",
  "seats": [
    "A1",
    "A2",
    "A3"
  ],
  "price_paise": 25000
}

A created show contains the configured seats, initially in AVAILABLE state.

Reserve Seats

POST /shows/{id}/reserve
Authorization: Bearer <JWT>
Content-Type: application/json

Example:

{
  "seats": [
    "A1"
  ],
  "idempotency_key": "reserve-a1-request-001"
}

A successful reservation returns a reservation identifier, show identifier, token-derived user identity, seats, amount in paise, and reservation status.

Example:

{
  "amount_paise": 25000,
  "reservation_id": "ba4fbd27-8c58-40bf-ab1d-066c653d002d",
  "seats": [
    "A1"
  ],
  "show_id": 2,
  "status": "COMPLETED",
  "user_id": "alice"
}

Domain conflicts such as an already-reserved seat are represented as HTTP 409 Conflict rather than server errors.

Get Show State

GET /shows/{id}
Authorization: Bearer <JWT>

The response exposes the show’s seats and their current state.

Seat states include:

• AVAILABLE
• HELD
• CONFIRMED / application reservation state as applicable

The service is designed around the reconciliation invariant:

available + held + confirmed = total seats

Cancel / Release

If enabled by the current implementation, reservation cancellation/release is exposed through the reservation endpoint implemented by the service.

The cancellation operation must only be permitted for the owning user and must never make an already-reserved seat available again after another valid reservation has taken ownership.

Running Locally

Prerequisites

Install:

• Java 21
• Maven 3.9+
• Docker
• Docker Compose
• PostgreSQL, if running without Docker
• curl
• jq for the burst script

Verify:

java -version
mvn -version
docker --version
docker compose version

Java 21 is required by the Maven build.

Local configuration

Do not commit production credentials.

The application reads database and Kafka configuration from environment variables. For local development, defaults may be supplied for the local PostgreSQL instance.

Example database variables:

export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres

Kafka credentials should be supplied through environment variables or an untracked .env file.

Running with Docker

The repository contains a Dockerfile using Java 21 and a Docker Compose configuration for local development.

Build the application:

docker build -t paytm-beat .

Run the application:

docker run -p 8080:8080   -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/postgres   -e SPRING_DATASOURCE_USERNAME=postgres   -e SPRING_DATASOURCE_PASSWORD=postgres   paytm-beat

Or use Docker Compose:

docker compose up --build

The application is exposed on:

http://localhost:8080

Docker Compose starts PostgreSQL locally and provides the database connection to the application.

Environment Variables

Production secrets and environment-specific configuration are supplied through environment variables.

Database

SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD

Kafka

KAFKA_BOOTSTRAP_SERVERS
KAFKA_SECURITY_PROTOCOL
KAFKA_SASL_MECHANISM
KAFKA_SASL_JAAS_CONFIG

Application Port

PORT

The application defaults to port 8080 when PORT is not supplied.

Never commit:

• Database passwords
• Kafka API keys
• Kafka API secrets
• JWT secrets
• Production credentials

Live Deployment

The service is deployed publicly on Render.

Live URL:

https://paytm-beat.onrender.com

The application is containerized and deployed using the repository’s Dockerfile.

The production service connects to managed PostgreSQL and the configured Confluent Cloud Kafka cluster using environment variables.

Health & Readiness

Liveness / Health

GET /actuator/health

Expected healthy response:

{
  "status": "UP"
}

Readiness

GET /actuator/health/readiness

The readiness group includes the application readiness state and database health.

The intention is that the service is considered ready only when its required database dependency is reachable.

Local verification

curl -i http://localhost:8080/actuator/health

curl -i http://localhost:8080/actuator/health/readiness

Production verification

curl -i https://paytm-beat.onrender.com/actuator/health

curl -i https://paytm-beat.onrender.com/actuator/health/readiness

Metrics

The application exposes Micrometer/Prometheus metrics.

Endpoint:

GET /actuator/prometheus

The metrics endpoint can be used to observe the application while running reservation bursts.

The reservation flow should expose/track the following business outcomes where implemented:

• Reservations confirmed
• Reservations declined because the seat was already taken
• Reservations declined because of the per-user limit
• Idempotent reservation replays
• Available seats

Application and JVM metrics are also available through the actuator configuration.

Metrics should be compared with the API’s final show state after a burst to verify that observed reservation outcomes reconcile with persisted state.

Burst Testing

The repository contains:

burst.sh

The script is intended to reproduce the on-sale concurrency scenario against the live service.

Make it executable:

chmod +x burst.sh

Run against the deployed service:

./burst.sh https://paytm-beat.onrender.com

The script creates a fresh test show and users, authenticates them, then performs a concurrent hot-seat reservation test.

It reports:

• Total requests
• Successful reservations (201)
• Domain conflicts (409)
• 5xx responses
• Other responses
• Hot-seat winner count
• Final seat reconciliation

For a larger test:

USERS=500 ./burst.sh https://paytm-beat.onrender.com

The concurrency level should be increased gradually when using a free-tier deployment to avoid platform-level resource limits masking application behaviour.

Expected hot-seat result

For many concurrent users targeting the same unique seat:

Exactly one request -> successful reservation
Remaining requests -> 409 seat-taken/domain decline
5xx responses       -> 0

A representative result is:

========================================
 HOT SEAT STORM
========================================

Total requests : 100
201 confirmed  : 1
409 declined   : 99
5xx errors     : 0

HOT SEAT RESULT: PASS
5xx RESULT: PASS

The script also fetches the final show state and checks:

available + held + confirmed == total seats

Concurrency & Correctness

The central correctness requirement is that reservation decisions must be made atomically at the persistence layer.

A read-then-write flow such as:

1. SELECT seat
2. Check AVAILABLE
3. UPDATE seat

is unsafe when two requests execute concurrently.

The implementation therefore relies on database-backed state transitions and transactional behaviour so that concurrent requests cannot both successfully acquire the same seat.

The correctness properties being tested are:

No Double-Sell

For a hot seat such as A1, concurrent reservation attempts must result in at most one successful reservation.

All competing requests must receive a domain-level decline such as:

409 Conflict

rather than a 500 Internal Server Error.

Per-User Limit

A user cannot reserve more than the configured per-user limit for a show.

The default requirement is:

4 seats per user per show

The limit must be enforced transactionally so that multiple concurrent requests from the same user cannot bypass the limit.

Multi-Seat Requests

Multi-seat reservations must have explicitly defined semantics.

The implementation should preserve those semantics under concurrent requests. In particular, a partial request must not leave the database in an inconsistent state or accidentally allocate the same seat to another user.

Idempotency

Reservation requests contain an idempotency key.

The intended behaviour is:

Same key + same request
        ↓
Return the original reservation

while:

Same key + different request
        ↓
409 Conflict

The idempotency record must be persisted with a uniqueness guarantee so that concurrent retries cannot create multiple reservations.

Idempotency prevents client retries caused by timeouts or network failures from creating a second reservation or a second charge.

Per-User Limit

The reservation service derives the user identity from the authenticated JWT.

The request body is not trusted to select another user’s identity.

This prevents a client from attempting to reserve or cancel resources on behalf of another user by adding a spoofed user identifier to the request.

Concurrent requests from the same authenticated user are subject to the same per-show booking limit.

Observability

The service provides three primary observability mechanisms:

Health

/actuator/health

Used to determine whether the application is running.

Readiness

/actuator/health/readiness

Used to determine whether the service is ready to serve traffic, including database reachability.

Prometheus Metrics

/actuator/prometheus

Used to inspect application and business metrics.

Logs

Application logs are used to trace request processing and reservation outcomes.

For production operation, the important events to monitor include:

• Reservation successes
• Seat-taken conflicts
• Per-user-limit conflicts
• Idempotent replays
• Database connectivity failures
• Unexpected 5xx responses
• Reconciliation mismatches
• Elevated reservation latency

A zero-5xx expectation during the concurrency burst is especially important because expected business contention should produce domain-level 4xx responses rather than application failures.

AI Usage

AI tools were used as an engineering assistant during development.

Usage included:

• Reviewing API and database design options.
• Identifying concurrency and race-condition risks.
• Reviewing Docker and deployment configuration.
• Structuring the burst-testing script.
• Reviewing edge cases around idempotency and concurrent reservations.
• Debugging Java/Maven and deployment configuration issues.
• Reviewing health, readiness, metrics, and environment-variable configuration.
• Helping document the implementation and trade-offs.

AI suggestions were reviewed and adapted to the actual implementation. The final code, architecture, database behaviour, testing, deployment configuration, and design decisions remain the responsibility of the author.

The implementation is intended to be explainable in an interview, including the transaction boundaries, database constraints, idempotency mechanism, concurrency behaviour, and trade-offs.

Assumptions & Trade-offs

PostgreSQL as the System of Record

PostgreSQL is used as the primary source of truth because seat allocation requires strong consistency and transactional guarantees.

Integer Money Values

All monetary amounts are represented as integer paise:

25000 = ₹250.00

No floating-point representation is used for money.

Database-First Consistency

The application prioritizes correctness of seat ownership over accepting every request. Under contention, losing requests are expected to receive 409 Conflict.

Availability vs Consistency

For a seat reservation system, accepting conflicting reservations is worse than declining a request. Therefore, the reservation decision favours strong consistency at the database layer.

If the database is unavailable, readiness should fail rather than allowing the application to make reservation decisions without the system of record.

Cold Starts

The deployed service is expected to tolerate a platform cold start and expose a healthy actuator endpoint after startup.

Free-Tier Constraints

The public deployment uses a free-tier hosting environment. Large bursts can be affected by platform CPU, memory, connection, or rate limits. The burst script therefore allows the concurrency level to be adjusted while still exercising the application’s correctness properties.

Clean Checkout

A clean checkout should contain everything required to build and run the service:

Paytm-Beat/
├── src/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── burst.sh
├── README.md
└── WRITEUP.md

Build with Maven:

mvn clean package

or build the container:

docker build -t paytm-beat .

Run with Docker Compose:

docker compose up --build

Submission

Repository

The public Git repository contains:

• Application source code
• Full commit history
• Dockerfile
• Docker Compose configuration
• Burst test script
• README
• WRITEUP

Live URL

https://paytm-beat.onrender.com

Key URLs

Health:

https://paytm-beat.onrender.com/actuator/health

Readiness:

https://paytm-beat.onrender.com/actuator/health/readiness

Prometheus:

https://paytm-beat.onrender.com/actuator/prometheus
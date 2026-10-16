# GigScore

A full-stack performance tracking platform for gig workers and freelancers. GigScore aggregates work data from multiple platforms, calculates a unified performance score, tracks growth trends, and provides AI-powered improvement suggestions.

---

## Overview

| Layer | Technology |
|-------|-----------|
| Frontend | React 18, Vite, Tailwind CSS, Recharts, Axios, React Router |
| Backend | Spring Boot 4, Java 21 |
| Security | Spring Security, JWT (jjwt 0.12.7) |
| Database | MySQL 8, Spring Data JPA, Hibernate |
| AI | Google Gemini API (optional) |
| Docs | Springdoc OpenAPI (Swagger UI) |
| Build | Maven, npm |

### Architecture

```text
┌─────────────────────────────────────────────────────┐
│                    Frontend                          │
│         React 18 + Vite + Tailwind CSS              │
│              http://localhost:5173                   │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP (REST)
┌──────────────────────▼──────────────────────────────┐
│                   Backend API                        │
│           Spring Boot 4  ·  Java 21                  │
│              http://localhost:8080                   │
│                                                     │
│  ┌──────────┐  ┌──────────┐  ┌───────────────────┐  │
│  │Controllers│→ │ Services │→ │ Repositories (JPA)│  │
│  └──────────┘  └──────────┘  └────────┬──────────┘  │
│       │              │                 │             │
│  ┌────▼────┐   ┌─────▼─────┐   ┌──────▼──────┐     │
│  │  DTOs   │   │   Score   │   │   MySQL 8   │     │
│  └─────────┘   │  Engine   │   └─────────────┘     │
│                └───────────┘                        │
│                                                     │
│  ┌─────────────────────────────────────────────┐    │
│  │  Security: JWT Filter → SecurityConfig      │    │
│  └─────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘
```

### Features

- **JWT Authentication** — Register/login with stateless tokens, BCrypt passwords, 1-hour expiry
- **Unified Dashboard** — Earnings, jobs, average rating, and active days in one view
- **Multi-Platform Tracking** — Categorized event logging across supported platforms (Uber, Upwork, Fiverr, Swiggy, Zomato, Rapido, DoorDash, TaskRabbit, Lyft, Freelancer)
- **Score Computation** — Weighted exponential formula producing a 0-100 score, computed server-side with a versioned config
- **Score History** — Persisted per-user snapshot log; chart renders real trajectory
- **Score Explanation** — Per-component point breakdown, band badge (EARLY/GROWING/STRONG), and two weakest-lever improvement suggestions
- **Data Quality Profiling** — Confidence rating per user based on completeness, consistency, recency, and diversity
- **Activity Feed** — Paged, newest-first activity list (max 50 per page)
- **AI Chat Assistant** — Gemini-powered guidance on improving your score (gracefully unavailable without API key)
- **Lender Portal** — Role-based access (LENDER) to view all workers ranked by score with full explanations
- **Rate Limiting** — Sliding-window in-memory limiter: login 10/min, chat 20/min per IP
- **Request Logging** — Completion-level access logs with status and duration
- **Actuator** — `/actuator/health` endpoint, public for uptime checks
- **Interactive API Docs** — Swagger UI with in-browser JWT authorization (dev environment or when enabled)
- **Light/Dark Theme** — Persistent preference on the frontend

### Project Structure

```text
GigScore/
├── backend/
│   └── gigscore/
│       ├── src/main/java/com/org/gigscore/
│       │   ├── config/         # Security, CORS, JWT, OpenAPI, rate limiter, request logging
│       │   ├── controller/     # REST controllers
│       │   ├── dto/            # Request and response DTOs
│       │   ├── entity/         # JPA entities
│       │   ├── exception/      # Custom exceptions and global handler
│       │   ├── repository/     # Spring Data JPA repositories
│       │   ├── security/       # CurrentUserResolver
│       │   ├── service/        # Business logic
│       │   └── util/           # Money, Platform enum
│       └── src/main/resources/
│           └── application.properties
│
├── frontend/
│   ├── src/
│   │   ├── components/         # Reusable React components
│   │   ├── pages/              # Application pages
│   │   ├── services/           # Axios HTTP client, service modules
│   │   ├── context/            # Auth and theme context
│   │   └── utils/              # Helper functions and tests
│   ├── nginx.conf
│   └── package.json
│
├── docker-compose.yml
└── README.md
```

---

## API Reference

All protected endpoints require a JWT Bearer token in the `Authorization` header. Tokens are returned by register/login endpoints and expire after 1 hour.

### Authentication

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/users` | Register a new user (password min 6 chars) | No |
| POST | `/api/v1/users/login` | Login and receive a JWT | No |

### Dashboard & Data

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/users/{userId}` | User dashboard with score and gig summaries | Yes |
| POST | `/api/v1/gigs` | Log a new gig event (`platform` must come from `GET /api/v1/platforms`) | Yes |
| GET | `/api/v1/activity/{userId}` | Paged activity entries, newest first (`?page=0&size=5`, max 50) | Yes |
| GET | `/api/v1/platforms` | List of allowed platform values | Yes |

### Score

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/score/{userId}` | Calculate and return gig score (0-100), band, explanation, and suggestions | Yes |
| GET | `/api/v1/score/{userId}/history` | Persisted score history (snapshot recorded on each score change) | Yes |
| GET | `/api/v1/score/config` | Score formula config (weights, exponents, targets) | Yes |

### AI Chat

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/chat/ask` | Send messages, receive AI guidance | Yes |

### Lender (Role-Based)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/lender/workers` | All workers ranked by score with data-quality profiles | Yes (LENDER) |
| GET | `/api/v1/lender/workers/{userId}` | Single worker's full credit view | Yes (LENDER) |

Users register as `WORKER` by default. The `LENDER` role is seeded via `gigscore.seed-lender.email` / `gigscore.seed-lender.password` in `application.properties`. Lender endpoints return 403 for non-lender tokens.

### Monitoring

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/actuator/health` | Application health check | No |

### Rate Limiting

- `POST /api/v1/users/login`: 10 requests/minute per IP
- `POST /api/v1/chat/ask`: 20 requests/minute per IP
- Exceeded → HTTP 429 with `Retry-After` header

---

## Score Formula

The score (0-100) combines four weighted components with exponential normalization:

| Component | Weight | Exponent | Target |
|-----------|--------|----------|--------|
| Earnings | 35% | 0.70 | $5,000 |
| Jobs Completed | 25% | 0.65 | 100 |
| Average Rating | 30% | 1.40 | 5.0 |
| Active Days | 10% | 1.00 | 30 |

```text
Score =
  (E/5000)^0.70 × 0.35
+ (J/100)^0.65 × 0.25
+ (R/5)^1.40 × 0.30
+ (D/30)^1.00 × 0.10
```

- Exponents < 1 (earnings, jobs) reward early progress; > 1 (rating) applies stricter scaling.
- Each component is normalized to 0-1, raised to its exponent, multiplied by its weight, summed, and scaled to 0-100.
- Score is capped at 100; band thresholds: EARLY (< 40), GROWING (40-69), STRONG (≥ 70).

### Monetary Precision

All money fields (`GigData.totalEarnings`, `Activity.amount`) are `BigDecimal` persisted as `DECIMAL(12,2)`. The canonical policy (`Money` in `com.org.gigscore.util`) is scale 2 with `HALF_UP` rounding, applied on every write and aggregation; `null` reads as zero.

---

## Local Setup

### Requirements

- Java 21+
- Maven 3.8+
- Node.js 18+
- MySQL 8+ (or use H2 for local dev — see below)

### 1. Create the database

```sql
CREATE DATABASE gigscore;
```

### 2. Configure environment variables

Copy `.env.example` to `.env` in the repository root and configure your values:

```bash
cp .env.example .env
```

| Variable | Description | Requirement | Example |
|----------|-------------|-------------|---------|
| `DB_USERNAME` | MySQL username | Optional (defaults to `root`) | `root` |
| `DB_PASSWORD` | MySQL password | **Required** (no weak default) | `<YOUR_SECURE_PASSWORD>` |
| `JWT_SECRET` | Secret for JWT signing (HMAC-SHA256) | **Required** (minimum 32 characters) | `<32+_CHAR_RANDOM_SECRET>` |
| `CORS_ORIGINS` | Comma-separated frontend origins | Optional (defaults to localhost) | `http://localhost:5173,http://127.0.0.1:5173` |
| `DDL_AUTO` | Hibernate schema mode | Optional (defaults to `update`) | `update` |
| `GEMINI_API_KEY` | Google Gemini API key | Optional (AI chat only) | `AIza...` |
| `GEMINI_MODEL` | Gemini model identifier | Optional (defaults to `gemini-2.5-flash`) | `gemini-2.5-flash` |
| `DEMO_LENDER_EMAIL` | Email for a seeded `LENDER` user | Optional (blank = skip) | `demo_lender@example.com` |
| `DEMO_LENDER_PASSWORD` | Password for seeded lender account | Optional (blank = skip) | `<STRONG_PASSWORD>` |
| `SWAGGER_ENABLED` | Explicitly enable Swagger UI and API docs | Optional (defaults to `false` in production) | `false` |

> **Security Note:** `DB_PASSWORD` and `JWT_SECRET` have no hardcoded insecure defaults. `JwtUtil` will fail fast at application startup if `JWT_SECRET` is missing or shorter than 32 bytes (256 bits). Never commit `.env` or any real secrets to Git.

### 3. Run the backend

```bash
cd backend/gigscore
mvn spring-boot:run
```

Backend starts at `http://localhost:8080`.

### 4. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts at `http://localhost:5173`.

### API Documentation

Swagger UI: `http://localhost:8080/swagger-ui.html`
OpenAPI spec: `http://localhost:8080/v3/api-docs`

> **Note:** By default, Swagger UI and OpenAPI documentation are enabled only in `dev`/`local` profiles or when `SWAGGER_ENABLED=true` is set. In production environments, they are disabled to avoid exposing API structure.

To test authenticated endpoints in development, click **Authorize** in Swagger UI and paste a JWT token.

---

## Testing

```bash
# Backend (H2 in-memory database, test profile)
cd backend/gigscore
mvn test

# Frontend (Vitest — score formula + API contract tests)
cd frontend
npm test
```

Backend tests cover: score formula boundaries, rating clamps, cross-user RBAC, short-password rejection, rate limiter behavior, platform validation, paged activity shape, and health endpoint accessibility.

---

## Docker

```bash
# 1. Create .env from template and configure DB_PASSWORD and JWT_SECRET
cp .env.example .env

# 2. Build and start containers
docker compose up --build
```

Requires: `DB_PASSWORD` and `JWT_SECRET` configured in `.env` (no weak defaults).

Services: `mysql` (port 3306), `backend` (port 8080), `frontend` (port 5173 → nginx port 80).

---

## Database Migrations (Flyway)

Database schema versioning is managed via **Flyway** with strict Hibernate validation (`spring.jpa.hibernate.ddl-auto=validate`). Hibernate's dangerous `ddl-auto=update` is disabled in production to guarantee predictable and reproducible schema evolution.

### Migration History
- **`V1__baseline_schema.sql`** — Portable baseline DDL defining initial tables (`user`, `gig_data`, `gig_score`, `score_history`, `activity`) with portable ANSI `TIMESTAMP(6)` datatypes.
- **`V2__unique_gig_data.sql`** — Enforces database-level constraint `UNIQUE(user_id, platform)` on `gig_data` and adds optimistic locking column `version BIGINT NOT NULL DEFAULT 0`.
- **`V3__indexes.sql`** — Adds composite B-tree indexes:
  - `idx_activity_user_timestamp` on `activity(user_id, timestamp)` (eliminates filesorts on recent activity lookups).
  - `idx_score_history_user_recorded` on `score_history(user_id, recorded_at)`.

### Running Migrations in Production
Flyway executes automatically during application startup:
```bash
# Migrations run against target datasource on startup
java -jar gigscore.jar
```
To run migrations manually via MySQL CLI prior to starting the application:
```sql
USE gigscore;

-- 1. Optimistic locking & Unique constraint
ALTER TABLE gig_data ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE gig_data ADD CONSTRAINT uk_gig_data_user_platform UNIQUE (user_id, platform);

-- 2. Composite indexes for high-frequency queries
CREATE INDEX idx_activity_user_timestamp ON activity (user_id, timestamp);
CREATE INDEX idx_score_history_user_recorded ON score_history (user_id, recorded_at);
```

---

## Data Integrity & Performance Hardening

1. **Read-Only Score Queries**: `GET /api/v1/score/{userId}` is strictly read-only (`@Transactional(readOnly = true)`). Cache misses query persisted score records or evaluate in-memory without inserting duplicate `score_history` rows or triggering table locks.
2. **Concurrent Gigs & Optimistic Locking**: Simultaneous gig submissions are protected at the database engine level via `UNIQUE(user_id, platform)` and `@Version`. Collisions are handled via a bounded 5-attempt retry loop using `TransactionTemplate`, ensuring every retry executes inside a fresh, clean database transaction.
3. **Lazy Associations**: All entity-to-user associations (`@ManyToOne`) use `FetchType.LAZY` with `spring.jpa.open-in-view=false`. Unnecessary eager joins on the `user` table are completely eliminated.
4. **N+1 Query Elimination & Pagination**: The lender directory endpoint (`/api/v1/lender/workers`) is paginated (`PageRequest`, default 20, max cap 50) and powered by a single JPQL projection with scalar subqueries. Total query count is strictly bounded to $\le 3$ statements ($O(1)$) regardless of page size.
5. **Exact Financial Precision**: All earnings metrics are represented as `BigDecimal` with exact scale (2 decimal places) across entities, services, and API DTOs to prevent IEEE-754 binary floating-point drift.
6. **Push Computations to Database**: Active days calculations execute as `COUNT(DISTINCT CAST(a.timestamp AS date))` directly inside the database engine rather than instantiating thousands of JPA entities in JVM memory.

---

## Known Limitations

- **In-Memory Cache Provider**: Default Spring caching uses an in-memory concurrent map provider. For multi-node cluster deployments, configure a distributed cache like Redis.
- **In-Memory Rate Limiting**: The sliding-window rate limiter tracks IPs in JVM memory; multi-instance deployments require a centralized store (e.g., Redis rate limiting).
- **No Email Verification / Password Reset Flow**: User account registration is immediate without email delivery integration.
- **AI Chat Session Context**: Gemini AI chat assistant is stateless and evaluates suggestions per request without storing long-term chat history.

---

## Future Improvements

### Security
- Add HTTPS / TLS termination via reverse proxy
- Add OAuth2 social login (Google, GitHub)
- Add multi-factor authentication (MFA)

### Performance & Scalability
- Redis distributed cache for multi-instance deployments
- Asynchronous score calculation via background message broker (RabbitMQ/Kafka)
- Database read replicas for lender reporting queries

---

## Production Scaling

```text
Current:     React → Spring Boot → MySQL

Future:      React CDN
                 │
           Load Balancer
                 │
          Multiple Backend Instances
           │           │
     Redis Cache    Message Queue
           │
     Database Replicas
```

---

## License

Currently unlicensed. Add a `LICENSE` file before open-source distribution.

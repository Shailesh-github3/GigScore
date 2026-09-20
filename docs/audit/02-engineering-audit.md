# Phase 2 — Engineering Audit

Date: 2026-09-20  
Auditor: Senior Backend Engineer & Technical Interviewer  
Target Repository: `D:\Java\Spring_boot\GigScore`

---

## 1. Architecture & Layering

```text
[HTTP Clients / React SPA]
        │  /api/v1/...
        ▼
[Security Filters] (RequestLoggingFilter → RateLimitFilter → JwtAuthFilter)
        │
        ▼
[Controllers] (UserController, GigDataController, GigScoreController, ...)
        │
        ▼
[Services] (UserService, GigDataService, GigScoreService, UserMetricsService, LenderService, ...)
        │
        ▼
[Repositories] (UserRepository, GigDataRepository, GigScoreRepository, ActivityRepository, ScoreHistoryRepository)
        │
        ▼
[Database] (MySQL 8 / In-Memory H2)
```

### 1.1 Layer Boundaries & Coupling
* **Controller-to-Repository Leakage:** In `GigScoreController.java` (lines 29–36, 59–72), `UserRepository` and `ScoreHistoryRepository` are injected directly into the REST controller, bypassing the service layer entirely to fetch user entities and map score history.
  * *Interviewer Critique:* Controllers must orchestrate HTTP concerns (DTO mapping, status codes, query param extraction). Bypassing services couples presentation directly to database queries and impairs transactional consistency.
* **Service-Layer Coupling:** `GigDataService` has 5 injected dependencies (`GigDataRepository`, `UserRepository`, `GigScoreService`, `UserMetricsService`, `ActivityService`). When a gig is added, `GigDataService` directly calls `activityService.recordGigAdded()`, `scoreService.calculateAndPersistScore()`, and `getUserDashboard()`. This creates a tight cascade of side effects within a single transactional boundary (`@Transactional`).
* **Testability:** High testability overall. All classes use constructor injection without field `@Autowired`, allowing dependencies to be cleanly mocked with Mockito in unit tests (`GigScoreServiceTest`, `UserServiceTest`, `GigDataServiceTest`).

---

## 2. API Design & REST Contracts

### 2.1 Endpoint Evaluation
| Endpoint | Method | Expected HTTP Behavior | Actual Code Behavior | REST Compliance |
|---|---|---|---|---|
| `/api/v1/users` | `POST` | `201 Created` with `Location` header | Returns `200 OK` with `LoginResponseDTO` | 🟡 Non-standard status code for resource creation |
| `/api/v1/users/login` | `POST` | `200 OK` with session/token DTO | Returns `200 OK` with `LoginResponseDTO` | ✅ Correct |
| `/api/v1/users/{userId}` | `GET` | `200 OK` read-only idempotent query | Returns `UserDashboardResponse` (read-only) | ✅ Correct (Protected by user ID verification) |
| `/api/v1/gigs` | `POST` | `201 Created` with created gig resource | Returns entire `UserDashboardResponse` | 🟡 Anti-pattern: Mutating a sub-resource returns root aggregate |
| `/api/v1/score/{userId}` | `GET` | `200 OK` idempotent safe read | **Mutates DB on cache miss:** writes to `gig_score` and inserts `score_history` row | 🔴 **CRITICAL REST VIOLATION:** `GET` request must be safe and idempotent |
| `/api/v1/score/{userId}/history`| `GET` | `200 OK` list of history entries | Returns `List<ScoreHistoryEntry>` | ✅ Correct |
| `/api/v1/activity/{userId}` | `GET` | `200 OK` paged activity feed | Returns `PagedActivities` (`page`, `size`, `totalElements`, `totalPages`) | ✅ Correct (Spring Pageable mapping) |
| `/api/v1/lender/workers` | `GET` | `200 OK` paged list | Returns full `List<LenderWorkerSummary>` | 🔴 Missing pagination (full table dump) |

### 2.2 DTO Leakage & Error Standardization
* **DTO Isolation:** No JPA `@Entity` classes are exposed in controller method signatures or responses. All inputs and outputs use dedicated DTOs (`UserDashboardResponse`, `ScoreResponse`, `ActivityResponse`, `LenderWorkerSummary`, `LenderWorkerDetail`).
* **Consistent Error Contract:** `GlobalExceptionHandler.java` and `ErrorResponseWriter.java` format all client and filter errors into a uniform JSON structure:
  ```json
  {
    "status": 400,
    "message": "Validation failed",
    "timestamp": "2026-09-20T12:00:00Z"
  }
  ```
  `Exception.class` catch-all suppresses internal stack traces in production (logging via SLF4J and returning generic HTTP 500).

---

## 3. Persistence & Database Design

### 3.1 Entity Relationship & Schema Deficiencies
* **Eager Loading Defaults:** In `GigData.java` (line 22), `GigScore.java` (line 27), `ScoreHistory.java` (line 22), and `Activity.java` (line 23), all `@ManyToOne` associations with `User` omit the `fetch` attribute. In JPA, `@ManyToOne` defaults to `FetchType.EAGER`. Hibernate automatically executes joins or separate selects for `User` on every entity fetch.
  * *Fix:* Add `fetch = FetchType.LAZY` to all `@ManyToOne` annotations.
* **Missing Unique Constraints on Aggregates:** `GigData` has no database-level unique constraint on `(user_id, platform)`. To compensate, `GigDataService.java` (lines 81–122) implements custom deduplication in Java:
  ```java
  List<GigData> rows = new ArrayList<>(gigDataRepository.findAllByUserAndPlatform(user, platform));
  if (rows.size() > 1) {
      // merges totals and deletes extra rows in memory
  }
  ```
  * *Consequence:* Under concurrent gig submissions for the same platform, race conditions will insert duplicate rows. The schema must enforce `UNIQUE (user_id, platform)`.
* **Missing Indexes on High-Frequency Query Paths:**
  * `Activity`: Queried by `findByUserOrderByTimestampDesc(User user, Pageable pageable)`. Lacks composite index on `(user_id, timestamp)`.
  * `ScoreHistory`: Queried by `findByUserOrderByRecordedAtAsc(User user)`. Lacks composite index on `(user_id, recorded_at)`.
* **Missing Audit Fields:** `User` has no `createdAt` or `updatedAt` timestamps.

### 3.2 Migration Strategy (Flyway vs `ddl-auto`)
* **Finding:** **ZERO Flyway or Liquibase migrations exist.**
* `application.properties` (line 7) uses `spring.jpa.hibernate.ddl-auto=${DDL_AUTO:update}`.
* *Fintech Reviewer Verdict:* Relying on Hibernate `ddl-auto=update` is completely unacceptable in production. It cannot rename columns, cannot create custom partial indexes, cannot perform non-destructive schema evolutions, and risks silent data corruption. Introducing Flyway with versioned `.sql` scripts (`V1__init_schema.sql`) is a mandatory P0 requirement.

---

## 4. Validation & Exception Handling

* **Jakarta Bean Validation:** Comprehensive validation annotations on request boundaries:
  * `CreateUserRequest`: `@NotBlank`, `@Email`, `@Size(min = 6)`
  * `GigEventRequest`: `@NotBlank`, `@DecimalMin(value = "0.01")`, `@DecimalMin("0.0")`, `@DecimalMax("5.0")`
  * Validated automatically via `@Valid` in controllers.
* **Business Validation:** Platform strings are validated against canonical enum values in `Platform.normalize()` (`com.org.gigscore.util.Platform`). Unsupported platform names cleanly throw `BadRequestException("Unknown platform '...'")`.
* **Security Validation:** `UserController`, `ActivityController`, and `GigScoreController` check `currentUserResolver.getCurrentUserId().equals(userId)` before querying. Mismatches throw `UnauthorizedAccessException("Access denied.")` yielding HTTP 403.

---

## 5. Frontend Architecture & React Engineering

### 5.1 Genuine Engineering vs UI Assembly Evaluation
* **Verdict:** **Hybrid — genuine structural foundation with legacy visual shell.**
  * *Genuine Engineering:* Centralized Axios client (`httpClient.js`) with request/response interceptors; `AbortController` integration in `useEffect` hooks for clean unmount cancellation; protected route guards based on role and auth status; separate service modules (`userService`, `scoreService`, `gigService`, `activityService`).
  * *UI Assembly / Deficiencies:* Prop drilling for `userId` across components; auth state stored in `localStorage`; styling relies on custom CSS variables and older Tailwind classes that do not match the target Stitch AI design tokens (`DESIGN.md`).

### 5.2 Frontend Risk & Health Audit
* **Authentication Vulnerability:** `localStorage.setItem("gigscoreToken", token)` leaves JWTs accessible to any script running on the page. In the presence of an XSS vulnerability, tokens can be extracted.
* **Missing Pages:** `Platforms.jsx`, `Insights.jsx`, `Activity.jsx`, and `Settings.jsx` do not exist. Navigation links in `Sidebar.jsx` are hardcoded to `/dashboard`, `/add-gig`, `/score`, `/lenders`.
* **Bundle Bloat:** Single JavaScript production bundle is **623.46 kB** minified, exceeding Vite's 500 kB chunk threshold due to un-chunked Recharts imports.

---

## 6. Financial Data Handling & Time-Zone Precision

### 6.1 Monetary Representation
* **Precision Policy:** All monetary storage in backend entities (`GigData.totalEarnings`, `Activity.amount`) strictly uses `BigDecimal` with SQL type `DECIMAL(12,2)`.
* **Arithmetic Correctness:** Handled through `Money.java` using `scale = 2` and `RoundingMode.HALF_UP`.
* **DTO Precision Leak:** In `LenderWorkerSummary.java` (line 14) and `LenderWorkerDetail.java` (line 14), earnings are typed as `Double`:
  ```java
  .totalEarnings(metrics.totalEarnings().doubleValue())
  ```
  * *Correction:* `LenderWorkerSummary.totalEarnings` must remain `BigDecimal` across the entire DTO boundary.

### 6.2 Currency Handling
* **Critical Missing Dimension:** **No currency code is stored.** Gigs for `Uber`, `Upwork`, `Swiggy`, and `Zomato` are aggregated together into a single numeric total without an ISO-4217 currency code (`USD`, `INR`, `GBP`). The frontend simply hardcodes `$`.

### 6.3 Date/Time & Time-Zone Defect
* **Entities use `LocalDateTime`:**
  * `Activity.timestamp`: `LocalDateTime`
  * `ScoreHistory.recordedAt`: `LocalDateTime`
  * `GigScore.createdAt`, `updatedAt`: `LocalDateTime`
* **Consequence:** `LocalDateTime` stores no offset or time zone. When the backend calculates "active days" (`activity.getTimestamp().toLocalDate()`) or today's earnings, it evaluates against server local time. If the server is in UTC or IST (+05:30) and the gig worker operates in US CST (-06:00), transactions recorded at 11:30 PM CST are grouped into the next calendar day.
* *Remedy:* Refactor all audit and transaction timestamps to `Instant` (or `OffsetDateTime` in UTC).

---

## 7. Testing Audit & Coverage Analysis

### 7.1 Current Test Breakdown (39 Tests Total)
* **Scoring Unit Tests (`GigScoreServiceTest` — 13 tests):**
  * Verifies formula outputs at targets (earnings = 5000 gives 35 pts, jobs = 100 gives 25 pts, rating = 5.0 gives 30 pts, days = 30 gives 10 pts, sum = 100.0).
  * Verifies clamping of ratings > 5.0 and negative values.
* **Service Unit Tests (`UserServiceTest`, `GigDataServiceTest`, `GeminiChatServiceTest` — 11 tests):**
  * Verifies BCrypt encoding, email conflict handling, platform deduplication merge logic, Gemini API failure fallback.
* **Integration Tests (MockMvc + In-Memory H2 — 15 tests):**
  * `UserControllerIntegrationTest`: 4 tests (register, login, IDOR prevention).
  * `GigDataControllerIntegrationTest`: 4 tests (amount/rating validation, platform checks).
  * `RateLimitIntegrationTest`: 2 tests (verifies HTTP 429 when exceeding 10 logins/min).
  * `LenderControllerIntegrationTest`: 3 tests (worker 403 forbidden, lender 200 ok).
  * `ActivityPaginationIntegrationTest`: 2 tests (verifies page size and order).

### 7.2 What Critical Business Logic Is NOT Protected?
1. **Concurrency / Race Conditions:** No test verifying simultaneous gig additions for the same user and platform.
2. **GET Idempotency on Score:** No test verifying whether multiple `GET /api/v1/score/{userId}` calls create duplicate rows in `score_history`.
3. **Time-Zone Rollover:** No test verifying daily earnings calculations across UTC midnight boundaries.
4. **Lender Consent Isolation:** No test verifying that worker A can deny lender B access to their financial data.
5. **Frontend Integration Tests:** Vitest only tests `scoreFormula.js` and `apiContract.test.js`. No component lifecycle or form submission tests exist.

---

## 8. Real Performance Bottlenecks

1. **Lender Portal Query Storm ($O(N)$ Multi-Table Scan):**
   * Location: `LenderService.java#getAllWorkers()`
   * Query path: `userRepository.findAll()` $\rightarrow$ for each user calls `calculateAggregates` (`findByUser` on `gig_data` + `findByUser` on `activity`) $\rightarrow$ for each user calls `getScoreForUser` (checking cache or writing to `gig_score` and `score_history`).
   * *Impact:* For 1,000 workers, loading the lender page executes over 2,000 database queries in a single request.
   * *Remedy:* Replace full entity hydration with a single JPQL/SQL projection with `JOIN` and pagination (`Pageable`).
2. **In-Memory Active Days Calculation:**
   * Location: `UserMetricsService.java#countDistinctActiveDays()`
   * Code: `activityRepository.findByUser(user).stream().map(a -> a.getTimestamp().toLocalDate()).collect(Collectors.toSet()).size()`
   * *Impact:* Fetches all historical activity entities for a user into the JVM heap just to count dates. For an active worker with 1,000 deliveries, 1,000 entities are hydrated on every score calculation.
   * *Remedy:* Replace with database aggregation: `SELECT COUNT(DISTINCT CAST(a.timestamp AS date)) FROM Activity a WHERE a.user = :user`.

---

## 9. DevOps, Observability & Environment Configuration

* **Docker & Compose:**
  * Clean multi-stage build in `backend/gigscore/Dockerfile` (Maven 3.9 build $\rightarrow$ `eclipse-temurin:21-jre` runtime).
  * Multi-stage build in `frontend/Dockerfile` (Node 20 build $\rightarrow$ `nginx:alpine` static hosting).
  * `docker-compose.yml` configures MySQL 8 with health check (`mysqladmin ping`), passing `JWT_SECRET` and database credentials.
* **CI/CD Pipeline:**
  * `.github/workflows/ci.yml` runs on push to `main`/`master` and pull requests.
  * Correctly sets up Java 21, runs `mvn test`, sets up Node 20, runs `npm test`, and runs `npm run build`.
* **Logging & Observability:**
  * `RequestLoggingFilter` logs HTTP method, URI, status code, and execution duration in milliseconds.
  * Spring Boot Actuator exposes `/actuator/health` and `/actuator/info` (configured in `application.properties`).
* **API Documentation:**
  * Swagger UI enabled at `/swagger-ui/index.html` via `springdoc-openapi-starter-webmvc-ui`.
  * `OpenApiConfig.java` defines JWT Bearer security scheme for in-browser authorization.

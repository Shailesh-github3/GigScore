# GigScore — Full Project Analysis

---

## 1. PROJECT OVERVIEW

### What the Project Does
GigScore is a full-stack web application that lets gig workers and freelancers log work events (earnings, ratings) across platforms (Swiggy, Zomato, Uber, Rapido, Upwork) and computes a unified **GigScore** — a weighted performance metric from 0 to 100 that quantifies the worker's overall reliability and efficiency.

### Problem It Solves
Gig workers operate across multiple platforms, each with its own earnings, ratings, and job counts. There is no unified way to quantify cross-platform performance. GigScore aggregates these metrics into a single score, allowing workers to track holistic performance, identify weak areas, and improve.

### Real-World Use Case
A delivery partner working on both Swiggy and Zomato can log each delivery's earnings and rating. The system merges data across platforms, computes a weighted score (earnings 35%, jobs 25%, ratings 30%, active days 10%), and displays trends, AI suggestions, and a dashboard.

### Target Users
- Freelancers and gig economy workers
- Delivery partners (food, ride-share)
- Freelance platform workers (Upwork, Fiverr)
- Anyone wanting to track multi-platform gig performance

### High-Level Workflow
1. User registers or logs in → receives JWT token
2. User lands on Dashboard → sees aggregated metrics (total earnings, jobs completed, avg rating, active days, recent activity feed)
3. User navigates to "Add Gig" → selects platform, enters amount + rating → system updates platform aggregates and recalculates GigScore
4. User visits "Score" page → sees score (0-100), score breakdown with formula components, AI-generated suggestions
5. User can use AI Chat widget → asks questions about score, receives replies from Gemini API
6. User can view recent activities across all platforms

### Why the Project Is Useful
- **Unified metric**: Consolidates multi-platform gig data into one score
- **Weighted scoring**: Uses a non-linear, power-weighted formula that rewards early progress and penalizes low ratings more aggressively
- **Audit trail**: Every gig event is recorded as an activity with timestamp
- **AI guidance**: Gemini integration provides conversational insights
- **Data deduplication**: Automatically merges duplicate platform records

---

## 2. COMPLETE TECHNICAL BREAKDOWN

### Java Version and Technologies
- **Java 21** (records used in 4 DTOs)
- **Spring Boot 4.0.4** (latest major version)
- **Maven** build tool
- **Lombok** (boilerplate reduction via @Data, @Builder)
- **Jakarta EE** (validation, persistence)

### Frameworks and Libraries
| Dependency | Purpose |
|---|---|
| `spring-boot-starter-data-jpa` | ORM via Hibernate |
| `spring-boot-starter-security` | Authentication & authorization |
| `spring-boot-starter-webmvc` | REST API framework |
| `spring-boot-starter-validation` | Bean Validation (@Valid, @NotBlank, etc.) |
| `spring-boot-starter-cache` | Spring Cache abstraction |
| `spring-boot-starter-websocket` | WebSocket support (dependency listed but no WebSocket endpoints implemented) |
| `springdoc-openapi-starter-webmvc-ui` | Swagger/OpenAPI documentation |
| `io.jsonwebtoken (jjwt)` v0.11.5 | JWT creation and parsing |
| `jackson-databind` | JSON serialization |
| `mysql-connector-j` | MySQL JDBC driver |
| `h2` (test scope) | In-memory database for tests |
| `spring-boot-devtools` | Development hot-reload |

### Database / Storage
- **Production**: MySQL (via `mysql-connector-j`)
- **Testing**: H2 in-memory (`application-test.properties`: `jdbc:h2:mem:testdb`)
- **Schema generation**: Hibernate `ddl-auto` (likely `update` in prod, `create-drop` in test)
- **Caching**: Spring Cache abstraction (`@EnableCaching`, `@Cacheable("scores")`, `@CacheEvict("scores")`)
- **No external cache provider** configured — defaults to `ConcurrentHashMap`

### APIs

**Internal REST API** (all return JSON):

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `/api/users` | POST | No | Register user, returns JWT |
| `/api/users/login` | POST | No | Login, returns JWT |
| `/api/users/{userId}` | GET | JWT | Dashboard with metrics + gig summaries |
| `/api/gigs` | POST | JWT | Log a gig event, returns updated dashboard |
| `/score/{userId}` | GET | JWT | Get computed GigScore (0-100) |
| `/api/activity/{userId}` | GET | JWT | Get 5 most recent activity entries |
| `/api/chat/ask` | POST | JWT | Send chat messages, receive Gemini reply |

**External API consumed**:
- **Google Gemini API** (`https://generativelanguage.googleapis.com/v1beta/models/...:generateContent`)
  - Model: `gemini-2.5-flash` (configurable)
  - Temperature: 0.7, TopP: 0.9

### Architecture Style
- **Monolithic REST API** with **JWT-based stateless authentication**
- **Layered architecture**: Controller → Service → Repository → Entity
- **Frontend-backend separation**: React SPA (Vite) communicates with backend via REST

### Project Structure (Backend)
```
backend/gigscore/
  pom.xml
  src/main/java/com/org/gigscore/
    GigscoreApplication.java         -- Main entry point, env validation
    config/
      CorsConfig.java                 -- CORS for localhost:5173
      JwtAuthFilter.java              -- Once-per-request JWT validation filter
      JwtUtil.java                    -- JWT token generation & parsing
      OpenApiConfig.java              -- Swagger/OpenAPI config with bearer auth
      SecurityConfig.java             -- Spring Security filter chain config
    controller/
      UserController.java             -- /api/users (register, login, dashboard)
      GigDataController.java          -- /api/gigs (log gig event)
      GigScoreController.java         -- /score/{userId} (get score)
      ActivityController.java         -- /api/activity/{userId} (recent activities)
      ChatController.java             -- /api/chat/ask (AI chat)
    dto/
      CreateUserRequest.java          -- Registration payload
      LoginDTO.java                   -- Login payload
      LoginResponseDTO.java           -- JWT + user info response
      GigEventRequest.java            -- Gig event payload
      UserDashboardResponse.java      -- Full dashboard response
      GigSummaryResponse.java         -- Per-platform summary
      ScoreResponse.java              -- Score + userId
      ActivityResponse.java           -- Activity feed item
      ChatRequestDTO.java             -- Chat request (record)
      ChatMessageDTO.java             -- Chat message (record)
      ChatResponseDTO.java            -- Chat reply (record)
    entity/
      User.java                       -- User entity
      GigData.java                    -- Platform aggregate entity
      GigScore.java                   -- Score entity
      Activity.java                   -- Activity log entity
    exception/
      GlobalExceptionHandler.java     -- @RestControllerAdvice
      ResourceNotFoundException       -- 404
      BadRequestException             -- 400
      DuplicateResourceException      -- 409
      UnauthorizedException           -- 401
      UnauthorizedAccessException     -- 403
      GeminiApiException              -- 502
      ErrorResponse.java              -- Error response body
    repository/
      UserRepository.java             -- JPA repository, findByEmail
      GigDataRepository.java          -- JPA repository, find by user+platform
      GigScoreRepository.java         -- JPA repository, findByUser
      ActivityRepository.java         -- JPA repository, findTop5ByUser
    security/
      CurrentUserResolver.java        -- Extracts current user from SecurityContext
    service/
      UserService.java                -- Registration, login, dashboard delegation
      GigDataService.java             -- Add gig, merge duplicates, dashboard
      GigScoreService.java            -- Score calculation & persistence
      UserMetricsService.java         -- Aggregates metrics across platforms
      ActivityService.java            -- Record & retrieve activity entries
      GeminiChatService.java          -- Chat via Gemini HTTP API
  src/test/java/com/org/gigscore/
    GigscoreApplicationTests.java     -- Context load test
    controller/
      UserControllerIntegrationTest.java -- Register + login integration test
      GigDataControllerIntegrationTest.java -- Cross-user access denial test
    service/
      GigScoreServiceTest.java        -- 4 unit tests for score calculation
      GigDataServiceTest.java         -- 2 unit tests for gig operations
```

### Frontend Structure (React + Vite + Tailwind)
```
frontend/
  src/
    App.jsx                           -- Router, auth state, dark mode management
    pages/
      Login.jsx                       -- Sign-in form
      CreateUser.jsx                  -- Registration form
      Dashboard.jsx                   -- Metrics + activity feed
      AddGig.jsx                      -- Platform selection + gig form
      Score.jsx                       -- Score display + breakdown + AI suggestions
    components/
      Navbar.jsx                      -- Top bar with dark mode toggle + profile
      Sidebar.jsx                     -- Navigation sidebar
      AiChatWidget.jsx                -- Floating chat panel
      PlatformCard.jsx                -- Platform selection card
      StatCard.jsx                    -- Metric display card
      ScoreHistoryChart.jsx           -- Score visualization
    services/
      httpClient.js                   -- Axios instance with JWT interceptor
      userService.js                  -- Login, register, dashboard
      gigService.js                   -- Add gig
      scoreService.js                 -- Get score
      activityService.js              -- Get recent activities
      chatService.js                  -- AI chat
    utils/
      scoreFormula.js                 -- Client-side score calculation (mirrors backend)
```

### Data Flow Between Components

```
[React Frontend] --HTTP/JSON + JWT--> [Spring Boot Backend]
                                           |
  POST /api/users (register)         --> UserService.createUser()
                                           ├── UserRepository.save()
                                           └── JwtUtil.generateToken()
  POST /api/users/login              --> UserService.login()
                                           ├── PasswordEncoder.matches()
                                           └── JwtUtil.generateToken()
  GET /api/users/{id}                --> UserService.getUserDashboard()
                                           ├── GigDataRepository.findByUser()
                                           ├── UserMetricsService.calculateAggregates()
                                           ├── GigScoreService.getScoreForUser()
                                           └── DTO assembly
  POST /api/gigs                     --> GigDataService.addGig()
                                           ├── GigDataRepository (resolve/merge/update)
                                           ├── ActivityService.recordGigAdded()
                                           └── GigScoreService.calculateAndPersistScore()
  GET /score/{id}                    --> GigScoreService.getScoreForUser()
                                           └── (cached via @Cacheable)
  POST /api/chat/ask                 --> GeminiChatService.generateReply()
                                           └── HTTP call to Google Gemini API
```

### Execution Flow — Add Gig (Complete Walkthrough)

1. **Frontend**: User selects platform, enters amount + rating, clicks Submit
2. **Frontend**: `addGig()` in `gigService.js` → POST `/api/gigs` with JWT Bearer token
3. **JwtAuthFilter**: Validates JWT, extracts email, sets SecurityContext
4. **GigDataController**: Sets `userId` from `CurrentUserResolver.getCurrentUserId()`
5. **GigDataService.addGig()`**:
   - Throws `BadRequestException` if userId null
   - Finds User by ID (`ResourceNotFoundException` if missing)
   - Normalizes platform name (trim)
   - `resolveOrCreatePlatformAggregate()`:
     - Finds all GigData rows for user+platform
     - If empty: creates new GigData with zero values
     - If one row: returns it
     - If multiple rows: merges earnings, jobs, active days; computes weighted average rating; deletes duplicates
   - Updates: earnings += amount, jobs += 1, activeDays += 1
   - Computes new running average rating: `((oldAvg * oldCount) + newRating) / newCount`
   - Saves updated GigData
   - Records Activity (platform, amount, rating, timestamp)
   - Calls `GigScoreService.calculateAndPersistScore(user)`:
     - Gets aggregate metrics via `UserMetricsService`
     - Computes score via weighted sublinear formula
     - Resolves existing GigScore or creates new
     - Saves score
   - Evicts cache entry (`@CacheEvict("scores", key="#request.userId")`)
   - Returns `UserDashboardResponse`

6. **Frontend**: Receives updated dashboard, re-renders with new metrics

---

## 3. CODE-LEVEL ANALYSIS

### `GigScoreService.java` — Core Scoring Engine
**Purpose**: Computes the weighted performance score for a user.

**Important Methods**:
- `calculateAndPersistScore(User user)`: Gets aggregate metrics, computes score, persists to DB, returns `ScoreResponse`
- `calculateScoreValue(UserAggregateMetrics)`: Implements the full scoring formula
- `getScoreForUser(User)`: Cached wrapper around calculateAndPersistScore
- `normalizedComponent(double value, double target)`: Returns `clamp(value/target, 0, 1)`
- `clamp(...)`: Standard min/max clamp
- `roundToTwoDecimals(...)`: Rounds to 2 decimal places

**Scoring Formula** (implemented in `calculateScoreValue`):
```
earningsComponent  = (min(earnings/5000, 1))^0.70  * 0.35 * 100
jobsComponent      = (min(jobs/100, 1))^0.65       * 0.25 * 100
ratingComponent    = (min(rating/5, 1))^1.40        * 0.30 * 100
activeDaysComponent= (min(activeDays/30, 1))^1.00   * 0.10 * 100

Total Score = earningsComponent + jobsComponent + ratingComponent + activeDaysComponent
```

**Design Decisions**:
- **Sublinear exponents (< 1) for earnings and jobs** (0.70, 0.65): This rewards early progress more. Going from 0→10 jobs gives more score points than 90→100 jobs. This makes the score approachable for new workers.
- **Supra-linear exponent for rating (1.40)**: This penalizes low ratings heavily. A 4.0/5 rating scores significantly more than a 3.0/5. This encourages quality.
- **Neutral exponent for active days (1.00)**: Linear relationship for consistency.
- **Weights**: Earnings (35%) and Rating (30%) are prioritized over Jobs (25%) and Active Days (10%).
- **Targets**: $5000 earnings, 100 jobs, 5.0 rating, 30 active days. These are fixed constants, not configurable per user or per platform.

**Possible Improvements**:
- Make targets configurable (application.properties or per-user)
- Add exponential moving average for ratings instead of cumulative average
- Add recency weighting (recent gigs matter more)
- Add caching for the score calculation results
- Consider time decay for older gigs

### `GigDataService.java` — Data Management
**Purpose**: Manages gig data aggregation, deduplication, and dashboard assembly.

**Important Methods**:
- `addGig(GigEventRequest)`: Main orchestration — resolves/creates platform aggregate, updates metrics, records activity, recalculates score
- `resolveOrCreatePlatformAggregate(User, String)`: Handles deduplication of platform records
- `getUserDashboard(Long)`: Assembles full dashboard response
- `toGigSummaryResponse(GigData)`: Maps entity to DTO

**Design Decisions**:
- **Duplicate platform data merging**: The `resolveOrCreatePlatformAggregate` method handles the case where multiple `GigData` rows exist for the same user+platform. This could happen due to data inconsistencies or legacy issues. It merges them into one row and deletes duplicates.
- **Running average calculation**: When adding a new gig, the average rating is recalculated incrementally: `(oldAvg * oldCount + newRating) / (oldCount + 1)`. This is correct because the average was previously a weighted average across platforms.
- **Caching**: `@CacheEvict` on `addGig` clears the score cache for that user. This ensures the score is recalculated on next access.
- **Transactional**: The method is `@Transactional`, ensuring atomicity of the multi-step operation (save gig → record activity → recalculate score).

### `UserMetricsService.java` — Aggregation
**Purpose**: Calculates aggregate metrics across all platforms for a user.

**Key Implementation**:
```java
for (GigData data : gigDataList) {
    totalEarnings += data.getTotalEarnings();
    totalJobs += data.getJobsCompleted();
    totalRating += data.getAvgRating() * data.getJobsCompleted();  // weighted by jobs
    totalActiveDays += data.getActiveDays();
}
avgRating = totalJobs > 0 ? totalRating / totalJobs : 0;
```

This correctly computes a **weighted average rating** across platforms. If a user has 10 jobs on Swiggy (avg 4.5) and 2 jobs on Uber (avg 3.0), the overall average is `(4.5*10 + 3.0*2) / 12 = 4.25`, not `(4.5 + 3.0)/2 = 3.75`.

### `UserService.java` — Authentication & Registration
**Important Methods**:
- `createUser(CreateUserRequest)`: Normalizes email (lowercase, trim), checks for duplicates, hashes password with BCrypt, saves user, returns JWT
- `login(LoginDTO)`: Normalizes email, finds user, verifies password (BCrypt match), supports legacy plaintext password upgrade

**Password Handling** (notable code in `login`):
```java
if (!authenticated && rawPassword.equals(storedPassword)) {
    authenticated = true;
    user.setPassword(passwordEncoder.encode(rawPassword));
    userRepository.save(user);  // Upgrade legacy plaintext to BCrypt
}
```
This implements a **backward-compatible password upgrade path**. If the stored password is plaintext (from an earlier version), it authenticates via direct string comparison, then upgrades to BCrypt. This is a good migration strategy but indicates the project previously stored passwords in plaintext.

### `GeminiChatService.java` — AI Integration
**Purpose**: Sends chat messages to Google Gemini API and returns the AI response.

**Key Details**:
- Uses `java.net.http.HttpClient` directly (not Spring's RestTemplate/WebClient)
- Constructs payload with `contents` array (role + parts)
- Configurable model via `gemini.model` property (default: `gemini-2.5-flash`)
- Temperature: 0.7, TopP: 0.9
- Extracts text from response JSON path: `candidates[0].content.parts[0].text`
- Model name normalization: strips `models/` prefix if present

**Error Handling**:
- Throws `GeminiApiException` for: missing API key, serialization failure, HTTP 4xx/5xx, empty response, parse failure
- Thread interruption is properly propagated: `Thread.currentThread().interrupt()`

### `JwtUtil.java` — Token Management
- Uses HMAC-SHA key from `jwt.secret` property
- Token expiration: 1 hour (3600000 ms)
- Subject: email address
- `extractEmail()` catches all exceptions (returns null on failure)
- `validateJwtToken()` simply checks if email extraction succeeds

### `SecurityConfig.java`
- CSRF disabled (stateless JWT auth)
- CORS enabled
- Stateless session management
- Public endpoints: `/api/users/login`, `/api/users` (POST), `/error`, Swagger UI paths
- All other endpoints require authentication
- `JwtAuthFilter` added before `UsernamePasswordAuthenticationFilter`
- HTTP Basic and Form Login disabled
- Password encoder: BCrypt

### `CorsConfig.java`
- `/api/**` allows GET, POST, PUT, PATCH, DELETE, OPTIONS from `localhost:5173` and `127.0.0.1:5173`
- `/score/**` allows GET, OPTIONS from same origins

### `CurrentUserResolver.java`
- Extracts currently authenticated user from `SecurityContextHolder`
- Gets email from `Authentication.getName()` (set by JwtAuthFilter)
- Looks up User entity by email
- `getCurrentUserId()` returns the user ID

---

## 4. OBJECT-ORIENTED PROGRAMMING ANALYSIS

### Encapsulation
- **Adequate**: Entities use Lombok `@Data` which generates getters/setters — this is standard Spring Boot practice but means fields are accessible for mutation
- **DTOs**: Separate request/response DTOs ensure internal entities are not exposed directly
- **Service dependencies**: Injected via constructor injection (good practice)

### Inheritance
- **Minimal**: Custom exceptions extend `RuntimeException` (standard pattern)
- `JwtAuthFilter extends OncePerRequestFilter` (Spring Security pattern)
- `CorsConfig implements WebMvcConfigurer` (Spring pattern)
- No domain inheritance (no base entity class, no `@MappedSuperclass`)

### Polymorphism
- **Spring-managed**: `@Service`, `@Repository`, `@Component` annotations enable Spring DI polymorphism
- Method overloading: Not used in the codebase
- Interface-based: Repositories extend `JpaRepository` interfaces; `WebMvcConfigurer` interface implementation

### Abstraction
- **Service layer abstracts business logic** from controllers
- **Repository layer abstracts database access** via JPA
- **DTOs abstract internal entity representation** from API responses
- **Exception hierarchy abstracts error handling** via `@RestControllerAdvice`

### Interfaces
- `WebMvcConfigurer` (Spring interface)
- `JpaRepository<T, ID>` (Spring Data interface)
- No custom interfaces or contracts defined in the project

### Class Relationships
```
UserController → UserService → UserRepository, JwtUtil, PasswordEncoder, GigDataService
GigDataController → GigDataService → GigDataRepository, UserRepository, GigScoreService, UserMetricsService, ActivityService
GigScoreController → GigScoreService → GigScoreRepository, UserMetricsService
ActivityController → ActivityService → ActivityRepository
ChatController → GeminiChatService

Entity Relationships:
User 1──→* GigData      (One user has many platform aggregates)
User 1──→* Activity     (One user has many activity entries)
User 1──→1 GigScore     (One user has one score record)
```

### SOLID Principles Analysis

**S — Single Responsibility**: Mostly followed.
- Each service has one primary responsibility (GigScoreService → scoring, UserMetricsService → aggregation, ActivityService → activity logging)
- Controllers handle only HTTP concerns
- `GigDataService` has a somewhat broad responsibility (gig management + dashboard assembly + score triggering), but this is reasonable for the application's size

**O — Open/Closed**: Partially followed.
- New entity types require new repositories, services, controllers (no plugin architecture)
- `GlobalExceptionHandler` can be extended by adding new `@ExceptionHandler` methods for new exception types
- The scoring formula is hardcoded in constants, not easily extensible without code changes

**L — Liskov Substitution**: Followed.
- Custom exceptions extend `RuntimeException` without behavior changes
- No inheritance hierarchies in domain model

**I — Interface Segregation**: Not demonstrated.
- No custom interfaces defined. The project relies on Spring's built-in interfaces

**D — Dependency Inversion**: Followed via Spring DI.
- Services depend on repository interfaces (not concrete implementations)
- Controllers depend on service interfaces (concrete classes, but injectable)

### Assessment
**OOP is applied at a basic-intermediate level.** The codebase follows standard Spring Boot patterns (layered architecture, DI, repository pattern). There is no custom interface design, no strategy pattern, no factory pattern, no polymorphism in domain logic. The project primarily uses Spring's built-in abstractions rather than defining its own. This is typical for a Spring Boot project of this size.

---

## 5. DESIGN PATTERN ANALYSIS

### Patterns Actually Used

1. **Builder Pattern** (via Lombok `@Builder`)
   - Used in: All response DTOs (`LoginResponseDTO`, `UserDashboardResponse`, `GigSummaryResponse`, `ScoreResponse`, `ActivityResponse`)
   - Where: DTO construction in service classes
   - Why: Clean, readable object creation with optional parameters

2. **Singleton Pattern** (via Spring's default bean scope)
   - Used in: All `@Service`, `@Repository`, `@Component`, `@Configuration` classes
   - Why: Single bean instance shared across the application

3. **Dependency Injection / Inversion of Control**
   - Used in: Constructor injection throughout all services and controllers
   - Why: Loose coupling, testability via mocking

4. **Template Method Pattern** (via Spring)
   - `OncePerRequestFilter` (JwtAuthFilter follows the template)
   - `JpaRepository` provides template methods for CRUD

5. **Front Controller Pattern** (via Spring MVC)
   - `DispatcherServlet` handles all incoming requests
   - Controllers handle specific routes

6. **Data Access Object (DAO) Pattern** (via Spring Data JPA)
   - Repository interfaces abstract database operations

7. **Transfer Object (DTO) Pattern**
   - Separate request/response objects for each API endpoint
   - Prevents entity exposure

8. **Global Exception Handler / Middleware Pattern**
   - `@RestControllerAdvice` with `@ExceptionHandler`

9. **Caching Pattern** (via Spring @Cacheable)
   - `@Cacheable("scores")` on `GigScoreService.getScoreForUser()`
   - `@CacheEvict("scores")` on `GigDataService.addGig()`

10. **Filter Chain Pattern** (via Spring Security)
    - `JwtAuthFilter` in the filter chain

### Assessment
The project uses **framework-level design patterns** (Spring's built-in patterns) rather than implementing custom design patterns. No custom strategy, factory, observer, decorator, or adapter patterns are implemented. This is appropriate for a project of this scope but limits the demonstration of advanced design pattern knowledge.

---

## 6. DATABASE ANALYSIS

### Database Structure (MySQL)

**Tables:**
1. **`user`** (from `User.java`)
   - `user_id` BIGINT PK AUTO_INCREMENT
   - `email` VARCHAR(255) NOT NULL UNIQUE
   - `password` VARCHAR(255)
   - `name` VARCHAR(255)

2. **`gig_data`** (from `GigData.java`)
   - `gig_id` BIGINT PK AUTO_INCREMENT
   - `user_id` BIGINT NOT NULL FK → user
   - `platform` VARCHAR(255)
   - `total_earnings` DOUBLE
   - `jobs_completed` INTEGER
   - `avg_rating` DOUBLE
   - `active_days` INTEGER

3. **`gig_score`** (from `GigScore.java`)
   - `gig_score_id` BIGINT PK AUTO_INCREMENT
   - `user_id` BIGINT NOT NULL FK → user (UNIQUE constraint)
   - `score` DOUBLE
   - `created_at` DATETIME (generated)
   - `updated_at` DATETIME (updated on change)

4. **`activity`** (from `Activity.java`)
   - `id` BIGINT PK AUTO_INCREMENT
   - `user_id` BIGINT NOT NULL FK → user
   - `platform` VARCHAR(255)
   - `action` VARCHAR(255)
   - `amount` DOUBLE
   - `rating` DOUBLE
   - `timestamp` DATETIME

### Entity Relationships
```
User ──1:N──> GigData     (One user has many platform entries)
User ──1:N──> Activity    (One user has many activity log entries)
User ──1:1──> GigScore    (One user has one score, enforced by UNIQUE(user_id))
```

### Queries
- `findByEmail(String)` — User lookup for authentication
- `findByUser(User)` — Get all gig data for a user
- `findAllByUserAndPlatform(User, String)` — Platform-specific lookup
- `findByUserAndPlatform(User, String)` — Optional single result
- `findByUser(User)` — Score lookup
- `findTop5ByUserOrderByTimestampDesc(User)` — Recent 5 activities

### ORM Usage
- Standard Spring Data JPA with Hibernate
- `@ManyToOne` relationships with `@JoinColumn`
- `@CreationTimestamp` / `@UpdateTimestamp` for automatic timestamping
- `@Table(uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))` for 1:1 constraint
- No cascading, no lazy/eager loading configuration (defaults apply)
- No custom JPQL or native queries

### Validation
- JPA annotations: `@Column(nullable = false, unique = true)` on email
- Jakarta Validation on DTOs: `@NotBlank`, `@Email`, `@Positive`, `@Min(0)`, `@Max(5)`
- Bean Validation triggered by `@Valid` in controller `@RequestBody`
- No database-level validation beyond unique constraints and NOT NULL

### Possible Improvements
- **Indexes**: No explicit indexes defined. Should add indexes on `user_id` columns in all tables, and on `platform` for filtering
- **Cascading**: No cascade operations defined. Deleting a user would fail due to FK constraints. Should add `cascade = CascadeType.ALL, orphanRemoval = true` on relationships
- **Column constraints**: `platform` could be `@Enumerated(EnumType.STRING)` instead of String for type safety
- **Decimal precision**: DOUBLE for monetary values is imprecise. Should use `BigDecimal` for `totalEarnings`
- **Audit fields**: Missing `@CreatedDate`, `@LastModifiedDate` on most entities (only GigScore has timestamps)
- **Connection pooling**: No HikariCP configuration exposed

---

## 7. ALGORITHM AND LOGIC ANALYSIS

### Scoring Algorithm
**Complexity**: O(n) where n = number of platforms the user has data on
- `UserMetricsService.calculateAggregates()` iterates over all `GigData` rows for a user
- The score formula itself is O(1) — four `Math.pow()` calls with pre-clamped values

**Formula**:
```
score = (min(earnings/5000, 1)^0.70 * 35) + (min(jobs/100, 1)^0.65 * 25) + (min(rating/5, 1)^1.40 * 30) + (min(days/30, 1)^1.00 * 10)
```

**Data Structures**:
- `ArrayList` in `resolveOrCreatePlatformAggregate` for duplicate rows
- `List<GigData>` for aggregated data
- `Set<String>` in frontend for connected platforms (`connectedPlatforms`)

### Rating Average Calculation
**Correctness**: The weighted average calculation is correct
```java
totalRating += data.getAvgRating() * data.getJobsCompleted();  // sum of (avg*count)
avgRating = totalJobs > 0 ? totalRating / totalJobs : 0;       // divide by total jobs
```
This gives more weight to platforms with more jobs completed.

### Running Average Update
When adding a new gig:
```java
int oldCount = data.getJobsCompleted() - 1;
double newAvg = ((data.getAvgRating() * oldCount) + request.getRating()) / data.getJobsCompleted();
```
This correctly computes the incremental running average.

### Deduplication Algorithm
When multiple `GigData` rows exist for the same user+platform:
1. Sum earnings, jobs, active days
2. Compute weighted average rating: `sum(avgRating * jobs) / sum(jobs)`
3. Update first row with merged values
4. Delete all subsequent rows

**Time Complexity**: O(d) where d = number of duplicate rows

### Opportunity for Improvement
- **No time-based decay**: All gigs are weighted equally regardless of age. A gig from 6 months ago counts the same as yesterday's.
- **No recency weighting**: Recent performance is not distinguished from historical performance.
- **Caching scope**: The score cache is not time-expiring. Cache entries persist until explicitly evicted (when a new gig is added).
- **Page/limit**: Activity feed is hardcoded to 5 entries. No pagination parameters.

---

## 8. SECURITY ANALYSIS

### Authentication
- **JWT-based**: Users receive a JWT upon registration or login
- **Token validation**: Performed in `JwtAuthFilter` (OncePerRequestFilter)
- **Token content**: Only email is stored in the subject claim. No roles, permissions, or other claims.
- **Token method**: HMAC-SHA256 via `Keys.hmacShaKeyFor()` — symmetric signing (same key signs and verifies)
- **Expiration**: 1 hour (short-lived, reasonable)

### Authorization
- **Resource ownership**: `CurrentUserResolver.getCurrentUserId()` extracts the authenticated user's ID from the email stored in the JWT
- **Access control**: Controllers check `currentUserResolver.getCurrentUserId().equals(userId)` and throw `UnauthorizedAccessException (403)` if mismatch
- **Test verification**: `GigDataControllerIntegrationTest.userA_cannotAccess_userBData()` confirms that User A cannot access User B's data

### Input Validation
- **DTO-level validation**: `@NotBlank`, `@Email`, `@Positive`, `@Min(0)`, `@Max(5)` on request DTOs
- **Validation enforcement**: `@Valid` in controller parameters triggers validation
- **Validation error handling**: `MethodArgumentNotValidException` is caught by `GlobalExceptionHandler` returning 400 with field-specific error message
- **Normalization**: Email is lowercased and trimmed before storage/lookup

### Data Protection
- **Password hashing**: BCrypt via `BCryptPasswordEncoder` (Spring Security standard)
- **JWT secret**: Injected via `@Value("${jwt.secret}")` — should be in environment/properties, not hardcoded
- **Environment validation**: `GigscoreApplication.validateEnvironmentVariables()` checks for required env vars on startup
- **Legacy password upgrade**: Plaintext passwords are upgraded to BCrypt on successful legacy login

### Error Handling
- **Global exception handler**: Maps all exception types to appropriate HTTP status codes
- **Error response body**: `ErrorResponse` contains status, message, and timestamp
- **Exception-to-status mapping**:
  - `ResourceNotFoundException` → 404
  - `BadRequestException` → 400
  - `DuplicateResourceException` → 409
  - `UnauthorizedException` → 401
  - `UnauthorizedAccessException` → 403
  - `GeminiApiException` → 502 Bad Gateway
  - `MethodArgumentNotValidException` → 400
  - `Exception` (catch-all) → 500 Internal Server Error

### SQL Injection Risks
- **None**: Spring Data JPA uses parameterized queries by default. No raw SQL strings are used.

### Vulnerabilities & Risks

1. **Symmetric JWT signing**: If the `jwt.secret` is compromised, anyone can forge tokens. Should consider using asymmetric keys (RS256) for production.

2. **No token revocation**: "Logout" in the frontend only removes the token from localStorage. The token remains valid until expiration (1 hour). No blacklist mechanism.

3. **No role-based access**: All authenticated users have the same permissions. No admin roles, no RBAC.

4. **Cross-User Access Test**: The test `userA_cannot_access_userBData` tests access control, but the implementation has a flaw — `getUserIdFromToken()` creates a *new* user rather than extracting the user ID from the token. It creates a dummy user and reads its ID. The test is actually testing that User A cannot access the dummy user's data. This partially validates access control but doesn't properly test that Bob's actual data is protected from Alice.

5. **No rate limiting**: API endpoints have no rate limiting protection.

6. **No HTTPS enforcement**: Not configured at the application level (assumed to be handled at infrastructure level).

7. **No audit logging**: No request logging for security events (failed logins, access violations).

8. **CORS is permissive**: `allowedHeaders("*")` allows all headers. Acceptable for development but should be tightened for production.

9. **Password field**: `password` field in `User` entity is `null`-able (no `@Column(nullable = false)`). This could allow users without passwords.

### Security Improvements
- Implement token blacklist/refresh mechanism
- Add rate limiting (`bucket4j` or Spring Gateway)
- Add request logging with security events
- Switch to RS256 asymmetric JWT signing
- Add password strength validation
- Use `@Column(nullable = false)` on password field
- Add role-based access control
- Remove unused `SPRING_SECURITY_USER` / `SPRING_SECURITY_PASSWORD` from `.env.example`

---

## 9. PERFORMANCE AND SCALABILITY

### Current Performance Considerations

1. **Caching**: Score results are cached via Spring's `ConcurrentHashMap` cache. Cache is evicted whenever a new gig is added. This means:
   - Dashboard page views hit the cache (fast)
   - Score page views hit the cache (fast)
   - Each gig addition evicts and recalculates (O(n) with n = number of platforms)

2. **Database queries**: Each request to load a dashboard performs:
   - 1 user lookup by ID
   - 1 gig data query by user
   - 1 gig score query by user
   - All are indexed by primary key (fast)

3. **Frontend optimization**: 
   - Parallel API calls via `Promise.all` on Dashboard and Score pages
   - Token stored in localStorage (fast access)
   - React state management is simple (no Redux overhead)

### Possible Bottlenecks

1. **Single cache node**: Spring's `ConcurrentHashMap` cache is local to the JVM instance. In a multi-instance deployment, each instance has its own cache, leading to cache inconsistency.

2. **No database connection pooling tuning**: Default HikariCP settings are used. Without configuration, the pool may not handle concurrent load efficiently.

3. **Score recalculation on every gig**: Every gig addition triggers a full score recalculation (including metric aggregation across all platforms). For users with many platforms, this is O(p) but still very fast.

4. **No pagination for activity feed**: Hardcoded to 5 entries, which is not a scaling concern.

5. **Synchronous Gemini API call**: The AI chat makes a synchronous HTTP call to Google Gemini. This blocks the request thread for potentially seconds. No timeout is configured on the HttpClient.

6. **NO websocket usage**: `spring-boot-starter-websocket` is imported but no WebSocket endpoints exist. The chat feature uses REST polling, not real-time.

### Memory Concerns
- Negligible for the current scope. The application handles small amounts of data per user (few platforms, few activities).
- Caching scores in memory for all active users could become a concern at scale (thousands of users).

### Database Optimization
- Missing indexes beyond primary keys and unique constraints
- `platform` field is a free-text string. If many users use the same platforms, indexing could help.
- No data archival strategy for old activity records

### How It Handles Growth
- The layered architecture allows for scaling horizontally (multiple instances behind a load balancer)
- The cache limitation (local ConcurrentHashMap) would need to be replaced with Redis for multi-instance deployments
- The monolithic architecture would eventually need to be split into microservices (auth service, scoring service, data service)
- No sharding or partitioning strategy

### Changes Needed for Production Scalability
1. Replace local cache with Redis (Spring Cache with Redis)
2. Add database read replicas for dashboard/score views
3. Add connection pooling configuration (max pool size, timeout)
4. Make Gemini API call asynchronous (async service + polling or WebSocket push)
5. Add pagination to activity feed
6. Add request rate limiting
7. Containerize with proper resource limits

---

## 10. TESTING ANALYSIS

### Existing Tests

**Integration Tests** (2 files):

1. **`UserControllerIntegrationTest.java`** — 1 test
   - `register_thenLogin_returnsToken()`: Registers a user (POST /api/users), verifies response has userId, name, email, token. Then logs in (POST /api/users/login) with same credentials, verifies token is returned.

2. **`GigDataControllerIntegrationTest.java`** — 1 test
   - `userA_cannot_access_userBData()`: Registers User A and User B, tries to access User B's dashboard with User A's token, expects 403 Forbidden.
   - **Note**: The `getUserIdFromToken()` helper creates a *new* user instead of extracting the ID from the token. The test does not properly test cross-user access to actual Bob data but tests access to a dummy user's data.

**Unit Tests** (2 files, Mockito-based):

3. **`GigScoreServiceTest.java`** — 4 tests
   - `calculateAndPersistScore_normalValues()`: Score with $2500, 50 jobs, 4.0 rating, 15 days → verifies score > 0
   - `calculateAndPersistScore_zeroJobs()`: All zeros → verifies score == 0.0
   - `calculateAndPersistScore_maxRating()`: 5.0 rating → verifies score > 0
   - `calculateAndPersistScore_zeroEarnings()`: 0 earnings with jobs → verifies score > 0

4. **`GigDataServiceTest.java`** — 2 tests
   - `addGig_duplicatePlatform_mergeRows()`: Creates two GigData rows for same user+platform, adds a new gig, verifies rows are merged, earnings = 1700, jobs = 16, activeDays = 9
   - `addGig_incrementalAverageRating()`: Existing 2 jobs with 4.0 avg, adds a gig with 5.0, verifies new avg = 4.333, jobs = 3, earnings = 2100

5. **`GigscoreApplicationTests.java`** — 1 test
   - `contextLoads()`: Verifies Spring context loads successfully

### Test Quality Assessment

**Strengths**:
- Both unit tests use Mockito properly (mock dependencies, inject mocks)
- `GigScoreServiceTest` tests edge cases (zero values, max rating, zero earnings)
- `GigDataServiceTest` validates the deduplication logic thoroughly
- Integration tests use `MockMvc` against a real Spring context with H2 database
- Test configuration uses `@ActiveProfiles("test")` with H2 in-memory DB

**Weaknesses**:
- **No tests for**: UserService (create user, login), ActivityService, GeminiChatService, Scoring formula edge cases (exponent behavior, clamping)
- **No tests for**: Controllers beyond the two integration tests (Score, Activity, Chat controllers untested)
- **No tests for**: Security configuration (unauthorized access returns 401, public endpoints accessible)
- **No tests for**: Validation (invalid input returns 400)
- **No tests for**: Exception handlers
- **No tests for**: Frontend (no unit tests for React components or services)
- **Test coverage is low**: Only 6 actual test methods across 4 test files
- **GigDataControllerIntegrationTest.getUserIdFromToken()** creates a dummy user rather than extracting userId from the token, which is a flawed testing approach

### Coverage Estimate
- Service layer: ~60% (GigScoreService and GigDataService tested, UserService/ActivityService/GeminiChatService untested)
- Controller layer: ~30% (UserController and partial GigDataController tested)
- Entity/DTO/Config layers: 0% tested
- Exception handling: 0% tested
- Frontend: 0% tested

### Recommended Testing Approach
1. **Unit tests for remaining services**: UserService (registration validation, login success/failure, legacy password upgrade), ActivityService, GeminiChatService
2. **Unit tests for scoring formula edge cases**: Very high earnings, very low rating, boundary values
3. **Integration tests for all controllers**: ScoreController, ActivityController, ChatController
4. **Security integration tests**: Unauthenticated requests return 401/403, public endpoints accessible
5. **Validation tests**: Invalid input returns correct error responses
6. **Exception handler tests**: Each exception type maps to correct status code
7. **Frontend tests**: Component rendering, service function testing with mocked API

---

## 11. HONEST PROJECT EVALUATION

### Skill Level Demonstrated
**Intermediate**

The project demonstrates solid Spring Boot fundamentals but does not exhibit advanced Java or software engineering skills.

### Java Skills Shown
- **Good**: Spring Boot configuration, JPA/Hibernate ORM mapping, REST API design, JWT authentication, BCrypt password hashing, Lombok usage, Jakarta validation, global exception handling, caching
- **Basic**: Java 21 features (records used in 4 DTOs but not extensively), HTTP client usage
- **Not demonstrated**: Advanced Java 21 features (sealed classes, pattern matching, virtual threads, sequenced collections), reactive programming, concurrency handling, custom annotations, reflection, advanced generics

### Software Engineering Practices
- **Good**: Constructor injection, layered architecture, separation of concerns, meaningful package structure, environment validation on startup, backward-compatible password migration
- **Adequate**: Exception handling (custom hierarchy + global handler), caching strategy, test coverage for core logic
- **Missing**: Design patterns (beyond framework defaults), CI/CD configuration, containerization (Dockerfile), API versioning strategy, comprehensive logging, monitoring/health endpoints, configuration management

### Strong Points
1. **Clean scoring algorithm**: The weighted power-exponent formula is mathematically sound and well-documented
2. **Data deduplication**: Handling duplicate platform records is a thoughtful robustness measure
3. **Backward-compatible password upgrade**: Legacy plaintext passwords are upgraded transparently
4. **Cache-invalidation strategy**: Score cache is correctly invalidated when new gig data arrives
5. **Cross-user access control**: Resource ownership checks are implemented
6. **AI integration**: Gemini chat adds practical value beyond basic CRUD
7. **Environment validation**: Application fails fast if required config is missing
8. **Test structure**: Tests use proper patterns (Mockito, MockMvc, test profile)

### Weak Points
1. **WebSocket dependency unused**: `spring-boot-starter-websocket` is imported but no WebSocket endpoints exist.
2. **Password field nullable**: `User.password` has no `@Column(nullable = false)`, allowing users without passwords.
3. **Limited test coverage**: Only core services are tested. No tests for UserService, GeminiChatService, controllers beyond two, security, validation, or frontend.
4. **No proper logging**: No SLF4J/Logback logging statements. Only exceptions are logged implicitly (if at all).
5. **No API versioning**: Endpoints are at `/api/` with no version prefix like `/api/v1/`.
6. **Test flaw**: `GigDataControllerIntegrationTest.getUserIdFromToken()` creates a dummy user instead of parsing the token.
7. **No documentation**: Beyond auto-generated Swagger, no ADRs, no architecture documentation.
8. **Secrets in source**: `.env.example` contains placeholder secrets, but if `.env` is committed, actual secrets would be exposed.

### What Recruiters May Question
- "Why is WebSocket in the dependencies but not used?"
- "How would this handle 10,000 concurrent users?"
- "Why is password nullable in the User entity?"
- "Why no test for the user service or AI chat service?"
- "Is there any CI/CD pipeline?"
- "How is the Gemini API key secured in production?"
- "Why no Dockerfile or docker-compose?"

### Improvements That Would Increase Project Value
1. Add Dockerfile and docker-compose.yml with MySQL
2. Add Redis for distributed caching
3. Implement WebSocket for real-time score updates
4. Add comprehensive test coverage (>80%)
5. Add CI/CD pipeline (GitHub Actions)
6. Add proper application.properties with profile-specific configs
7. Add SLF4J logging throughout services
8. Add API versioning (v1 prefix)
9. Create a proper README with setup instructions, screenshots, architecture diagram
10. Add user profile management (update name, change password)

---

## 12. FEATURE EXTRACTION

### Implemented Features

1. **User Registration**
   - What: Create a new user account with name, email, password
   - How: POST `/api/users` → validates email uniqueness → hashes password with BCrypt → saves user → returns JWT token + user info
   - Why: Enables identity management and session creation

2. **User Login**
   - What: Authenticate with email and password
   - How: POST `/api/users/login` → finds user by email → verifies BCrypt hash (with legacy plaintext fallback) → returns JWT token + user info
   - Why: Provides secure authenticated access to the system

3. **JWT Authentication**
   - What: Stateless token-based authentication for all protected endpoints
   - How: `JwtAuthFilter` intercepts all requests → validates Bearer token → sets SecurityContext with user email
   - Why: Enables secure, stateless API access without server-side session storage

4. **User Dashboard**
   - What: Display aggregated performance metrics and per-platform summaries
   - How: GET `/api/users/{userId}` → aggregates gig data across platforms → fetches score → returns totalEarnings, jobsCompleted, avgRating, activeDays, score, gigSummaries
   - Why: Provides a single-pane view of the user's complete gig performance

5. **Log Gig Event**
   - What: Record a new gig completion with platform, amount, and rating
   - How: POST `/api/gigs` → resolves/creates platform aggregate → updates metrics → calculates running average rating → records activity → recalculates score → returns updated dashboard
   - Why: Core data collection feature that drives all analytics and scoring

6. **Platform Data Deduplication**
   - What: Automatically merge duplicate platform records for the same user
   - How: `resolveOrCreatePlatformAggregate()` finds all rows for user+platform → merges earnings/jobs/days → computes weighted rating → deletes duplicates
   - Why: Ensures data consistency if multiple records exist for the same platform

7. **GigScore Calculation**
   - What: Compute a unified performance score (0-100)
   - How: Weighted formula with exponents: earnings(35%*E^0.70) + jobs(25%*J^0.65) + rating(30%*R^1.40) + activeDays(10%*D^1.00)
   - Why: Quantifies overall gig worker reliability and efficiency into a single metric

8. **Score Caching**
   - What: Cache score results to avoid redundant computation
   - How: `@Cacheable("scores")` on `getScoreForUser()` → `@CacheEvict("scores")` on `addGig()`
   - Why: Improves dashboard and score page load performance

9. **Recent Activity Feed**
   - What: Display 5 most recent gig events with details
   - How: GET `/api/activity/{userId}` → queries `findTop5ByUserOrderByTimestampDesc()` → returns activity entries with platform, action, amount, rating, timestamp
   - Why: Provides an audit trail of recent work for transparency and tracking

10. **AI-Powered Chat**
    - What: Conversational AI assistant that answers questions about scores and performance
    - How: POST `/api/chat/ask` → sends messages to Google Gemini API with controlled temperature/topP → returns AI response
    - Why: Provides interactive guidance and insights beyond static dashboard data

11. **Nonlinear Score Contribution Display**
    - What: Show exact formula breakdown for each score component
    - How: Frontend `Score.jsx` computes contributions locally using `scoreFormula.js` and displays each component's formula with live values
    - Why: Educates users on how their score is calculated and where to improve

12. **AI Suggestions**
    - What: Display contextual tips based on current metrics
    - How: `Score.jsx` checks `jobsCompleted < 3` → shows beginner tip, otherwise shows advanced tip
    - Why: Provides basic guidance without requiring server-side AI call

13. **Dark Mode Toggle**
    - What: Switch between light and dark color themes
    - How: `Navbar.jsx` button → toggles `dark` class on document → persists preference to localStorage → applies Tailwind dark mode
    - Why: Improves user experience and accessibility

14. **Cross-User Access Control**
    - What: Prevent users from accessing other users' data
    - How: Controllers call `currentUserResolver.getCurrentUserId()` → compares with requested `userId` → throws `UnauthorizedAccessException(403)` on mismatch
    - Why: Enforces data privacy and isolation between users

15. **Structured Error Handling**
    - What: Consistent JSON error responses with status code, message, and timestamp
    - How: `GlobalExceptionHandler` maps each exception type to correct HTTP status → returns `ErrorResponse` with details
    - Why: Provides predictable error format for frontend consumption

16. **OpenAPI Documentation**
    - What: Auto-generated Swagger UI documentation for all endpoints
    - How: `springdoc-openapi` + `OpenApiConfig` with bearer token security scheme
    - Why: Enables easy API exploration and testing

17. **Password Migration**
    - What: Automatically upgrade legacy plaintext passwords to BCrypt
    - How: Login attempts plaintext comparison after BCrypt fails → if match, re-hashes with BCrypt and saves
    - Why: Ensures secure password storage without breaking existing users

---

## 13. RESUME CONVERSION

### A) Long Project Description

**GigScore — Multi-Platform Performance Scoring System for Gig Workers**

GigScore is a full-stack web application built with Spring Boot 4.0.4 and Java 21 that enables gig economy workers to track earnings, ratings, and job completions across multiple platforms (Swiggy, Zomato, Uber, Rapido, Upwork) and computes a unified GigScore — a weighted performance metric from 0 to 100.

The backend implements a layered REST API architecture with JWT-based stateless authentication, BCrypt password hashing, Spring Data JPA for MySQL persistence, and Spring Cache for score result caching. A custom scoring algorithm uses a non-linear weighted formula with sublinear exponents for earnings and jobs (rewarding early progress) and a supra-linear exponent for ratings (penalizing low ratings more heavily).

Core features include user registration/authentication, gig event logging with automatic platform data deduplication, incremental running average rating calculation, cross-user access control, and an AI-powered chat assistant integrated with Google Gemini API for conversational score guidance.

The frontend is built with React 19, Vite, and Tailwind CSS, providing a dashboard for key metrics, a gig logging interface, detailed score breakdown with formula visualization, and an AI chat widget. The application includes a structured error handling system with custom exception hierarchy and a global exception handler.

Testing covers the scoring engine (edge cases: zero values, max rating), gig data deduplication, and integration tests for user registration/login flow and cross-user access control.

### B) Portfolio Description

**GigScore** is a full-stack performance analytics platform for gig workers. Built with Spring Boot 4.0.4 and Java 21, it aggregates multi-platform gig data into a unified weighted score (0-100). Key engineering decisions include: a non-linear scoring algorithm with power-weighting to reward early progress and penalize low ratings, automatic platform data deduplication with weighted average calculation, JWT stateless authentication with BCrypt hashing, and Gemini AI integration for conversational guidance. The React frontend displays live metrics, score breakdowns, and activity feeds. Demonstrates proficiency in Spring Boot, JPA/Hibernate, REST API design, caching, AI integration, and secure authentication patterns.

### C) GitHub README Summary

# GigScore

A full-stack performance scoring system for gig economy workers. Track earnings, ratings, and job completions across platforms (Swiggy, Zomato, Uber, Rapido, Upwork) and compute a unified GigScore (0-100) using a weighted power-exponent formula.

## Features

- **User Authentication**: JWT-based registration and login with BCrypt password hashing
- **Gig Event Logging**: Record earnings and ratings per platform with automated rating recalculation
- **Platform Data Deduplication**: Automatic merging of duplicate platform records
- **GigScore Algorithm**: Weighted score formula (earnings 35%, jobs 25%, ratings 30%, active days 10%) with nonlinear power-weighting
- **Performance Dashboard**: Aggregated metrics with per-platform breakdowns
- **Activity Feed**: Recent gig event timeline
- **AI Chat Assistant**: Gemini-powered conversational guidance
- **Dark Mode**: Theme persistence with localStorage
- **OpenAPI Documentation**: Swagger UI at `/swagger-ui.html`

## Tech Stack

- **Backend**: Java 21, Spring Boot 4.0.4, Spring Security, Spring Data JPA, MySQL
- **Frontend**: React 19, Vite, Tailwind CSS, Axios
- **Auth**: JWT with HMAC-SHA256, BCrypt
- **AI**: Google Gemini API (gemini-2.5-flash)
- **Caching**: Spring Cache abstraction
- **API Docs**: SpringDoc OpenAPI

### D) Resume Version

**Project Title**: GigScore — Multi-Platform Gig Worker Performance Scoring System

**One-Line Description**: Full-stack web application with a custom weighted scoring algorithm that aggregates gig worker performance data across multiple platforms into a unified metric.

**Technologies**: Java 21, Spring Boot 4.0.4, Spring Security, Spring Data JPA, MySQL, React 19, Vite, Tailwind CSS, JWT, BCrypt, Gemini API, Mockito, JUnit 5

**Bullet Points**:

1. **Designed and implemented a nonlinear scoring algorithm** using power-weighting (exponents 0.65–1.40) across four weighted dimensions (earnings 35%, jobs 25%, ratings 30%, active days 10%) to compute a unified 0-100 performance score that rewards early progress and penalizes low ratings.

2. **Built a JWT-authenticated REST API** with Spring Security, implementing stateless authentication, BCrypt password hashing with backward-compatible plaintext migration, and resource-level access control preventing cross-user data access.

3. **Developed an automated data deduplication system** that detects and merges duplicate platform records using weighted average rating calculation, ensuring data consistency without manual intervention.

4. **Integrated Google Gemini API** for conversational AI chat guidance, implementing synchronous HTTP client communication with configurable model, temperature, and error handling for API failures.

5. **Achieved data integrity through Spring transaction management** with atomic operations across multiple entities (gig data updates, activity logging, score recalculation), and implemented Spring Cache for score result caching with strategic cache eviction.

---

## 14. INTERVIEW PREPARATION

### Beginner Questions

**Q1: What is the purpose of the `@SpringBootApplication` annotation?**

*Why interviewer asks it*: Tests understanding of Spring Boot entry point conventions.

*Strong answer*: It's a convenience annotation that combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`. In GigScore, it's placed on `GigscoreApplication` to enable Spring Boot auto-configuration, register beans from the `com.org.gigscore` package, and mark the class as a configuration source. The class also includes a `@PostConstruct` method that validates required environment variables (`jwt.secret` and `gemini.api.key`) during startup, failing fast if they're missing.

---

**Q2: What is the difference between `@Entity` and `@Table`?**

*Why interviewer asks it*: Tests JPA entity mapping fundamentals.

*Strong answer*: `@Entity` marks a class as a JPA entity (mapped to a database table). `@Table` is optional and allows customizing table name, schema, and constraints. In GigScore, `GigScore` uses `@Table(uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))` to enforce a one-to-one relationship between users and their scores at the database level, preventing duplicate score records.

---

**Q3: How does dependency injection work in your project?**

*Why interviewer asks it*: Tests understanding of Spring IoC.

*Strong answer*: We use constructor injection for all services. For example, `GigDataService` has five dependencies injected through its constructor: `GigDataRepository`, `UserRepository`, `GigScoreService`, `UserMetricsService`, and `ActivityService`. Spring automatically resolves and injects these beans. We prefer constructor injection over field injection because it makes dependencies explicit, enables proper unit testing with Mockito's `@InjectMocks`, and prevents null references.

---

**Q4: What is the `@Transactional` annotation used for?**

*Why interviewer asks it*: Tests understanding of transaction management.

*Strong answer*: `@Transactional` ensures that a method executes within a database transaction. In GigScore, `GigDataService.addGig()` is annotated with `@Transactional` because it performs multiple database operations atomically: updating the `GigData` entity, saving an `Activity` record, and recalculating the `GigScore`. If any step fails, all changes are rolled back, preventing data inconsistency.

---

**Q5: How does JWT authentication work in your project?**

*Why interviewer asks it*: Tests understanding of stateless authentication.

*Strong answer*: When a user registers or logs in, the server generates a JWT containing the user's email as the subject, signed with HMAC-SHA256 using a secret key, with a 1-hour expiration. The client stores this token in localStorage and sends it as a `Bearer` token in the `Authorization` header for all subsequent requests. The `JwtAuthFilter` (a `OncePerRequestFilter`) intercepts each request, validates the token by parsing and verifying the signature, extracts the email, and sets it in the `SecurityContext`. This allows the server to identify the user without maintaining server-side sessions.

---

### Intermediate Questions

**Q6: Explain the scoring algorithm in detail. Why did you choose sublinear exponents for earnings and jobs but a supra-linear exponent for ratings?**

*Why interviewer asks it*: Tests understanding of algorithm design decisions and mathematical reasoning.

*Strong answer*: The score is computed as: `(min(earnings/5000, 1)^0.70 * 35) + (min(jobs/100, 1)^0.65 * 25) + (min(rating/5, 1)^1.40 * 30) + (min(days/30, 1)^1.00 * 10)`. Each component is normalized to [0,1] by dividing by a target, then raised to an exponent, multiplied by a weight, and scaled to 100.

The sublinear exponents (0.70 for earnings, 0.65 for jobs) mean the function is concave — early progress yields more score per unit than later progress. For example, going from 0 to 10 jobs gives more points than going from 90 to 100 jobs. This makes the score approachable for new workers and prevents experienced workers from maxing out too easily.

The supra-linear exponent (1.40 for ratings) means the function is convex — higher ratings are disproportionately rewarded. A 4.5 rating scores significantly more than a 3.0. This incentivizes quality over quantity.

The neutral exponent (1.00 for active days) means a linear relationship — each day of activity contributes equally.

The weights (35% earnings, 25% jobs, 30% rating, 10% active days) prioritize financial performance and quality over pure volume.

---

**Q7: How does the platform data deduplication work? Why is it necessary?**

*Why interviewer asks it*: Tests understanding of data integrity and edge case handling.

*Strong answer*: The `resolveOrCreatePlatformAggregate()` method in `GigDataService` handles the case where multiple `GigData` rows exist for the same user and platform. This could happen due to race conditions, data migration issues, or legacy bugs. When detected, the method:

1. Sums total earnings, jobs completed, and active days across all duplicate rows
2. Computes a weighted average rating: `sum(avgRating * jobsCompleted) / sum(jobsCompleted)` — this correctly weights each row's rating by the number of jobs it represents
3. Updates the first row with merged values
4. Deletes all subsequent rows

This is necessary because the scoring algorithm assumes one aggregate record per platform. Without deduplication, the same platform's data would be double-counted, inflating the score.

---

**Q8: How do you prevent users from accessing each other's data?**

*Why interviewer asks it*: Tests understanding of authorization and security.

*Strong answer*: We use a `CurrentUserResolver` component that extracts the authenticated user's identity from the `SecurityContext`. When a controller receives a request with a `userId` path variable, it calls `currentUserResolver.getCurrentUserId()` and compares it with the requested ID. If they don't match, it throws `UnauthorizedAccessException`, which the `GlobalExceptionHandler` maps to HTTP 403 Forbidden.

The `CurrentUserResolver` works by reading the email from the JWT (stored in `Authentication.getName()` by `JwtAuthFilter`), looking up the corresponding `User` entity from the database, and returning the user's ID. This ensures that even if a user modifies the `userId` in a request, they can only access data associated with their own JWT.

---

**Q9: What caching strategy did you implement and why?**

*Why interviewer asks it*: Tests understanding of caching patterns and trade-offs.

*Strong answer*: We use Spring's `@Cacheable` and `@CacheEvict` annotations on the `GigScoreService`. The `getScoreForUser()` method is annotated with `@Cacheable("scores", key = "#user.userId")`, which caches the score result keyed by user ID. The `addGig()` method in `GigDataService` is annotated with `@CacheEvict(value = "scores", key = "#request.userId")`, which evicts the cached score for that user whenever a new gig is added.

This strategy ensures that:
- Dashboard and score page loads are fast because they hit the cache
- The score is always fresh because it's recalculated after any data change
- Cache invalidation is targeted (only the affected user's cache is cleared)

The current implementation uses Spring's default `ConcurrentHashMap` cache, which is suitable for single-instance deployments. For production with multiple instances, we would replace this with Redis.

---

**Q10: How does the legacy password migration work?**

*Why interviewer asks it*: Tests understanding of backward compatibility and security migration.

*Strong answer*: The `login()` method in `UserService` first attempts BCrypt verification using `passwordEncoder.matches()`. If that fails, it falls back to a direct string comparison with the stored password. If the plaintext comparison succeeds, it means the stored password is in legacy plaintext format. The method then re-hashes the password with BCrypt and saves the updated hash to the database.

This approach allows users with legacy plaintext passwords to log in without disruption, while transparently upgrading their credentials to BCrypt on first login. After the upgrade, all subsequent logins use BCrypt verification. This is a common migration pattern when upgrading password storage from plaintext to hashed.

---

### Advanced Questions

**Q11: The `GigDataControllerIntegrationTest` has a flawed `getUserIdFromToken()` method. What's wrong with it and how would you fix it?**

*Why interviewer asks it*: Tests ability to identify and fix testing anti-patterns.

*Strong answer*: The `getUserIdFromToken()` method creates a *new* user in the database and returns that user's ID, rather than extracting the user ID from the provided JWT token. This means the test `userA_cannot_access_userBData()` doesn't actually test that User A cannot access User B's real data — it tests that User A cannot access a dummy user's data.

To fix this, I would parse the JWT token to extract the email, then look up the user by email to get the actual user ID. Since we already have `JwtUtil` available in the application context, I would inject it into the test and use `jwtUtil.extractEmail(token)` to get the email, then query the database for the user. Alternatively, I could modify the registration endpoint to return the userId in the response, which it already does, and capture it directly from the registration response.

---

**Q12: The project imports `spring-boot-starter-websocket` but doesn't use it. How would you leverage WebSocket to improve the application?**

*Why interviewer asks it*: Tests ability to identify unused dependencies and propose meaningful improvements.

*Strong answer*: I would implement WebSocket-based real-time score updates. Currently, when a user adds a gig, the score is recalculated on the server, but the frontend only sees the updated score when it makes a new API call. With WebSocket, the server could push the updated score to the client immediately after recalculation.

I would configure a WebSocket endpoint (e.g., `/ws/score/{userId}`), and after `GigDataService.addGig()` recalculates the score, it would publish the new score to the user's WebSocket session. The frontend would subscribe to this endpoint and update the score display in real-time without polling.

Additionally, the AI Chat feature could use WebSocket for streaming responses from Gemini, showing the AI's reply token-by-token as it's generated, rather than waiting for the complete response.

---

**Q13: The scoring formula uses fixed targets ($5000, 100 jobs, 30 days). How would you make this configurable and extensible?**

*Why interviewer asks it*: Tests understanding of configuration management and extensibility patterns.

*Strong answer*: I would refactor the scoring formula to use the Strategy pattern. First, I'd extract the scoring parameters into `application.properties`:
```properties
gigscore.score.earnings-target=5000
gigscore.score.jobs-target=100
gigscore.score.rating-max=5.0
gigscore.score.active-days-target=30
gigscore.score.weight.earnings=0.35
gigscore.score.weight.jobs=0.25
gigscore.score.weight.rating=0.30
gigscore.score.weight.active-days=0.10
gigscore.score.exponent.earnings=0.70
gigscore.score.exponent.jobs=0.65
gigscore.score.exponent.rating=1.40
gigscore.score.exponent.active-days=1.00
```

Then I'd create a `ScoreConfig` class annotated with `@ConfigurationProperties("gigscore.score")` to bind these properties. The `GigScoreService` would inject this config instead of using hardcoded constants.

For extensibility, I'd define a `ScoreComponent` interface:
```java
public interface ScoreComponent {
    double calculate(UserAggregateMetrics metrics);
    String getName();
}
```
Each dimension (earnings, jobs, rating, activeDays) would implement this interface. New dimensions could be added by implementing the interface and registering the bean. The total score would be the sum of all `ScoreComponent` beans.

---

**Q14: How would you handle the Gemini API call being slow or failing? What improvements would you make?**

*Why interviewer asks it*: Tests understanding of resilience patterns and external API integration.

*Strong answer*: Currently, the Gemini API call is synchronous and blocks the request thread. I would make several improvements:

1. **Add timeout configuration**: Set connect timeout (5s), request timeout (30s), and read timeout (30s) on the `HttpClient` to prevent thread starvation.

2. **Implement circuit breaker**: Use Resilience4j's `@CircuitBreaker` to detect when Gemini is unavailable and fail fast instead of waiting for timeouts.

3. **Add retry with backoff**: Use `@Retry` with exponential backoff for transient failures (5xx errors).

4. **Make the call asynchronous**: Use `@Async` with a `CompletableFuture` to free up the request thread. The frontend could poll for the result or receive it via WebSocket.

5. **Cache common responses**: Cache responses for frequently asked questions to reduce API calls.

6. **Add fallback responses**: If Gemini is unavailable, return a cached or default response instead of an error.

---

**Q15: The `User` entity's `password` field is nullable. What risks does this pose and how would you fix it?**

*Why interviewer asks it*: Tests attention to detail and understanding of data integrity.

*Strong answer*: A nullable password field means a user could be created without a password, or a password could be set to null. This poses several risks:
- A user with a null password could log in without providing a password (the `isBlank()` check in `UserService.login()` would skip BCrypt verification, and the plaintext comparison would fail, but the behavior is undefined)
- Database-level integrity is not enforced
- The `CreateUserRequest` DTO has `@NotBlank` on password, but this is only validated at the controller level — direct repository operations could bypass this

To fix this, I would:
1. Add `@Column(nullable = false)` to the `password` field in the `User` entity
2. Add a `@NotEmpty` validation annotation on the password field in the DTO
3. Add a database migration (Flyway or Liquibase) to add the NOT NULL constraint to existing data, setting a default for any null passwords (or requiring users to reset them)

---

## 15. FINAL RULE

This analysis is based entirely on the actual codebase at commit `528039b109f6be6b1f554acc2192dda810c96c98`. No features, metrics, or capabilities have been invented. All claims are verifiable from the source code. The project demonstrates intermediate-level Spring Boot development with a well-designed scoring algorithm, proper authentication, and practical AI integration, while having clear areas for improvement in testing coverage, production readiness, and advanced Java features.
# Phase 4 — Security Audit

Date: 2026-09-20  
Auditor: Application Security Reviewer & Backend Architect  
Target Repository: `D:\Java\Spring_boot\GigScore`

---

## 1. Vulnerability Findings Log

All findings are verified directly from code, configuration, dependency trees, and Git history.

| ID | Severity | Location | Vulnerability | How It Could Be Abused | Correct Fix | Effort | Priority |
|---|---|---|---|---|---|---|---|
| **SEC-01** | **Critical** | Git History (commit `6b35e0c` / `99766ad`, `backend/gigscore/src/main/resources/application.properties:4`) | Hardcoded Database Password in Git Commit History | Anyone with read access to the GitHub repository can retrieve the plaintext database credential (`spring.datasource.password=<REDACTED>`) and compromise the database. | 1. Immediately rotate the MySQL database password.<br>2. Purge commit history using `git-filter-repo` or BFG Repo-Cleaner.<br>3. Verify `.env` remains in `.gitignore`. | 30 mins | **P0** |
| **SEC-02** | **High** | `backend/gigscore/src/main/java/com/org/gigscore/service/LenderService.java:37-41` | Unconsented Worker Data Exposure (BOLA / Privacy Violation) | Any user registered or seeded as `LENDER` can call `GET /api/v1/lender/workers` and `GET /api/v1/lender/workers/{userId}` to scrape every worker's name, email, earnings, score, active days, and recent activity with zero consent from the worker. | 1. Implement a `ConsentGrant` entity.<br>2. Workers must explicitly issue a time-limited consent token or link.<br>3. Restrict lender queries to consented workers only (`WHERE grant.lenderId = :id AND grant.expiresAt > NOW()`). | 3 hours | **P0** |
| **SEC-03** | **High** | `frontend/src/App.jsx:117-123` & `frontend/src/services/httpClient.js:8` | JWT Stored in Browser `localStorage` (XSS Exposure) | If an attacker achieves XSS execution (e.g. through a vulnerable NPM dependency or malicious review text), they can read `localStorage.getItem("gigscoreToken")` and exfiltrate user credentials. | Store JWT in an `httpOnly`, `Secure`, `SameSite=Strict` cookie. Remove token handling from JavaScript. | 2 hours | **P1** |
| **SEC-04** | **High** | `backend/gigscore/src/main/java/com/org/gigscore/service/GeminiChatService.java:62-63` | Google API Key Exposed in Outbound URL Query String | The Gemini API key is appended to the URL: `...:generateContent?key=...`. HTTP proxies, firewalls, and gateway logs record full URLs, leaking the private key into plain-text logs. | Pass the API key in the `x-goog-api-key` HTTP header rather than in the URL query string. | 15 mins | **P0** |
| **SEC-05** | **Medium** | `backend/gigscore/src/main/java/com/org/gigscore/service/UserService.java:69-70` | Account Enumeration via Login Timing Side-Channel | If an email does not exist, login returns 401 in ~2ms. If the email exists, BCrypt verification runs, taking ~150ms. Attackers can enumerate registered users by measuring response latency. | Execute a dummy BCrypt hash comparison when the user is not found to ensure constant-time response latency. | 30 mins | **P1** |
| **SEC-06** | **Medium** | `backend/gigscore/src/main/java/com/org/gigscore/controller/GigScoreController.java:42-49` | State Mutation Side-Effect on Idempotent GET Request | Calling `GET /api/v1/score/{userId}` invokes `calculateAndPersistScore()`, which writes a new row to `score_history` on cache misses. A script repeatedly calling `GET` can flood the database with millions of identical history records. | Enforce that `GET` endpoints are strictly read-only. Persist score snapshots only on gig mutations or scheduled sync jobs. | 1 hour | **P1** |
| **SEC-07** | **Medium** | `backend/gigscore/src/main/java/com/org/gigscore/service/GigDataService.java:81-97` | Race Condition in Platform Aggregate Ingestion | `GigData` lacks a database `UNIQUE(user_id, platform)` constraint. Concurrent gig submissions for the same platform execute parallel `SELECT`s, bypass in-memory deduplication, and create duplicate rows. | Add a database unique constraint on `(user_id, platform)` and use atomic SQL/JPA upserts. | 1 hour | **P1** |
| **SEC-08** | **Medium** | `backend/gigscore/src/main/java/com/org/gigscore/service/LenderService.java:37` | Denial of Service via Unpaginated Full-Table Memory Hydration | `getAllWorkers()` calls `userRepository.findAll()` and hydrides every user, gig data record, and score in the database into the JVM heap. Repeated requests by a lender exhaust server memory. | Add pagination (`Pageable`) with max page size 50 and use a single projection query with `JOIN`. | 45 mins | **P1** |
| **SEC-09** | **Low** | `backend/gigscore/src/main/java/com/org/gigscore/config/SecurityConfig.java:67` | Public Swagger UI & OpenAPI Spec Exposure | Swagger UI (`/swagger-ui/**`) and OpenAPI schema (`/v3/api-docs/**`) are publicly accessible without authentication in all environments, exposing API routes and DTO structures to reconnaissance. | Restrict Swagger/OpenAPI endpoints to the `dev` Spring profile or require `ROLE_ADMIN`. | 30 mins | **P2** |
| **SEC-10** | **Low** | `frontend/package.json` | 6 High-Severity CVEs in Frontend Dependency Tree | `npm audit` reports 10 vulnerabilities (GHSA-qwww-vcr4-c8h2 in `react-router`, GHSA-c83g-rgw3-j3cx in `browserslist`, GHSA-mh99-v99m-4gvg in `brace-expansion`). | Run `npm audit fix` and upgrade `react-router-dom` to the latest patched minor release. | 20 mins | **P2** |

---

## 2. In-Depth Security Domain Analysis

### 2.1 Authentication & Password Security
* **Password Hashing:** Implemented via `BCryptPasswordEncoder` in `SecurityConfig.java:39`. Password hashing strength is sound.
* **Password Policy:** Jakarta Bean Validation requires `@Size(min = 6)` on `CreateUserRequest.java:18`. For fintech-grade security, password complexity should require at least 8 characters with digit and symbol requirements.
* **Legacy Plaintext Backdoor:** In `UserService.java:86-90`:
  ```java
  if (!authenticated && rawPassword.equals(storedPassword)) {
      authenticated = true;
      user.setPassword(passwordEncoder.encode(rawPassword));
      userRepository.save(user);
  }
  ```
  While intended as an auto-upgrade mechanism for legacy data, having an explicit plaintext comparison in an active authentication service is a code-smell that interviewers will flag.

### 2.2 Authorization & Role-Based Access Control (RBAC)
* **Default-Deny:** `SecurityConfig.java:70` enforces `.anyRequest().authenticated()`.
* **Privilege Escalation:** Safe. `CreateUserRequest` does not expose a `role` field; `UserService.java:47` explicitly hardcodes `user.setRole(UserRole.WORKER)`. Users cannot elevate themselves to `LENDER` or `ADMIN` during registration.
* **Lender Role Seeding:** Implemented via `LenderDataSeeder.java` checking environment variables `DEMO_LENDER_EMAIL` and `DEMO_LENDER_PASSWORD`.
* **IDOR / BOLA Prevention on Worker Endpoints:**
  * `UserController.java:43`: verifies `currentUserResolver.getCurrentUserId().equals(userId)`.
  * `GigDataController.java:31`: overrides request `userId` with `currentUserResolver.getCurrentUserId()`.
  * `GigScoreController.java:43, 60`: verifies `currentUserResolver.getCurrentUserId().equals(userId)`.
  * `ActivityController.java:37, 46`: verifies `currentUserResolver.getCurrentUserId().equals(userId)`.
  * *Verdict:* Worker endpoints are thoroughly protected against IDOR.

### 2.3 Secrets & Configuration Exposure
* **Git History Check:** Commit `6b35e0c` previously contained `spring.datasource.password=<REDACTED>` in `application.properties`. This credential must be considered compromised.
* **Environment Parameterization:** Current `application.properties` cleanly parameterizes `spring.datasource.password=${DB_PASSWORD:root}`, `jwt.secret=${JWT_SECRET}`, and `gemini.api.key=${GEMINI_API_KEY:}`.
* **Spring Actuator:** `management.endpoints.web.exposure.include=health,info` with `management.endpoint.health.show-details=never`. Sensitive actuator endpoints (e.g. `/actuator/env`, `/actuator/beans`, `/actuator/heapdump`) are disabled.

### 2.4 Rate Limiting & Denial of Service
* **Filter Implementation:** `RateLimitFilter.java` implements an in-memory sliding window using `ConcurrentHashMap` and thread-safe timestamp queues.
* **Thresholds:**
  * Login: 10 requests / minute per client IP.
  * AI Chat: 20 requests / minute per client IP.
* **Response:** Returns HTTP 429 Too Many Requests with `Retry-After: 60` and standard JSON error response via `ErrorResponseWriter`.

---

## 3. UI-Driven Security Surface (Target System Design)

When implementing the target UI features from Phase 5, the following attack vectors must be defended by design:

### 3.1 Platform OAuth & Credential Storage
* The target UI displays "Connect Platform" for Upwork, Uber, Fiverr, TaskRabbit.
* **Requirement:** Never store third-party user passwords. If storing OAuth access/refresh tokens from sandbox or mock providers, encrypt them at rest using **AES-256-GCM** with a dedicated encryption key. Ensure tokens are annotated with `@JsonIgnore` and never returned in API responses or logged.

### 3.2 Signed Score Credential & Public Verification Endpoint
* Target UI shows: "Cryptographically Verified", "Copy Verification Link", "Download Signed Credential (PDF)", and Verified Hash `#9fa2-842`.
* **Cryptographic Architecture:**
  1. Produce canonical JSON: `{"userId":1,"score":842,"tier":"Tier 1 Elite","issuedAt":"2026-09-20T12:00:00Z","expiresAt":"2026-10-20T12:00:00Z","version":2}`.
  2. Compute HMAC-SHA256 signature using a server-side signing secret: `signature = HMAC-SHA256(canonicalJson, signingKey)`.
  3. Verification Link: `https://gigscore.io/verify/{token}` where token is base64url-encoded `{payload}.{signature}`.
  4. **Minimum Disclosure Policy:** `GET /api/v1/verify/{token}` must return only public credibility data (Score, Tier, Issue Date, Expiration Date). It must **never leak** worker email, full name, bank accounts, or individual gig transaction logs.

### 3.3 CSV Formula Injection (Export Ledger CSV)
* Target UI includes "Export ledger CSV" on the dashboard.
* **Vulnerability:** If platform names, job descriptions, or client review quotes contain `=CMD|' /C ...'!A0`, `+`, `-`, or `@`, opening the CSV in Microsoft Excel or Google Sheets executes external system commands.
* **Requirement:** In the CSV export serializer, any field starting with `=`, `+`, `-`, or `@` must be prepended with a single quote (`'`) to neutralize formula execution.

### 3.4 Stored XSS in Activity Feeds
* Target UI Recent Activity displays review text: `“Exceptional communication and delivery speed, clean handoff!”`.
* **Requirement:** Ensure React renders text inside standard JSX nodes (`<p>{review.text}</p>`), which applies automatic HTML entity escaping. Strictly prohibit `dangerouslySetInnerHTML`.

---

## 4. Top 10 Security Fixes Ranked

1. **Rank 1 (P0): Rotate MySQL Password & Scrub Git History (SEC-01)**
   * Eliminates the active hardcoded database credential found in repository history.
2. **Rank 2 (P0): Implement Worker Consent Model for Lender Access (SEC-02)**
   * Closes the massive BOLA/privacy leak where any lender can scrape all users.
3. **Rank 3 (P0): Move Gemini API Key to Request Header (SEC-04)**
   * Prevents API key leakage in intermediate proxy and gateway URL logs.
4. **Rank 4 (P1): Fix GET /api/v1/score/{userId} State Mutation (SEC-06)**
   * Restores REST idempotency and stops duplicate history spam on cache misses.
5. **Rank 5 (P1): Migrate Frontend JWT to HttpOnly Cookies (SEC-03)**
   * Eliminates token theft via client-side XSS.
6. **Rank 6 (P1): Add Database UNIQUE Constraint on (user_id, platform) (SEC-07)**
   * Solves concurrency race conditions in platform aggregation.
7. **Rank 7 (P1): Implement Constant-Time BCrypt Check on Login (SEC-05)**
   * Prevents user account enumeration through response time side-channels.
8. **Rank 8 (P1): Paginate Lender Worker List (SEC-08)**
   * Prevents denial-of-service via unbounded JVM heap allocation.
9. **Rank 9 (P2): Run `npm audit fix` on Frontend Dependencies (SEC-10)**
   * Patches known high-severity CVEs in the React build tree.
10. **Rank 10 (P2): Restrict Swagger UI / OpenAPI Spec in Production (SEC-09)**
    * Reduces public reconnaissance footprint.

---

## 5. Quick Wins (Under 1 Hour)

* **Win 1 (15 mins) — Header-Based Gemini Auth:**
  Change `GeminiChatService.java:63` from appending `?key=` in the URL to `.header("x-goog-api-key", apiKey)`.
* **Win 2 (30 mins) — Login Timing Equalization:**
  In `UserService.java:70`, if `userRepository.findByEmail()` is empty, execute `passwordEncoder.matches("dummy", "$2a$10$dummyHash...")` before throwing `UnauthorizedException`.
* **Win 3 (20 mins) — Frontend Vulnerability Patch:**
  Run `npm audit fix` inside the `frontend/` directory to automatically update vulnerable sub-dependencies.
* **Win 4 (30 mins) — Remove Plaintext Password Fallback:**
  Delete lines 84–90 in `UserService.java` to eliminate the plaintext password comparison code smell.

# Phase 1 — Actual System Inventory & Feature Matrix

Date: 2026-09-20  
Auditor: Senior Software Engineer & Security Reviewer  
Target Repository: `D:\Java\Spring_boot\GigScore`

---

## 1. System Inventory

This inventory documents what **actually exists** in the codebase based on strict code inspection, distinguishing between complete implementation, partial logic, demo mocks, and missing features.

### 1.1 Authentication & Authorization
* **Status:** 🟡 PARTIAL
* **Locations:**
  * Backend: `com.org.gigscore.controller.UserController`, `com.org.gigscore.service.UserService`, `com.org.gigscore.config.SecurityConfig`, `com.org.gigscore.config.JwtUtil`, `com.org.gigscore.config.JwtAuthFilter`, `com.org.gigscore.entity.User`, `com.org.gigscore.entity.UserRole`
  * Frontend: `frontend/src/pages/Login.jsx`, `frontend/src/pages/CreateUser.jsx`, `frontend/src/services/userService.js`, `frontend/src/services/httpClient.js`
  * Database: `user` table (`user_id`, `email`, `password`, `name`, `role`)
* **How It Works:**
  * Registration (`POST /api/v1/users`) validates email/password, checks uniqueness, hashes password using `BCryptPasswordEncoder`, sets `role = WORKER`, and returns a JWT token.
  * Login (`POST /api/v1/users/login`) authenticates credentials and issues an HMAC-SHA256 JWT expiring in 1 hour.
  * Stateless security filter chain verifies JWT on requests, populating `SecurityContextHolder` with `ROLE_WORKER` or `ROLE_LENDER`.
  * Demo lender account is seeded on startup via environment variables (`LenderDataSeeder`).
* **What Is Missing:**
  * No refresh token flow (token hard-expires after 60 minutes).
  * Tokens stored in browser `localStorage` (vulnerable to XSS).
  * No password reset or email verification workflow.
  * No worker consent mechanism for lender data sharing.
* **Quality & Verdict:** **KEEP & HARDEN**. Solid Spring Security baseline for a fresher; needs cookie-based auth and consent scoping.

---

### 1.2 Multi-Platform Work Ingestion (Add Gig)
* **Status:** 🟡 PARTIAL
* **Locations:**
  * Backend: `com.org.gigscore.controller.GigDataController`, `com.org.gigscore.service.GigDataService`, `com.org.gigscore.util.Platform`, `com.org.gigscore.util.Money`
  * Frontend: `frontend/src/pages/AddGig.jsx`, `frontend/src/components/PlatformCard.jsx`, `frontend/src/services/gigService.js`
  * Database: `gig_data` table (`gig_id`, `user_id`, `platform`, `total_earnings`, `jobs_completed`, `avg_rating`)
* **How It Works:**
  * Worker selects a platform card (e.g., Upwork, Uber) and enters amount and rating (1.0–5.0).
  * `POST /api/v1/gigs` validates the platform name against `Platform` enum, updates running totals (`totalEarnings`, `jobsCompleted`, `avgRating`) in `gig_data`, writes an audit log in `activity`, and recalculates `GigScore`.
* **What Is Missing:**
  * No automated platform integration, webhook, or OAuth connector.
  * Does not store granular transactions or external IDs (`external_transaction_id`).
  * Ingestion is entirely self-reported with no idempotency guarantees against re-submitting duplicate gigs.
* **Quality & Verdict:** **IMPROVE**. Replace manual form submissions with a sandbox connector abstraction supporting idempotent ingestion of timestamped deliveries and transactions.

---

### 1.3 Platform Integrations & Sync Protocol
* **Status:** ⚪ UI/DEMO ONLY
* **Locations:**
  * Backend: `com.org.gigscore.controller.PlatformController` (`GET /api/v1/platforms` returns static string list)
  * Frontend: `frontend/src/pages/AddGig.jsx` renders static platform cards with "Connected" pills
* **How It Works:**
  * The frontend displays 10 platform cards; clicking one toggles the manual gig entry modal.
* **What Is Missing:**
  * The target UI's "Node Sync Protocol v2.4", "One-click OAuth", "Sync now", "Auto-sync toggle", and "Refresh All" buttons do not exist on the backend.
  * No token storage, credential encryption, or background sync schedulers exist.
* **Quality & Verdict:** **REPLACE WITH SANDBOX CONNECTORS**. Real third-party gig APIs (Uber, DoorDash, Upwork) are strictly enterprise-restricted. Implement a mock connector interface backed by deterministic seeded sandbox generators.

---

### 1.4 Scoring Engine
* **Status:** 🟡 PARTIAL
* **Locations:**
  * Backend: `com.org.gigscore.service.GigScoreService`, `com.org.gigscore.dto.ScoreConfig`, `com.org.gigscore.entity.GigScore`
  * Frontend: `frontend/src/pages/Score.jsx`, `frontend/src/utils/scoreFormula.js`
  * Database: `gig_score` table (`gig_score_id`, `user_id`, `score`, `created_at`, `updated_at`)
* **How It Works:**
  * Calculates score $S \in [0, 100]$ using power-weighted exponential normalization:
    $$S = \left(\frac{E}{5000}\right)^{0.70} \cdot 35 + \left(\frac{J}{100}\right)^{0.65} \cdot 25 + \left(\frac{R}{5}\right)^{1.40} \cdot 30 + \left(\frac{D}{30}\right)^{1.00} \cdot 10$$
  * Formula parameters and targets are exposed via `GET /api/v1/score/config`.
  * Results are cached in Spring's in-memory cache manager (`@Cacheable(value = "scores")`) and evicted upon new gig submission.
* **What Is Missing:**
  * The target design specifies **6 factor diagnostics** (Reliability 25%, Customer Rating 20%, Completion Rate 20%, Response Time 15%, Consistency 10%, Earnings Growth 10%) on a **0-1000** scale (or 0-900).
  * Current engine only calculates 4 components on a 0-100 scale.
  * Inputs are vulnerable to gaming: wash income or rapid duplicate entries inflate the score without penalty.
* **Quality & Verdict:** **IMPROVE**. Align the scoring engine to the 6 target factors, scale to 0-1000, and enforce explainability-by-construction.

---

### 1.5 Score History & Trajectory Tracking
* **Status:** 🟡 PARTIAL
* **Locations:**
  * Backend: `com.org.gigscore.entity.ScoreHistory`, `com.org.gigscore.repository.ScoreHistoryRepository`, `com.org.gigscore.controller.GigScoreController` (`GET /api/v1/score/{userId}/history`)
  * Frontend: `frontend/src/components/ScoreHistoryChart.jsx`, `frontend/src/pages/Score.jsx`
  * Database: `score_history` table (`id`, `user_id`, `score`, `score_version`, `recorded_at`)
* **How It Works:**
  * Each call to `GigScoreService.calculateAndPersistScore()` inserts an immutable row into `score_history`.
  * Frontend fetches history and renders a Recharts `<LineChart />` with points T1, T2, etc.
* **What Is Missing:**
  * **Defect:** Every cache miss on `GET /api/v1/score/{userId}` triggers `calculateAndPersistScore()`, creating duplicate history entries even when the score has not changed.
  * Does not store factor contribution breakdowns with the snapshot.
  * Trajectory chart lacks the target UI's milestone annotations ("Aug Milestone", "Record 5-star streak") and time-range tabs (7D/30D/3M/1Y).
* **Quality & Verdict:** **IMPROVE**. Deduplicate snapshot insertions (only save on actual score delta or scheduled sync) and store factor snapshots.

---

### 1.6 Score Explainability & Recommendations
* **Status:** 🟡 PARTIAL
* **Locations:**
  * Backend: `GigScoreService.suggestionsFor()`, `GigScoreService.bandFor()`, `ScoreResponse`
  * Frontend: `Score.jsx` (Score Breakdown table and "What to improve" list)
* **How It Works:**
  * Returns exact component point values ($E, J, R, D$).
  * Categorizes score into bands: `Needs Improvement` (<50), `Fair` (50-74), `Strong` (75-89), `Excellent` (≥90).
  * Generates rule-based suggestion strings if any component score is below a fixed threshold.
* **What Is Missing:**
  * Suggestions are static string templates; no dynamic "what-if" simulator (e.g., "Improving response time to <15m adds +12 pts").
  * Does not explain positive vs negative performance drivers.
* **Quality & Verdict:** **IMPROVE**. Build a lightweight what-if recalculator and risk driver summary.

---

### 1.7 Data Quality Profiling & Confidence
* **Status:** 🟡 PARTIAL
* **Locations:**
  * Backend: `com.org.gigscore.dto.DataQualityProfile`, `com.org.gigscore.dto.ConfidenceLevel`, `GigScoreService.computeDataQuality()`
  * Frontend: Rendered in `Lenders.jsx` table (badges: HIGH / MEDIUM / LOW)
* **How It Works:**
  * Evaluates volume: `< 5 gigs` or `< 3 days` = `LOW`; `< 20 gigs` or `< 10 days` = `MEDIUM`; else `HIGH`.
* **What Is Missing:**
  * No recency decay analysis, no platform diversity check, no duplicate/anomaly detection.
* **Quality & Verdict:** **IMPROVE**. Keep explainable rules, but add basic sanity checks (impossible hourly earnings, suspicious repeat timestamps).

---

### 1.8 Activity Feed & Transaction Logging
* **Status:** 🟡 PARTIAL
* **Locations:**
  * Backend: `com.org.gigscore.controller.ActivityController`, `com.org.gigscore.service.ActivityService`, `com.org.gigscore.entity.Activity`
  * Frontend: `Dashboard.jsx` (renders recent 5 items)
  * Database: `activity` table (`id`, `user_id`, `platform`, `action`, `amount`, `rating`, `timestamp`)
* **How It Works:**
  * Adding a gig writes an activity row.
  * `GET /api/v1/activity/{userId}?page=0&size=5` supports paginated retrieval sorted descending by timestamp.
* **What Is Missing:**
  * No dedicated "Activity" / Ledger page in the frontend navigation (target UI links to "View full ledger" and "Export ledger CSV").
  * No CSV export endpoint exists.
* **Quality & Verdict:** **IMPROVE**. Add full Activity page and injection-safe CSV ledger export.

---

### 1.9 Lender Portal & Decision Support
* **Status:** 🟡 PARTIAL
* **Locations:**
  * Backend: `com.org.gigscore.controller.LenderController`, `com.org.gigscore.service.LenderService`, `com.org.gigscore.dto.LenderWorkerSummary`, `com.org.gigscore.dto.LenderWorkerDetail`
  * Frontend: `frontend/src/pages/Lenders.jsx`
* **How It Works:**
  * Endpoints `/api/v1/lender/**` are restricted to `ROLE_LENDER` in `SecurityConfig`.
  * Returns all users in the system ranked by GigScore with earnings, active days, and confidence level.
  * Clicking worker details retrieves breakdown and recent activities.
* **What Is Missing:**
  * **Critical Privacy Gap:** Lenders have unconsented global visibility into every user registered in the system.
  * Performance flaw: `LenderService.getAllWorkers()` calls `userRepository.findAll()` and computes aggregates for every user sequentially in memory without pagination.
  * No lender-facing screen exists in the target design package.
* **Quality & Verdict:** **IMPROVE**. Add explicit worker consent grants (`ConsentGrant` entity) and pagination.

---

### 1.10 AI Assistant (Gemini)
* **Status:** 🟡 PARTIAL
* **Locations:**
  * Backend: `com.org.gigscore.controller.ChatController`, `com.org.gigscore.service.GeminiChatService`
  * Frontend: `frontend/src/components/AiChatWidget.jsx`, `frontend/src/services/chatService.js`
* **How It Works:**
  * Floating widget sends chat history to backend. Backend proxies messages to Google Gemini API (`gemini-2.5-flash`) via `java.net.http.HttpClient`.
  * Gracefully fails with 503 error response when `GEMINI_API_KEY` is not configured.
* **What Is Missing:**
  * Security risk: API key is passed in URL query parameters (`?key=...`) rather than `x-goog-api-key` header.
  * Context-unaware: Backend sends raw client messages with no system prompt and no worker telemetry/score context.
* **Quality & Verdict:** **HARDEN & ENRICH**. Move API key to request header and inject user performance summaries into the system prompt.

---

### 1.11 Rate Limiting & Access Security
* **Status:** ✅ COMPLETE
* **Locations:**
  * Backend: `com.org.gigscore.security.RateLimitFilter`, `com.org.gigscore.config.SecurityConfig`
* **How It Works:**
  * In-memory sliding-window request limiter keyed by client IP.
  * Restricts `/api/v1/users/login` (default 10 req/min) and `/api/v1/chat/ask` (default 20 req/min).
  * Returns HTTP 429 with `Retry-After: 60`.
* **Quality & Verdict:** **KEEP — DO NOT OVERENGINEER**. Excellent fresher-level security implementation.

---

### 1.12 Financial Precision & Money Handling
* **Status:** ✅ COMPLETE
* **Locations:**
  * Backend: `com.org.gigscore.util.Money`, entities `GigData.totalEarnings`, `Activity.amount`
* **How It Works:**
  * `Money` class enforces `BigDecimal` scale 2 with `RoundingMode.HALF_UP`.
  * Database schema uses `DECIMAL(12,2)`. No raw floating-point calculations for monetary values.
* **Quality & Verdict:** **KEEP — DO NOT OVERENGINEER**. Mathematically sound; meets fintech standards.

---

### 1.13 Target UI Alignment (Dashboard, Platforms, GigScore, Insights)
* **Status:** ❌ NOT IMPLEMENTED
* **Details:**
  * Target UI screens (`01-dashboard.png`, `02-platforms.png`, `03-gigscore.png`, `04-insights.png`) have distinct component requirements (score ring, platform cards with sync metrics, hourly surge breakdown, signed credentials) that do not match the existing legacy frontend.
* **Verdict:** **BUILD IN PHASES**. Rebuild frontend layout and views to match target designs backed by real API data.

---

### 1.14 Public Credential Verification & Signed Proof
* **Status:** ❌ NOT IMPLEMENTED
* **Details:**
  * UI claims "Public Ledger Status · Cryptographically Verified", "Copy Verification Link", "Download Signed Credential (PDF)".
  * No backend endpoints or cryptographic hashing/signing logic exist in the codebase.
* **Verdict:** **BUILD (SMALL)**. Implement HMAC-SHA256 score snapshot signing, a public verification endpoint `GET /api/v1/verify/{hash}`, and PDF generation.

---

## 2. Feature Matrix

| Feature | Status | Frontend | Backend | Database | Quality (with WHY) | Interview Value | Keep / Improve / Remove |
|---|---|---|---|---|---|---|---|
| **User Auth & JWT** | 🟡 PARTIAL | `Login.jsx`, `CreateUser.jsx` | `UserController`, `JwtAuthFilter` | `user` table | **Decent:** Clean BCrypt + stateless filter chain, but tokens stored in localStorage and no refresh tokens. | **High** (Standard backend requirement) | **Improve** (HttpOnly cookies, refresh token) |
| **RBAC (Worker vs Lender)** | 🟡 PARTIAL | Route guards in `App.jsx` | `hasRole('LENDER')` in `SecurityConfig` | `role` column | **Solid:** Enforces 403 on lender APIs; but demo lender seeded via env var rather than DB admin flow. | **High** (Shows auth understanding) | **Keep & Harden** |
| **Gig Data Ingestion** | 🟡 PARTIAL | `AddGig.jsx` manual form | `GigDataService.addGig` | `gig_data` table | **Weak:** Merely increments counters; lacks individual delivery records and external idempotency IDs. | **Medium** (Basic CRUD) | **Improve** (Canonical delivery entity, sandbox connectors) |
| **Platform Connectors** | ⚪ UI/DEMO | Static platform cards | Static enum endpoint | None | **Superficial:** Cards are dummy triggers for manual form; no real sync or sandbox simulation. | **High if honest** | **Improve** (Sandbox connector abstraction) |
| **Scoring Engine** | 🟡 PARTIAL | `scoreFormula.js` mirror | `GigScoreService` | `gig_score` table | **Moderate:** Clean non-linear formula, but only 4 factors on 0-100 scale; does not match target UI's 6 factors. | **Very High** (Core domain logic) | **Improve** (6 factors, 0-1000 scale, explainable by construction) |
| **Score History & Chart** | 🟡 PARTIAL | Recharts `<LineChart />` | `ScoreHistoryRepository` | `score_history` table | **Weak:** Persists duplicate snapshots on read cache misses; chart uses T1, T2 intervals instead of dates. | **Medium** | **Improve** (Snapshot on delta only, date ranges) |
| **Score Explainability** | 🟡 PARTIAL | Breakdown table in `Score.jsx` | `suggestionsFor` | None | **Basic:** Shows formula math, but suggestions are hardcoded string heuristics; no what-if analysis. | **High** | **Improve** (What-if simulator, risk drivers) |
| **Data Quality Profiling** | 🟡 PARTIAL | Badge in `Lenders.jsx` | `ConfidenceLevel` | None | **Basic:** Volume check only (jobs & days count); lacks recency decay or anomaly detection. | **High** | **Improve** (Explainable anomaly rules) |
| **Activity Feed** | 🟡 PARTIAL | 5 items on `Dashboard.jsx` | Paged `ActivityController` | `activity` table | **Partial:** Backend supports pagination; frontend has no dedicated full page or export. | **Medium** | **Improve** (Activity ledger page + CSV export) |
| **Lender View** | 🟡 PARTIAL | `Lenders.jsx` table | `LenderService` | `user` table | **Insecure & Inefficient:** Unconsented data leakage of all workers; unpaginated full table scan in memory. | **High** | **Improve** (Worker consent grant, pagination) |
| **Gemini AI Chat** | 🟡 PARTIAL | `AiChatWidget.jsx` | `GeminiChatService` | None | **Weak:** Exposes API key in URL query parameter; no user context or system prompt injected. | **Medium** | **Harden** (Header auth, performance context injection) |
| **Rate Limiting** | ✅ COMPLETE | Handles 429 | `RateLimitFilter` | In-memory | **High Quality:** Sliding window algorithm, well unit-tested, returns standard Retry-After headers. | **Very High** (Shows resilience engineering) | **KEEP — DO NOT OVERENGINEER** |
| **Money Handling** | ✅ COMPLETE | Formatted in UI | `Money.java`, `BigDecimal` | `DECIMAL(12,2)` | **High Quality:** Zero float/double leaks in currency fields; explicit scale and rounding. | **High** (Critical fintech skill) | **KEEP — DO NOT OVERENGINEER** |
| **Target UI Re-skin** | ❌ NOT IMPLEMENTED | None | None | None | **Missing:** High-density precision UI design from Stitch AI is not yet built. | **Very High** (Visual credibility) | **Build in Slices** |
| **Signed Credential / Verifier** | ❌ NOT IMPLEMENTED | None | None | None | **Missing:** UI claims cryptographically verified credentials, but no signing or verification endpoints exist. | **Very High** (Defensible alternative to fake blockchain) | **Build (Small)** (HMAC/SHA-256 + public verify endpoint) |

---

## 3. Domain-Flow Check

### Target Architectural Flow vs Reality

$$\text{Gig Worker} \xrightarrow{(1)} \text{Profile/Financial Data} \xrightarrow{(2)} \text{Income/Work Data} \xrightarrow{(3)} \text{Data Validation} \xrightarrow{(4)} \text{Reliability Analysis} \xrightarrow{(5)} \text{GigScore} \xrightarrow{(6)} \text{Score Explanation} \xrightarrow{(7)} \text{Lender Decision Support}$$

| Step | Transition | Status | Verification Evidence |
|---|---|---|---|
| **(1)** | `Worker → Profile/Auth` | **EXISTS** | `POST /api/v1/users` creates user profile and returns JWT (`UserService.java#createUser`). |
| **(2)** | `Profile → Work Data` | **SUPERFICIAL** | `POST /api/v1/gigs` relies on manual user inputs (`amount`, `rating`) rather than automated or sandbox platform ingestion (`GigDataService.java#addGig`). |
| **(3)** | `Work Data → Validation` | **EXISTS** | Jakarta Bean Validation enforces positive amounts and 1–5 ratings (`GigEventRequest.java`). |
| **(4)** | `Validation → Reliability` | **SUPERFICIAL** | Aggregation simply caps values against static ceilings ($5k, 100 jobs); no volatility or trend analysis (`GigScoreService.java#normalizedComponent`). |
| **(5)** | `Reliability → GigScore` | **EXISTS** | Deterministic non-linear formula generates score $\in [0, 100]$ (`GigScoreService.java#computeComponents`). |
| **(6)** | `GigScore → Explanation` | **PARTIAL** | Exact component values and band tags returned, but suggestions are hardcoded static strings (`ScoreResponse.java`). |
| **(7)** | `Explanation → Lender Support` | **SUPERFICIAL** | Lenders see all workers in an unconsented table (`LenderService.java#getAllWorkers`). No consent tokens, no risk report PDF, no formal underwriting decision support. |

---

## 4. Fundamental Architectural Questions Answered

### 1. Is the score meaningful?
**No, not as a financial/credit-risk instrument.** Currently, it is a synthetic gamified activity metric. Because inputs are manually submitted numbers that simply increment counters, anyone can achieve a 100/100 score in 5 minutes by submitting five $1,000 gigs with 5.0 ratings. To become meaningful for lending, the score must evaluate **volatility, consistency over time, and cancellation/completion rates** from timestamped transaction records.

### 2. Can a lender understand *why* someone got their score?
**Partially.** A lender can see the 4 numerical contributions (Earnings, Jobs, Rating, Active Days) and a simple confidence tag. However, they cannot audit earnings volatility, platform statement authenticity, dispute frequency, or trend stability.

### 3. Is there a lender flow at all?
**Yes, but it is architecturally flawed.** A dedicated backend role (`ROLE_LENDER`) and frontend page (`/lenders`) exist, but the flow is an unconsented global table leak where any registered lender sees every worker in the database. A true fintech workflow requires a **worker-controlled consent grant** where workers issue an access token or link to a specific lender.

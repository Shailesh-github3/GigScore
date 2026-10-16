# Phase 0 — Reconnaissance & Environment Audit

Date: 2026-09-20  
Auditor: Senior Software Engineer & Security Reviewer  
Target Repository: `D:\Java\Spring_boot\GigScore` (active Git repository)  
Secondary Workspace Reference: `D:\Java\Projects\GigScore-main`

---

## 1. Design Asset Verification

All target UI assets were located, inspected, and verified against the product specification:

| Asset Path | Status | Resolution / Details | Description |
|---|---|---|---|
| `docs/design/00-signin/screen.png` | Verified | 1440x1110 PNG (220,782 bytes) | Clean sign-in modal, Google auth, "256-bit encrypted", SOC2 claim |
| `docs/design/00-signup/screen.png` | Verified | 1440x1110 PNG (142,637 bytes) | Registration with platform selection pills, password meter, SOC2 badge |
| `docs/design/01-dashboard/screen.png` | Verified | 1440x1365 PNG (415,580 bytes) | "Good morning, Alex", 4 KPI cards, 7D chart, AI insight, connected platforms, recent activity, auto-sweep |
| `docs/design/02-platforms/screen.png` | Verified | 1440x1479 PNG (443,388 bytes) | Integrations grid, sync counts, MTD earnings, 4 connected cards, 3 available connectors, verification pipeline |
| `docs/design/03-gigscore/screen.png` | Verified | 1440x1776 PNG (520,192 bytes) | Score telemetry ring (842/1000), 6 factor cards, 1Y trajectory with milestone annotations, calculation methodology |
| `docs/design/04-insights/screen.png` | Verified | 1440x1998 PNG (1,144,645 bytes) | 5 vector KPIs, earnings growth chart, platform breakdown (3 platforms listed), hourly peak recommendations |
| `docs/design/01-dashboard/DESIGN.md` | Verified | 237 lines (11,612 bytes) | Complete design tokens: Geist + JetBrains Mono typography, `#059669` emerald, `#090A0C` obsidian, 4px grid rhythm, hairline borders |

### Key Observations from Design Inspection
1. **Missing Screens:** The sidebar lists **Activity** and **Settings**, but no dedicated mockups exist in the design package.
2. **Missing Lender Screen:** No lender-facing or decision-support interface is designed in any of the PNGs or `DESIGN.md`.
3. **Internal Data Contradictions:** Score scale alternates between `/1000` (GigScore screen) and `/900` (Platforms and Insights screens); factor breakdown weights sum to ~910/1000 rather than 842; DoorDash is connected in Dashboard/Platforms but missing from Insights; revenue figures conflict across screens ($3,012.50 MTD vs $4,280 30D).

---

## 2. Repository & Technology Stack Mapping

```text
GigScore/
├── .github/workflows/          # CI pipeline definition
├── backend/gigscore/           # Spring Boot Maven backend
│   ├── src/main/java/          # 41 Java source files
│   ├── src/main/resources/     # application.properties
│   ├── src/test/java/          # 11 Test classes (39 test cases)
│   ├── pom.xml                 # Maven project configuration
│   └── mvnw.cmd                # Maven wrapper (v3.9.12)
├── frontend/                   # React SPA (Vite build)
│   ├── src/                    # Components, pages, services, styles
│   ├── package.json            # Node/npm dependency manifest
│   ├── vite.config.js          # Vite configuration
│   └── eslint.config.js        # ESLint flat config
├── docs/
│   ├── design/                 # Target UI screen mockups and DESIGN.md
│   └── audit/                  # Audit reports and evidence
├── docker-compose.yml          # Local container definition
├── README.md                   # Project documentation
├── PROJECT_ANALYSIS.md         # Historical analysis document
└── INTERVIEW_PREPARATION.md    # Historical interview prep notes
```

### Verified Runtime & Toolchain Versions
- **Operating System:** Windows 11 (build 10.0, amd64)
- **Java Runtime / Compiler:** Java 21.0.8 LTS (Oracle Corporation, JDK 21)
- **Backend Framework:** Spring Boot 4.0.4 (Spring Framework 7.0.6, Hibernate Core 7.2.7.Final)
- **Backend Build Tool:** Apache Maven 3.9.12 (via Maven Wrapper) / Maven 3.9.15 (system)
- **Node.js:** v24.13.1
- **Package Manager:** npm 11.8.0
- **Frontend Framework:** React 18.3.1
- **Bundler & Dev Server:** Vite 8.1.5 (Vite 8.0.1 in manifest)
- **Styling:** Tailwind CSS 3.4.17 with PostCSS 8.5.8 and Autoprefixer 10.4.27
- **Database:** MySQL 8 (`com.mysql:mysql-connector-j` runtime); H2 Database 2.4.240 (`com.h2database:h2` test scope)

### Full Dependency List & Risk Flags

#### Backend (`backend/gigscore/pom.xml`)
| Dependency | Version / Scope | Status | Assessment / Risk |
|---|---|---|---|
| `spring-boot-starter-data-jpa` | Managed (4.0.4) | Active | Core persistence layer using Hibernate 7.2.7 |
| `spring-boot-starter-security` | Managed (4.0.4) | Active | Stateless JWT security filter chain |
| `spring-boot-starter-web` | Managed (4.0.4) | Active | REST controllers and Tomcat embedded server |
| `spring-boot-starter-validation` | Managed (4.0.4) | Active | Jakarta Bean Validation on request DTOs |
| `spring-boot-starter-cache` | Managed (4.0.4) | Active | In-memory `ConcurrentMapCacheManager` for score caching |
| `spring-boot-starter-actuator` | Managed (4.0.4) | Active | `/actuator/health` and `/actuator/info` exposed |
| `spring-boot-devtools` | Runtime optional | Active | Development hot-reload |
| `io.jsonwebtoken:jjwt-api` | 0.12.7 | Active | Standard JWT parsing and signing |
| `io.jsonwebtoken:jjwt-impl` | 0.12.7 (runtime) | Active | JJWT HMAC-SHA implementation |
| `io.jsonwebtoken:jjwt-jackson` | 0.12.7 (runtime) | Active | JSON serialization for claims |
| `com.mysql:mysql-connector-j` | Runtime | Active | Production/dev driver for MySQL 8 |
| `springdoc-openapi-starter-webmvc-ui`| 2.8.6 | Active | Swagger UI and OpenAPI v3 documentation |
| `org.projectlombok:lombok` | Optional | Active | Boilerplate reduction via annotation processing |
| `com.h2database:h2` | 2.4.240 (test scope) | **RISK** | **Test-only scope:** prevents running the backend locally in dev mode with an in-memory database without altering `pom.xml` |
| `spring-boot-starter-parent` | 4.0.4 | **CAUTION** | Spring Boot 4.0.4 represents an experimental/milestone line rather than production LTS 3.4.x |
| Database Migration Tool | None | **DEFECT** | **Missing Flyway or Liquibase.** Relies on Hibernate `ddl-auto=update`, which is unacceptable for production/fintech |

#### Frontend (`frontend/package.json`)
| Package | Version | Status | Assessment / Risk |
|---|---|---|---|
| `react` / `react-dom` | ^18.3.1 | Active | Stable React 18 base |
| `react-router-dom` | ^7.13.1 | Active | Client-side routing with protected route logic |
| `axios` | ^1.13.6 | Active | Centralized HTTP client with JWT interceptors |
| `recharts` | ^3.8.0 | Active | SVG charting for score history and trends |
| `tailwindcss` | ^3.4.17 | Active | Utility-first styling with CSS variables |
| `vitest` | ^5.0.0 (dev) | Active | Unit and contract test runner |
| `eslint` / plugins | ^9.39.4 (dev) | Active | Flat configuration linting |
| Large Bundle Size | 623.46 kB | **WARNING** | Single JS chunk triggers Vite `chunkSizeWarningLimit` warning (>500 kB) |
| `caniuse-lite` | Embedded | Notice | Outdated browser database (6 months old) |

---

## 3. Build, Test & Lint Verification

All test suites and build pipelines were executed cleanly without modifying any source files.

### 3.1 Backend Build & Test Run
* **Command:** `./mvnw test`
* **Test Database:** In-memory H2 (`jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER` under `@ActiveProfiles("test")`)
* **Execution Time:** 1 min 25 sec
* **Result:** **BUILD SUCCESS**
  * `com.org.gigscore.GigscoreApplicationTests`: 1 test passed
  * `com.org.gigscore.controller.ActivityPaginationIntegrationTest`: 3 tests passed
  * `com.org.gigscore.controller.GigDataControllerIntegrationTest`: 4 tests passed
  * `com.org.gigscore.controller.LenderControllerIntegrationTest`: 3 tests passed
  * `com.org.gigscore.controller.RateLimitIntegrationTest`: 2 tests passed
  * `com.org.gigscore.controller.ScorePlatformControllerIntegrationTest`: 2 tests passed
  * `com.org.gigscore.controller.UserControllerIntegrationTest`: 4 tests passed
  * `com.org.gigscore.service.GeminiChatServiceTest`: 4 tests passed
  * `com.org.gigscore.service.GigDataServiceTest`: 2 tests passed
  * `com.org.gigscore.service.GigScoreServiceTest`: 13 tests passed
  * `com.org.gigscore.service.UserServiceTest`: 5 tests passed
  * **Summary:** **39 tests run, 0 failures, 0 errors, 0 skipped**

### 3.2 Frontend Test Run
* **Command:** `npm test` (vitest run)
* **Execution Time:** 1.71 sec
* **Result:** **SUCCESS**
  * `src/utils/scoreFormula.test.js`: 4 tests passed
  * `src/services/apiContract.test.js`: 3 tests passed
  * **Summary:** **2 test files passed, 7 tests passed, 0 failures**

### 3.3 Frontend Lint
* **Command:** `npm run lint` (eslint .)
* **Result:** **CLEAN** (0 errors, 0 warnings)

### 3.4 Frontend Production Build
* **Command:** `npm run build` (vite build)
* **Execution Time:** 16.56 sec
* **Output:**
  * `dist/index.html`: 0.61 kB (gzip: 0.38 kB)
  * `dist/assets/index-Bt-JKV64.css`: 25.77 kB (gzip: 5.54 kB)
  * `dist/assets/index-DXl9nHu1.js`: 623.46 kB (gzip: 190.34 kB)
* **Result:** **BUILD SUCCESS**

---

## 4. Current Application Screenshots

Under Operating Rule 1 ("Never run anything that writes to a non-disposable database"), the persistent MySQL 8 instance on port 3306 was left untouched. The frontend application was launched via Vite dev server on port 5173, and Microsoft Edge in headless mode captured high-resolution screenshots of all available pages into `docs/audit/screenshots/current/`:

| Screen Name | File Path | Current State Summary |
|---|---|---|
| Sign In | `docs/audit/screenshots/current/login.png` | Minimalist white card with email/password inputs, "Access Premium Tier" label, Sign In CTA |
| Sign Up | `docs/audit/screenshots/current/register.png` | Registration form with Full Name, Email, Password, Create Account CTA |
| Dashboard | `docs/audit/screenshots/current/dashboard.png` | Sidebar navigation ("Dashboard", "Add Gig", "Score"), top header with user avatar, AI chat FAB, error banner when disconnected |
| Add Gig | `docs/audit/screenshots/current/add-gig.png` | Header "Connect Your Workforce Ecosystem", error banner when disconnected |
| Score | `docs/audit/screenshots/current/score.png` | Score formula description, weighted formula breakdown table, server score display (0.00), improvement section |
| Lender Portal | `docs/audit/screenshots/current/lenders.png` | RBAC-protected lender table view ("Lenders" active in sidebar for `LENDER` role), empty/error state when disconnected |

All screenshot files are saved and preserved under `docs/audit/screenshots/current/`.

---

## 5. README & Documentation Claims (To Be Verified in Phase 1-4)

The project README and `PROJECT_ANALYSIS.md` make specific claims that must be rigorously verified against code evidence:

1. **Multi-Platform Data Ingestion:** Claims support for logging gigs across Uber, Upwork, Fiverr, Swiggy, Zomato, Rapido, DoorDash, TaskRabbit, Lyft, Freelancer.
2. **Deterministic Score Formula:** Claims an exponential power-weighted formula:  
   `Score = (E/5000)^0.70 × 0.35 + (J/100)^0.65 × 0.25 + (R/5)^1.40 × 0.30 + (D/30)^1.00 × 0.10`  
   producing 0-100 with bands EARLY (<40), GROWING (40-69), STRONG (≥70).
3. **Score History Trajectory:** Claims immutable score snapshots recorded on each score change and rendered on a trajectory chart.
4. **Explainability Engine:** Claims factor component breakdowns and automatic identification of the two weakest levers for score improvement.
5. **Data Quality Profiling:** Claims user confidence profiling evaluated across completeness, consistency, recency, and diversity.
6. **Paged Activity Stream:** Claims newest-first paged activity with max page size limit 50.
7. **Gemini AI Integration:** Claims conversational AI guidance with graceful fallback when `GEMINI_API_KEY` is not provided.
8. **Role-Based Lender Access:** Claims `LENDER` role authorization restricting `/api/v1/lender/**` endpoints with a seedable lender account.
9. **Sliding-Window Rate Limiting:** Claims IP-based rate limiting (10 req/min on login, 20 req/min on chat) with HTTP 429 and `Retry-After`.
10. **Monetary Precision Policy:** Claims strict `BigDecimal` usage with scale 2 `HALF_UP` rounding and no raw float/double leaks.
11. **Docker Compose Orchestration:** Claims full containerized orchestration for MySQL, Spring Boot backend, and Nginx frontend.

---

## 6. Assumptions Log

1. **Active Codebase Directory:** The user provided `D:\Java\Spring_boot\GigScore` in the prompt, which contains active Git history (`ee5e77a`), recent commits, design mockups, and passing tests. The secondary directory `D:\Java\Projects\GigScore-main` is an older, uncommitted snapshot. The audit evaluates `D:\Java\Spring_boot\GigScore` as the source of truth, while mirroring audit deliverables to `D:\Java\Projects\GigScore-main` for workspace compatibility.
2. **Database Non-Interference:** Local MySQL 8 was running as a system service. To comply with Operating Rule 1, persistent database writes were prohibited during reconnaissance; builds and integration tests ran strictly against the in-memory H2 test profile.
3. **Target Design Interpretation:** The 4 primary target PNGs (`01-dashboard.png`, `02-platforms.png`, `03-gigscore.png`, `04-insights.png`) plus sign-in/sign-up represent the target frontend interface. Missing screens (Activity, Settings, Lender Portal) must be treated as functional requirements to design cleanly rather than ignored.

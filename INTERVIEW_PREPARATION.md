# GigScore – Complete Interview Preparation Guide

---

## 1. PROJECT UNDERSTANDING

### What does this project do?
GigScore is a full-stack web application that aggregates gig and freelancing work data (from platforms like Swiggy, Zomato, Uber, Rapido, Upwork) into a single dashboard, computes a unified "GigScore" using a weighted formula, and provides AI-powered guidance via a Gemini-powered chat assistant.

### What problem does it solve?
Gig workers and freelancers typically work across multiple platforms. Their performance data (earnings, ratings, jobs completed) is fragmented across these platforms. There is no single, unified metric that measures their overall reliability and performance. GigScore solves this by:
- **Centralizing** data from multiple platforms into one view
- **Quantifying** performance with a single 0–100 score
- **Providing actionable insights** via an AI chat assistant
- **Tracking trends** over time with historical data

### Who would use it?
- **Freelancers** on platforms like Upwork, Fiverr
- **Gig workers** on delivery platforms like Swiggy, Zomato, Uber, Rapido
- **Anyone** who works across multiple gig economy platforms and wants to track their unified performance

### Complete User Flow

```
User arrives at Login Page
       |
       v
[Has account?] --No--> Register (name, email, password)
       |                       |
       |                   [JWT issued, auto-login]
       v                       |
    Login (email + password)   |
       |                       |
       v                       v
    Dashboard Page
       |
       ├── Views 4 StatCards: Total Earnings, Jobs Completed, Avg Rating, Activity Days
       ├── Views Recent Activity timeline
       |
       v
    Add Gig Page
       |
       ├── Sees 5 platform cards (Swiggy, Zomato, Uber, Rapido, Upwork)
       ├── Clicks "Connect" on a platform
       ├── Fills in: Amount earned + Rating received
       ├── Submits
       |
       v
    Backend:
       ├── Updates GigData aggregate for that platform
       ├── Records Activity entry
       ├── Recalculates GigScore
       └── Returns updated dashboard
       |
       v
    Score Page
       |
       ├── Views current GigScore (0-100)
       ├── Views score breakdown (Earnings, Jobs, Rating, Active Days contributions)
       ├── Views Growth Trajectory chart (line chart of score over time)
       └── Reads AI Suggestions
       |
       v
    AI Chat Widget (floating button)
       |
       ├── Ask questions about score, improvements
       ├── Gemini API generates contextual responses
       └── Chat history persists in sessionStorage
```

### Simple One-Liner for Interview
> "GigScore is a full-stack application I built that aggregates gig workers' performance data from multiple platforms into a unified dashboard, computes a weighted GigScore out of 100, and provides AI-driven suggestions through Google Gemini."

---

## 2. TECHNICAL OVERVIEW

### Tech Stack

| Layer | Technology | Why It Was Used |
|-------|-----------|-----------------|
| **Frontend** | React 18 + Vite | Modern, fast build tooling; React for component-based UI |
| | Tailwind CSS 3 | Utility-first CSS for rapid, consistent styling with dark mode support |
| | Recharts | Lightweight React-native charting library for the score trend chart |
| | Axios | Promise-based HTTP client with interceptors for JWT injection |
| | React Router v7 | Client-side routing with declarative navigation |
| **Backend** | Spring Boot 4 | Production-grade Java framework, auto-configuration, embedded server |
| | Java 21 | Modern Java with records, pattern matching, virtual threads |
| | Spring Security + JWT | Stateless authentication without session management |
| | Spring Data JPA | ORM with repository pattern, reduces boilerplate |
| | MySQL 8 | Relational database for ACID compliance and structured relationships |
| **External** | Google Gemini API | AI chat responses for score guidance |
| **Build** | Maven + Vite | Standard Java build tool + fast JS bundler |

### Architecture

```
┌─────────────────────────────────────────────────┐
│                  Frontend (React)               │
│  ┌──────────┐ ┌──────────┐ ┌────────────────┐  │
│  │  Pages   │ │Components│ │ Services (Axios)│  │
│  │ (Login,  │ │(Navbar,  │ │  httpClient    │  │
│  │ Dashboard│ │Sidebar,  │ │  interceptors  │  │
│  │ AddGig,  │ │Chart,    │ │  attach JWT    │  │
│  │ Score)   │ │Chat, etc)│ │  Bearer token  │  │
│  └──────────┘ └──────────┘ └────────────────┘  │
│         │           │                │          │
│         └───────────┴────────────────┘          │
│                        │ HTTP (REST)            │
└────────────────────────┼────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────┐
│              Backend (Spring Boot)              │
│                                                 │
│  HTTP Request → CORS Filter → JwtAuthFilter    │
│       → SecurityConfig → Controller            │
│                            │                    │
│                     ┌──────┴──────┐            │
│                     │             │            │
│                  Service       DTO/Entity      │
│                     │                          │
│               Repository (JPA)                 │
│                     │                          │
│                  MySQL DB                       │
│                                                 │
│  External: Gemini API (via HttpClient)         │
└─────────────────────────────────────────────────┘
```

### Data Flow (Complete Request Lifecycle)

**Example: Adding a Gig Event**

```
1. User fills form in AddGig.jsx
2. React calls gigService.addGig(payload)
3. httpClient interceptor attaches JWT from localStorage
4. Axios sends POST /api/gigs to backend (port 8080)
5. CORS filter validates origin (localhost:5173)
6. JwtAuthFilter extracts Bearer token, validates JWT, sets SecurityContext
7. GigDataController.addGig() receives GigEventRequest DTO
8. GigDataService.addGig():
   a. Validates request
   b. Finds User by ID
   c. resolveOrCreatePlatformAggregate() — finds or creates aggregate row
   d. Updates totalEarnings, jobsCompleted, activeDays, avgRating (weighted)
   e. Saves to GigData table
   f. activityService.recordGigAdded() — inserts Activity row
   g. scoreService.calculateAndPersistScore() — recalculates GigScore
   h. getUserDashboard() — aggregates all data and returns UserDashboardResponse
9. Response flows back: JSON → Controller → HTTP → Axios → React state
10. Dashboard re-renders with updated values
```

---

## 3. FRONTEND EXPLANATION

### Frontend Structure

```
frontend/
  src/
    App.jsx              # Root component, routing, auth state, theme
    main.jsx             # ReactDOM entry point
    index.css            # Tailwind directives + CSS variables for theming
    pages/
      Login.jsx          # Email/password login form
      CreateUser.jsx     # Registration form
      Dashboard.jsx      # Main metrics view + activity timeline
      AddGig.jsx         # Platform selection + gig submission form
      Score.jsx          # Score display, breakdown, chart, AI suggestions
    components/
      Navbar.jsx         # Top bar: dark mode toggle, user avatar, logout
      Sidebar.jsx        # Left nav: Dashboard, Add Gig, Score links
      StatCard.jsx       # Reusable metric card (earnings, jobs, rating, etc.)
      PlatformCard.jsx   # Individual platform card (Swiggy, Zomato, etc.)
      ScoreHistoryChart.jsx  # Recharts LineChart for score trend
      AiChatWidget.jsx   # Floating chat panel with Gemini integration
    services/
      httpClient.js      # Axios instance with JWT interceptor
      userService.js     # createUser, loginUser, getUserDashboard
      gigService.js      # addGig
      scoreService.js    # getScore
      activityService.js # getRecentActivities
      chatService.js     # askChatAssistant
    utils/
      scoreFormula.js    # Client-side score calculation logic (duplicated from backend)
```

### Component Organization

- **App.jsx** is the root. It manages:
  - `currentUser` state (from localStorage on init)
  - `resolvedDarkMode` state (from localStorage)
  - Authentication callback that saves JWT + user data
  - Logout handler that clears all localStorage
  - `MainLayout` sub-component that conditionally renders auth pages vs. authenticated pages

- **MainLayout** (inside App.jsx):
  - If on `/login` or `/register` → renders only the Route, no Navbar/Sidebar
  - If no `currentUser` → redirects to `/login`
  - Otherwise → renders Navbar + Sidebar + main content Routes

### State Management

**There is no global state management library (Redux, Zustand, etc.).** The project uses:
1. **React useState** for local component state
2. **localStorage** for persistence across sessions:
   - `gigscoreToken` — JWT for API calls
   - `gigscoreUserId` / `gigscoreUserName` / `gigscoreUserEmail` — user identity
   - `gigscoreTheme` / `gigscoreDarkMode` — theme preference
3. **sessionStorage** for AI chat message history
4. **Props drilling**: `userId` is passed from App → MainLayout → each page

**Interview note**: This is a valid choice for a smaller application. For production, you'd likely add React Context or Zustand to avoid prop drilling.

### API Call Pattern

All services use a shared `httpClient` (Axios instance):
```javascript
// httpClient.js
const httpClient = axios.create({ baseURL: "http://127.0.0.1:8080" });

httpClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("gigscoreToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

Every API call automatically attaches the JWT. Services are thin wrappers:
```javascript
// gigService.js
export async function addGig(payload) {
  const response = await httpClient.post("/api/gigs", payload);
  return response.data;
}
```

**Pattern**: Services return `response.data` directly, so pages work with parsed JSON, not Axios response objects.

### Authentication Flow

```
Registration:
  1. User fills name, email, password
  2. POST /api/users → backend creates user (BCrypt password) + returns JWT
  3. Frontend stores JWT + user info in localStorage
  4. Navigate to /dashboard

Login:
  1. User fills email, password
  2. POST /api/users/login → backend validates + returns JWT
  3. Frontend stores JWT + user info in localStorage
  4. Navigate to /dashboard

Logout:
  1. Clear all gigscore* keys from localStorage
  2. Set currentUser to null
  3. Navigate to /login
```

### Theme System

- Default is light mode
- Toggle persists to `localStorage` as `gigscoreTheme`
- Uses Tailwind's `dark:` variant + CSS custom properties
- `document.documentElement.classList.toggle("dark")` controls the class
- Backward compatibility with legacy `gigscoreDarkMode` key

### Important Frontend Decisions

1. **No state management library**: Kept simple with useState + localStorage. Acceptable for this scope.
2. **Score formula duplicated on frontend**: `utils/scoreFormula.js` mirrors backend logic. This enables real-time preview without API calls but creates a maintenance risk.
3. **Recharts for charts**: Simple declarative API, well-suited for a single line chart.
4. **CSS variables for theme**: Allows instant transition between dark/light without page reload.
5. **service layer separation**: All HTTP logic in `services/`, UI logic in `pages/` + `components/`.

---

## 4. BACKEND EXPLANATION

### Backend Architecture

```
Controller Layer (REST endpoints, thin: validate + delegate)
       │
       ▼
Service Layer (business logic, orchestration)
       │
       ▼
Repository Layer (Spring Data JPA, database access)
       │
       ▼
Entity Layer (JPA entities mapped to MySQL tables)
```

### Request Flow

```
HTTP Request
  → CORS Filter (CorsConfig.java — allows localhost:5173)
  → JwtAuthFilter (OncePerRequestFilter)
     ├── Extracts "Bearer" token from Authorization header
     ├── Validates JWT via JWTutill
     ├── Sets SecurityContextHolder (authenticated principal = email)
     └── Passes to next filter
  → SecurityConfig chain
     ├── CSRF disabled (stateless API)
     ├── Session creation = STATELESS
     ├── Permits: OPTIONS, /api/users/**, POST /api/gigs
     └── All others require authentication
  → RestController method
```

### API Endpoints

| Method | Endpoint | Purpose | Auth |
|--------|----------|---------|------|
| POST | `/api/users` | Register new user | No |
| POST | `/api/users/login` | Authenticate, get JWT | No |
| GET | `/api/users/{userId}` | Get dashboard data | No* |
| POST | `/api/gigs` | Add a gig event | No* |
| GET | `/api/activity/{userId}` | Get recent 5 activities | Yes |
| GET | `/score/{userId}` | Get current score | Yes |
| POST | `/api/chat/ask` | Ask AI assistant | Yes |

***Note**: The security config permits POST `/api/gigs` and `/api/users/**` without authentication. This is likely a development oversight — in production, all endpoints except login/register should require auth.

### Controller Layer

Each controller is thin — it validates the DTO, calls a service, and returns the response.

**UserController**: Registration + login + dashboard retrieval
**GigDataController**: Single endpoint for adding gigs
**GigScoreController**: Score retrieval (rebuilds on every call)
**ActivityController**: Fetches latest activities
**ChatController**: Proxies chat messages to Gemini

### Service Layer

**UserService**:
- `createUser()`: Validates input, normalizes email (lowercase), checks for duplicates, BCrypt-encodes password, saves user, generates JWT
- `Login()`: Validates credentials, supports BCrypt + legacy plaintext fallback (with automatic upgrade), generates JWT
- `getUserDashboard()`: Delegates to GigDataService

**GigDataService** (core business logic):
- `addGig()`: The most important method:
  1. Validates request (userId, platform, amount, rating required)
  2. `resolveOrCreatePlatformAggregate()`: Finds existing aggregate row for user+platform, or creates new. If duplicates exist, merges them (handles data inconsistencies)
  3. Updates totals incrementally: earnings += amount, jobs += 1, activeDays += 1
  4. Recalculates average rating using weighted formula: `((oldAvg × oldCount) + newRating) / newCount`
  5. Saves GigData
  6. Records Activity entry
  7. Calls `scoreService.calculateAndPersistScore()`
  8. Returns full updated dashboard

**GigScoreService**:
- Uses a weighted + exponent formula:
  ```
  Score = (
    (Earnings/EarningsTarget)^0.70 × 35 +
    (Jobs/JobsTarget)^0.65 × 25 +
    (Rating/5)^1.40 × 30 +
    (ActiveDays/30)^1.00 × 10
  ) × 100
  ```
- Exponents < 1 (earnings: 0.70, jobs: 0.65) reward early progress faster
- Exponent > 1 (rating: 1.40) is stricter — punishes low ratings more
- All components are normalized (capped at 1.0) before exponent weighting

**UserMetricsService**:
- Aggregates across all platform rows for a user
- Computes total earnings, total jobs, weighted average rating, total active days

**ActivityService**:
- Records every gig addition as an Activity row
- Returns latest 5 for the timeline

**GeminiChatService**:
- Uses Java HttpClient (no Spring RestTemplate)
- Builds Gemini API request with `contents` array (messages with role/parts)
- Configurable model + API key via env variables
- Handles errors with specific exception types
- Role mapping: "assistant" → "model", everything else → "user"

### Authentication & Authorization

```
1. JWTutill:
   - Key: HMAC-SHA from jwt.secret (configured via env)
   - Claims: subject = email, issuedAt, expiration (1 hour)
   - Methods: generateToken(), extractEmail(), validateJwtToken()

2. JwtAuthFilter:
   - Extracts "Bearer xxx" from Authorization header
   - Validates JWT
   - Sets UsernamePasswordAuthenticationToken in SecurityContext
   - Principal = email (String), no GrantedAuthorities (no roles/RBAC)

3. SecurityConfig:
   - CSRF disabled
   - Stateless sessions
   - Public: OPTIONS, POST /api/gigs, /api/users/**
   - All other endpoints: authenticated
   - Adds JwtAuthFilter before UsernamePasswordAuthenticationFilter
```

**Security concern**: The application has no role-based authorization. Any authenticated user could theoretically access any userId's data. The endpoints accept `userId` as a path variable but never verify that the requesting user owns that userId. This is a significant security gap for production.

---

## 5. DATABASE UNDERSTANDING

### Database Design

```sql
Database: gigscore (MySQL 8)

Tables:
  1. user
  2. gig_data
  3. gig_score
  4. activity
```

### Entity Relationships

```
User (1) ─────── (N) GigData      (one user has many platform aggregates)
User (1) ─────── (1) GigScore     (one user has one score)
User (1) ─────── (N) Activity     (one user has many activity events)
```

### Table Schemas

**User Table**
| Column | Type | Constraints |
|--------|------|-------------|
| user_id | BIGINT | PK, AUTO_INCREMENT |
| email | VARCHAR | NOT NULL, UNIQUE |
| password | VARCHAR | BCrypt hash |
| name | VARCHAR | - |

**GigData Table** (platform-level aggregates)
| Column | Type | Constraints |
|--------|------|-------------|
| gig_id | BIGINT | PK, AUTO_INCREMENT |
| user_id | BIGINT | FK → User, NOT NULL |
| platform | VARCHAR | e.g. "Swiggy", "Uber" |
| total_earnings | DOUBLE | Cumulative earnings for this platform |
| jobs_completed | INT | Total jobs on this platform |
| avg_rating | DOUBLE | Weighted average rating |
| active_days | INT | Days with activity |

**GigScore Table** (user-level score)
| Column | Type | Constraints |
|--------|------|-------------|
| gig_score_id | BIGINT | PK, AUTO_INCREMENT |
| user_id | BIGINT | FK → User, NOT NULL |
| score | DOUBLE | Computed score (0-100) |

**Activity Table** (event log)
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| user_id | BIGINT | FK → User, NOT NULL |
| platform | VARCHAR | Platform name |
| action | VARCHAR | e.g. "Added gig" |
| amount | DOUBLE | Earnings for this gig |
| rating | DOUBLE | Rating for this gig |
| timestamp | DATETIME | When the gig was added |

### Why This Design

1. **Denormalized aggregates (GigData)**: Instead of storing individual gig transactions and aggregating on read, the app maintains running totals. This makes reads O(1) per platform, which is fast for dashboard display. The trade-off is write complexity — every insert must carefully update running totals.

2. **Separate score table**: The GigScore is computed separately and cached. This avoids recalculating from raw data on every read. The current implementation actually recalculates on every GET /score/{userId} call, but the table exists for potential caching.

3. **Activity as event log**: Separate table for timeline/audit trail. Fixed at 5 most recent — not meant for full history.

4. **Single table for all platforms**: A `platform` column distinguishes data sources rather than separate tables per platform. Simple and flexible for adding new platforms (just add to the PLATFORMS array in frontend).

### How Data Is Stored, Retrieved, and Updated

**Storage**: JPA automatically creates/updates tables via `ddl-auto=update`. All entities use `GenerationType.IDENTITY` for primary keys.

**Retrieval**: Spring Data JPA repositories provide `findBy*`, `findAllBy*` methods. Queries are derived from method names (no custom JPQL needed).

**Updates**: All updates go through the service layer. The pattern is:
1. Load entity (via findById or findByUser)
2. Modify fields
3. Save (JPA merge or persist)

---

## 6. ARCHITECTURE AND DESIGN DECISIONS

### Why This Architecture?

**Monolithic (Spring Boot + React)**: The application is small enough that a monolithic architecture is appropriate. It avoids the complexity of microservices while keeping a clean separation between frontend and backend.

**REST API**: Simple, stateless, works well with React. JWT eliminates server-side session storage.

**Why Spring Boot 4 + Java 21?**
- Production-grade security (Spring Security)
- Auto-configuration reduces boilerplate
- JPA makes database operations declarative
- Java 21 records simplify DTOs (UserAggregateMetrics)
- Maven for dependency management

**Why React 18 + Vite?**
- Vite is significantly faster than Create React App for development
- HMR (Hot Module Replacement) is instant
- React is the most widely used frontend framework — proven ecosystem

**Why MySQL?**
- The data is highly relational (User → GigData → GigScore → Activity)
- ACID compliance ensures data integrity for financial/rating data
- JPA is designed for relational databases

### Alternatives Considered

| Component | Chosen | Alternative | Trade-off |
|-----------|--------|-------------|-----------|
| Database | MySQL | MongoDB | MongoDB would be more flexible for varying platform data shapes, but MySQL ensures consistency for financial data |
| Frontend state | useState + localStorage | Redux/Zustand | Simpler for small app, but would become messy as complexity grows |
| AI service | Direct HTTP call to Gemini API | LangChain/Spring AI | Direct call is simpler, LangChain adds abstraction for multi-model support |
| Auth | JWT | Session cookies | JWT is stateless, works for SPA + API separation. Session cookies would need CSRF protection but could be more secure |
| Chart library | Recharts | D3, Chart.js | Recharts is more React-idiomatic, D3 gives more control |

### Folder Structure Rationale

```
backend/gigscore/
  Config/       — Cross-cutting concerns (security, CORS, JWT)
  Controller/   — HTTP layer (thin delegates)
  DTO/          — Request/Response objects
  Entity/       — JPA domain models
  Repository/   — Database access (Spring Data interfaces)
  Service/      — Business logic

frontend/src/
  pages/        — Full page components (one per route)
  components/   — Reusable UI pieces
  services/     — API client wrappers
  utils/        — Shared utility functions
```

This separation follows standard layered architecture for the backend and a feature-based organization for the frontend. Controllers don't contain business logic. Services don't handle HTTP concerns. This makes testing easier and keeps concerns separated.

---

## 7. IMPORTANT CODE CONCEPTS

### 1. Score Calculation Algorithm (GigScoreService.java)

```java
public double calculateScoreValue(UserAggregateMetrics metrics) {
    double earningsComponent = Math.pow(
        normalizedComponent(metrics.totalEarnings(), EARNINGS_TARGET), EXP_EARNINGS) * WEIGHT_EARNINGS;
    double jobsComponent = Math.pow(
        normalizedComponent(metrics.jobsCompleted(), JOBS_TARGET), EXP_JOBS) * WEIGHT_JOBS;
    double ratingComponent = Math.pow(
        normalizedComponent(clamp(metrics.avgRating(), 0.0, 5.0), 5.0), EXP_RATING) * WEIGHT_RATING;
    double activeDaysComponent = Math.pow(
        normalizedComponent(metrics.activeDays(), ACTIVE_DAYS_TARGET), EXP_ACTIVE_DAYS) * WEIGHT_ACTIVE_DAYS;

    double score = (earningsComponent + jobsComponent + ratingComponent + activeDaysComponent) * MAX_SCORE;
    return roundToTwoDecimals(score);
}
```

**Why it's important**: This is the core business logic — the "secret sauce" of the application. The weighted + exponent approach allows fine-tuning: lower exponents (0.7, 0.65) reward early progress (diminishing returns), higher exponent (1.4 for rating) is more punishing. This encourages both consistency and quality.

**Targets**: $5000 earnings, 100 jobs, 5.0 rating, 30 active days. These define "perfection" for each dimension.

### 2. Platform Aggregate Management (GigDataService.resolveOrCreatePlatformAggregate)

```java
private GigData resolveOrCreatePlatformAggregate(User user, String platform) {
    List<GigData> rows = new ArrayList<>(gigDataRepository.findAllByUserAndPlatform(user, platform));
    if (rows.isEmpty()) {
        // Create new aggregate for this platform
        GigData g = new GigData();
        g.setUser(user);
        g.setPlatform(platform);
        // ... initialize to 0
        return g;
    }
    // If multiple rows exist (data inconsistency), merge them
    GigData primary = rows.get(0);
    // ... merge all rows into primary, delete duplicates
    return primary;
}
```

**Why it's important**: This handles an edge case where duplicate platform aggregates may exist (due to bugs or race conditions). Instead of failing, it intelligently merges them by summing earnings/jobs/activeDays and recalculating weighted average rating. This makes the system resilient to data corruption.

### 3. Average Rating Incremental Update

```java
int oldCount = data.getJobsCompleted() - 1;
double newAvg = ((data.getAvgRating() * oldCount) + request.getRating()) / data.getJobsCompleted();
```

**Why it's important**: Instead of recalculating from all past transactions (which aren't stored individually), the system maintains a running weighted average. This is efficient O(1) per update but requires careful implementation to avoid off-by-one errors.

### 4. JWT Authentication Filter

```java
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;  // Don't block — let SecurityConfig decide
    }
    String token = authHeader.substring(7);
    if (jwtUtil.validateJwtToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
        String email = jwtUtil.extractEmail(token);
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    filterChain.doFilter(request, response);
}
```

**Why it's important**: This filter runs on every request. If the token is missing or invalid, it doesn't throw an error — it simply doesn't set authentication. SecurityConfig then enforces which endpoints require auth. This separation of concerns (filter extracts token, config decides access) is a clean Spring Security pattern.

### 5. Gemini Chat Service

```java
public String generateReply(List<ChatMessageDTO> messages) {
    // Build request payload matching Gemini API format
    List<Map<String, Object>> contents = messages.stream()
        .map(message -> Map.of("role", toGeminiRole(message.role()),
                               "parts", List.of(Map.of("text", message.content()))))
        .toList();

    Map<String, Object> payload = Map.of(
        "contents", contents,
        "generationConfig", Map.of("temperature", 0.7, "topP", 0.9));

    // Java HttpClient POST request
    HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    // Parse response: root → candidates[0] → content → parts[0] → text
}
```

**Why it's important**: This is the integration point with Google's Gemini API. It uses Java's built-in HttpClient (no RestTemplate/WebClient) and carefully maps the conversation format. The `toGeminiRole()` method normalizes roles ("assistant" → "model"). The error handling distinguishes between configuration errors (no API key), bad requests, and server errors.

### 6. Frontend Axios Interceptor (httpClient.js)

```javascript
httpClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("gigscoreToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

**Why it's important**: This single interceptor ensures every API call automatically includes the JWT. It's a clean cross-cutting concern — individual service files don't need to worry about auth headers. This is a standard pattern for SPAs with JWT auth.

### 7. Score History Chart Logic (ScoreHistoryChart.jsx)

```javascript
const buildScoreHistory = (activities = [], currentScore = 0) => {
  // Build score trajectory by replaying activities chronologically
  let totalEarnings = 0, jobsCompleted = 0, ratingSum = 0;
  const activeDays = new Set();

  const points = sorted.map((activity, index) => {
    totalEarnings += activity.amount;
    jobsCompleted += 1;
    ratingSum += activity.rating;
    // Calculate what the score WOULD HAVE BEEN at this point
    const avgRating = ratingSum / jobsCompleted;
    const score = scoreFromAggregates(totalEarnings, jobsCompleted, avgRating, activeDays.size);
    return { interval: `T${index + 1}`, score, timeLabel: ... };
  });
  return points;
};
```

**Why it's important**: Since only current aggregates are stored (not raw transaction history), the frontend reconstructs the score trajectory by replaying activity events chronologically and computing what the score would have been at each step. This is a clever approach but means the historical accuracy depends on having all activity events.

---

## 8. INTERVIEW PREPARATION

### 30-Second Project Explanation

> "GigScore is a full-stack application that lets gig workers track their performance across multiple platforms like Swiggy, Uber, and Upwork in one place. It computes a unified score from 0 to 100 based on earnings, jobs completed, ratings, and consistency, and provides AI-powered suggestions through Google Gemini. I built it with React on the frontend and Spring Boot with MySQL on the backend."

### 2-Minute Project Explanation

> "GigScore solves a real problem for gig economy workers who operate across multiple platforms. Their performance data is siloed — there's no single metric that captures their overall reliability.
>
> The application has a React frontend with a dashboard showing key metrics, a page for adding gig events by platform, and a score page with detailed breakdown. The backend is a Spring Boot REST API with JWT authentication.
>
> The core innovation is the score algorithm. It takes four metrics — total earnings, jobs completed, average rating, and active days — normalizes each against a target, applies an exponent to shape the curve, then weights them (35% earnings, 25% jobs, 30% rating, 10% activity days). The exponents below 1 reward early progress, and the exponent above 1 on ratings is stricter.
>
> There's also an AI chat widget powered by Google Gemini that users can ask for score improvement suggestions.
>
> The biggest technical challenge was the score calculation — getting the formula right, handling average rating updates incrementally, and ensuring consistency when data comes in."

### Detailed Technical Explanation (5 Minutes)

> "Let me walk through the full architecture.
>
> **Frontend**: React 18 with Vite for fast builds. Tailwind CSS for styling with full dark mode support. Axios with a request interceptor that automatically attaches the JWT from localStorage. React Router handles navigation with protected routes. There's no state management library — I used useState and localStorage since the state is relatively simple.
>
> **Backend**: Spring Boot 4 with Java 21. The architecture follows a standard layered pattern: Controllers receive HTTP requests, Services contain business logic, Repositories handle database access via Spring Data JPA, and Entities map to MySQL tables.
>
> **Authentication**: JWT-based. When a user registers or logs in, the backend generates a JWT with their email as the subject, signed with HMAC-SHA. Every subsequent request goes through a JwtAuthFilter that validates the token and sets the Spring Security context. The SecurityConfig permits unauthenticated access only to register and login endpoints.
>
> **The Score Algorithm**:
> - Four dimensions weighted differently: earnings (35%), jobs (25%), rating (30%), active days (10%)
> - Each dimension is normalized (value / target, capped at 1.0)
> - An exponent is applied: earnings^0.7, jobs^0.65, rating^1.4, active days^1.0
> - The exponents < 1 create diminishing returns — you get more score improvement early on, which encourages new workers
> - The exponent > 1 on ratings means a 4.0 rating gives proportionally less than a 5.0, incentivizing quality
> - The weighted sum is multiplied by 100 to get a 0-100 score
>
> **Data Flow**: When a user adds a gig event, the backend:
> 1. Validates the input
> 2. Finds or creates a platform aggregate row (user + platform)
> 3. Incrementally updates earnings, jobs count, active days
> 4. Recalculates the running average rating using a weighted formula
> 5. Records an activity event for the timeline
> 6. Recalculates and persists the overall GigScore
> 7. Returns the updated dashboard in one response
>
> **AI Chat**: The chat widget sends messages to a Spring Boot endpoint, which forwards them to Google Gemini's API using Java's HttpClient. The response is streamed back and displayed in a chat panel. The conversation history is maintained in sessionStorage so it persists across page navigations within the same session.
>
> **Database**: Four tables with foreign keys to user. GigData stores per-platform aggregates (not individual transactions). GigScore stores the computed score. Activity stores individual events for the timeline. This denormalized design makes reads fast but requires careful write logic."

### Interview Q&A

**Q: Why did you build this project?**
> "I noticed that gig workers often operate on multiple platforms — maybe they do Swiggy deliveries and also freelance on Upwork. There's no unified way to measure their overall performance or track their reputation across platforms. I wanted to create a tool that gives them a single score they can monitor and improve, plus AI-driven suggestions on how to improve it."

**Q: Why did you choose this tech stack?**
> "Spring Boot is my go-to for backend because it provides production-grade security, database access, and REST API support out of the box. Java 21 gives us records and pattern matching which clean up the code. For the frontend, React is the most mature ecosystem with the best tooling, and Vite gives near-instant hot reloading. MySQL works well because our data is highly relational — users, platforms, scores, and activities all have clear relationships. Tailwind CSS allowed me to implement the dark mode theme with very little effort using the dark: variant."

**Q: What was the biggest challenge?**
> "The score algorithm itself was tricky. I had to balance four different metrics with different scales — earnings in dollars, rating on a 1-5 scale, jobs count, and active days. The normalization and exponent approach was the solution: each metric is normalized to a 0-1 range, then shaped with an exponent, then weighted. Getting the exponents right required experimentation — I wanted the score to be encouraging for new workers while still being meaningful for experienced ones. The second challenge was the incremental average rating update — maintaining a correct running average without storing every individual rating required careful math."

**Q: How does the system work internally?**
> *See the 5-minute detailed explanation above.*

**Q: What would you improve if you had more time?**
> "Several things. First, I'd add proper role-based authorization — currently any user could access any userId's data. Second, the score is recalculated on every read, which is wasteful — I'd cache it and only recalculate on writes. Third, I'd add automated testing, especially for the score calculation logic. Fourth, I'd replace the props drilling with React Context or Zustand. Fifth, I'd move the score configuration (targets, weights, exponents) to a database table so it can be tuned without code changes."

**Q: How would you scale this project?**
> "The current architecture supports thousands of users easily. For scaling to hundreds of thousands: add Redis caching for dashboard responses and scores, implement database read replicas for query-heavy endpoints, add pagination for activity history (currently limited to 5), and move the score calculation to a background job queue. The frontend could be deployed to a CDN. If we needed to support more external platforms, I'd implement an adapter pattern so each platform can have custom data transformation logic."

**Q: What security considerations did you handle?**
> "I used BCrypt for password hashing, JWT for stateless authentication, environment variables for all secrets (database credentials, JWT secret, API keys), and CORS configuration to restrict origins to the frontend domain. Passwords are never stored in plaintext. The JWT has a 1-hour expiration. However, I acknowledge there's room for improvement — I'd add rate limiting, proper authorization checks (ensuring a user can only access their own data), HTTPS enforcement, and input sanitization for the chat endpoint."

---

## 9. CRITICAL REVIEW

### Strengths

1. **Clean layered architecture**: Backend follows standard Controller → Service → Repository pattern. Each layer has a single responsibility.
2. **Innovative scoring algorithm**: The weighted + exponent approach is thoughtful and tunable.
3. **Dark mode**: Fully implemented with CSS variables and Tailwind. Persistent to localStorage.
4. **Duplicate handling**: `resolveOrCreatePlatformAggregate()` and `resolveOrCreateUserGigScore()` handle data inconsistencies gracefully by merging duplicates.
5. **Backward-compatible auth**: Login supports BCrypt with automatic upgrade from legacy plaintext.
6. **Complete user flow**: Registration → Login → Dashboard → Add Gig → Score → AI Chat is a complete, cohesive experience.

### Weaknesses

1. **No authorization checks**: Any user can access any userId's data. The endpoints accept `userId` as a path variable but never verify ownership.
2. **No input sanitization for chat**: The chat endpoint passes user messages directly to Gemini without sanitization or content filtering.
3. **Score recalculated on every read**: `getScoreForUser()` calls `calculateAndPersistScore()` every time, which is wasteful. It should be cached.
4. **Duplicated score formula**: `scoreFormula.js` on frontend mirrors `GigScoreService.java` on backend. Any change must be made in both places — a maintenance risk.
5. **No pagination for activities**: Limited to 5 most recent. No mechanism to view older activities.
6. **No error boundaries**: If an API call fails, error messages are shown inline but there are no React error boundaries for catastrophic failures.
7. **Hardcoded platform list**: `PLATFORMS = ["Swiggy", "Zomato", "Uber", "Rapido", "Upwork"]` is hardcoded in frontend. Adding a platform requires a code change.

### Scalability Concerns

1. **Score calculation is O(N) in aggregate rows**: As the number of platforms grows, the recalculation takes proportionally longer. For a user with 50 platforms, this could be noticeable.
2. **No connection pooling tuning**: Default HikariCP settings may not handle high concurrency.
3. **Single MySQL instance**: No read replicas, no sharding.
4. **Monolithic deployment**: Backend and all services are in one JAR. Cannot scale components independently.

### Production-Ready Changes Needed

1. **Add proper authorization**: Extract userId from JWT, not request path. Verify ownership before returning data.
2. **Add rate limiting**: On login endpoint (prevent brute force) and chat endpoint (control API costs).
3. **Cache scores**: Use Redis or in-memory cache with invalidation on gig addition.
4. **Add HTTPS**: Configure SSL certificate, possibly behind a reverse proxy (Nginx).
5. **Add logging framework**: Structured logging with SLF4J/Logback for production observability.
6. **Add monitoring**: Health endpoints, metrics (Spring Actuator), APM integration.
7. **Database migration tool**: Replace `ddl-auto=update` with Flyway/Liquibase for controlled schema migration.
8. **Environment-specific config**: Use Spring Profiles (dev/staging/prod) with separate configurations.
9. **Add comprehensive tests**: Unit tests for services, integration tests for controllers, end-to-end tests for critical flows.
10. **CI/CD pipeline**: GitHub Actions for automated build, test, and deployment.
11. **Containerization**: Docker Compose for local dev, Kubernetes for production.
12. **Proper secret management**: Use a vault solution (AWS Secrets Manager, HashiCorp Vault) instead of .env files.

---

### Quick Reference Diagram

```
                      ┌──────────────────────────────┐
                      │       React Frontend          │
                      │  ┌────────────────────────┐  │
                      │  │  App.jsx               │  │
                      │  │  ├── currentUser state │  │
                      │  │  ├── darkMode state    │  │
                      │  │  └── Layout logic      │  │
                      │  │     ├── Auth pages     │  │
                      │  │     └── Main app       │  │
                      │  │         ├── Navbar     │  │
                      │  │         ├── Sidebar    │  │
                      │  │         └── Routes     │  │
                      │  │             ├── /dashboard │
                      │  │             ├── /add-gig   │
                      │  │             └── /score     │
                      │  └────────────────────────┘  │
                      │                              │
                      │  Services (Axios + JWT)      │
                      │  userService / gigService    │
                      │  scoreService / chatService  │
                      └──────────┬───────────────────┘
                                 │ HTTP REST
                                 ▼
┌────────────────────────────────────────────────────┐
│              Spring Boot Backend                    │
│                                                     │
│  ┌──────────────┐  ┌──────────┐  ┌────────────┐   │
│  │ Controllers  │→│ Services │→│Repositories│   │
│  │ UserController│ │UserService│ │JPA extends│   │
│  │ GigDataContr. │ │GigDataSvc│ │CrudRepo   │   │
│  │ ScoreContr.   │ │ScoreSvc  │ │           │   │
│  │ ActivityContr.│ │MetricsSvc│ │           │   │
│  │ ChatController│ │ChatSvc   │ │           │   │
│  └──────────────┘  └──────────┘  └─────┬──────┘   │
│                                         │          │
│  ┌──────────────┐  ┌──────────────┐     │          │
│  │ Security     │  │ Integration  │     │          │
│  │ JwtAuthFilter│  │ Gemini API   │     ▼          │
│  │ JWTutill     │  │ HttpClient   │  ┌──────┐     │
│  │ SecurityConf │  └──────────────┘  │ MySQL│     │
│  │ CORS Config  │                   │  DB  │     │
│  └──────────────┘                   └──────┘     │
└────────────────────────────────────────────────────┘

Score Formula:
Score = [ (E/5000)^0.7 × 35 + (J/100)^0.65 × 25 + (R/5)^1.4 × 30 + (D/30)^1.0 × 10 ] × 100

Where: E = Earnings, J = Jobs, R = Rating, D = Active Days
Each component clamped to [0, 1] before exponent
```

---

*Use this guide as your reference for technical interviews. Focus on understanding the architectural decisions, the data flow, and the reasoning behind the score algorithm. The weaknesses and improvements sections show that you can critically evaluate your own work — which is exactly what senior engineers do.*
# Phase 5 — "Make the Backend Worthy of the Target UI" Plan

**Target Document:** `docs/audit/05-ui-gap-analysis.md`  
**Scope:** Deep visual, domain, contract, and engineering reconciliation between the target Stitch UI (`docs/design/`) and the GigScore backend (`D:\Java\Spring_boot\GigScore`).

---

## 5.1 Screen Element Inventory

Every screen from the Stitch AI target design (`docs/design/`) has been disassembled into distinct UI widgets and data elements:

### 1. Global Application Shell (Common Across All Screens)
* **Top Bar / Brand Identity:**
  * Logo mark (`GigScore` typography, stylized emerald lightning/meter icon).
  * Environment & Sync Status: `"Live Sync: Q3 2024"`, pulsating green dot (`"4 NODES ACTIVE"`).
  * Global Search / Quick Jump (`⌘K` shortcut trigger).
  * Notifications Center: Bell icon with active unread indicator badge (count `3`).
  * User Footer / Session Profile: Avatar badge (`"AR"`), user name (`"Alex Rivera"`), subtitle (`"PRO OPERATOR"`), contextual profile action dropdown.
* **Left Rail Navigation (Fixed 64px/240px expanded):**
  * Core links: `Dashboard` (LayoutDashboard), `Platforms` (Network / Layers), `GigScore` (Gauge / ShieldCheck), `Activity` (Activity / History), `Insights` (TrendingUp / Sparkles), `Settings` (Settings / Sliders).
  * System telemetry footer: `"Engine v2.4.1"`, `"Hashed Verification #9fa2-842"`.

---

### 2. Screen 1: Dashboard (`01-dashboard/code.html`)
* **KPI Metrics Ribbon (4 Cards):**
  1. *Today's Earnings:* Value `\$184.50`, Trend `+12.4% vs yesterday`, Subtext `3 platform payouts (Uber, DoorDash, Upwork)`.
  2. *Jobs Completed:* Value `8 units`, Trend `+2 ahead of target`, Subtext `Target 10/day · 80% daily goal pacing`.
  3. *Average Rating:* Value `4.87 / 5.0`, Trend `+0.08 this month`, Subtext `99.4% CSAT across 312 client reviews`.
  4. *Composite GigScore:* Value `842`, Trend `+24 pts (7-day delta)`, Subtext `Tier: Platinum · Top 4% across regional index`.
* **Primary Visualizations:**
  * *7-Day Aggregate Earnings Bar/Area Chart:* Total `\$1,482.00`, Daily Average `\$211.71/day`, Projected Run-rate `\$6,340/month`. Tooltip breakouts showing peak earning windows (`18:00–21:30 CST`).
  * *"Export Ledger CSV"* interactive header action.
* **AI Performance Insights Widget:**
  * Highlight Card: `"Optimized Friday Evening Surge: Upwork milestones + Uber peak hours increased hourly yield by 28% (\$34.20/hr)."`.
* **Platform Node Status Grid:**
  * 4 Active Nodes: `Uber` (`Synced 4m ago`, `\$840.50`), `Upwork` (`Synced 12m ago`, `\$1,240.00`), `Fiverr` (`Synced 1h ago`, `\$620.00`), `DoorDash` (`Synced 2h ago`, `\$312.00`).
* **Progress Gauge:**
  * *Daily Revenue Target:* Progress bar at `73.8%` (`\$184.50 / \$250.00`).
* **Recent Activity Feed (4 Live Rows):**
  * Upwork: Fixed Milestone Approved (`+\$450.00`, `Completed`, `14:22`).
  * Uber: Peak Ride Fare (`+\$38.20`, `Completed`, `12:05`).
  * DoorDash: Batch Delivery (`+\$24.50`, `Completed`, `10:48`).
  * Fiverr: Gig Escrow Release (`+\$120.00`, `Completed`, `Yesterday`).
* **Financial Services Interop:**
  * *Bank Auto-Sweep Indicator:* `"Next automatic ACH sweep: Tonight 23:59 CST (Balance: \$1,482.00)"`.

---

### 3. Screen 2: Platforms Management (`02-platforms/code.html`)
* **Header & Control Ribbon:**
  * Headline: `"Node Sync Protocol v2.4"`.
  * Summary Counters: `4 Connected Nodes`, `3 Available Connectors`.
  * Sync telemetry: `"Last sync: 4 mins ago"`, `"Auto-sync: Enabled (15m intervals)"`, `"Refresh All" CTA button`.
* **Aggregated Performance Metrics Banner:**
  * *MTD Total Gross Earnings:* `\$3,012.50`.
  * *Reputation Score:* `4.93 / 5.0` (`99.2% Trust Coefficient`).
  * *Active Dispatches / Deliveries:* `72 completed cycles`.
  * *GigScore Contribution:* `824 / 900` (`Tier: Elite`).
* **Connected Platform Cards (4 Cards):**
  * *Upwork:* Status `Active Sync`, Revenue `\$1,240.00`, Job Success `98%`, Rating `4.96/5.0`, Sync interval `Real-time webhook`.
  * *Uber:* Status `Active Sync`, Revenue `\$840.50`, Rating `4.90/5.0`, Completed trips `42`, Acceptance `94%`.
  * *Fiverr:* Status `Active Sync`, Revenue `\$620.00`, Rating `4.85/5.0`, Orders `12`, On-time delivery `100%`.
  * *DoorDash:* Status `Active Sync`, Revenue `\$312.00`, Rating `4.92/5.0`, Deliveries `18`, Customer rating `99%`.
* **Available Connectors Directory:**
  * Connectable integrations: `TaskRabbit`, `Instacart`, `Freelancer.com`. Connect CTAs triggering OAuth consent.
* **Compliance & Trust Badges:**
  * `"SOC2 Type II Certified Pipeline"`, `"256-bit TLS AES Encrypted Node Transport"`.

---

### 4. Screen 3: GigScore Detail & Telemetry (`03-gigscore/code.html`)
* **Primary Score Ring & Header:**
  * Central Telemetry Gauge: Score `842 / 1000`.
  * Tier classification: `Tier 1: Elite Operator` (`Top 4% of 48,200 Gig Workers`).
  * 90-day momentum: `+41 pts`.
  * Cryptographic Hash: `Verified Ledger Hash #9fa2-842-c7e1`.
  * Next scheduled evaluation: `In 3h 42m`.
* **Verifiable Credential Card:**
  * Public Credential Link: `https://gigscore.fi/verify/ar-842-9fa2` (`"Copy Verified Link"`).
  * PDF Export: `"Download Signed Attestation (Lender Proof)"`.
* **Six Component Factor Breakdown Grid:**
  1. *Reliability Index (25% Weight):* Score `92/100` · Completion rate `99.2%`, cancellation rate `<0.8%`.
  2. *Client Feedback (20% Weight):* Score `96/100` · Weighted rating `4.91/5.0` across 3 platforms.
  3. *Work Completion (20% Weight):* Score `94/100` · 72/74 tasks delivered within deadline SLA.
  4. *Response Velocity (15% Weight):* Score `87/100` · Median inquiry response time `18 minutes`.
  5. *Income Consistency (10% Weight):* Score `81/100` · Coefficient of variation `0.18` (low volatility).
  6. *Growth Trajectory (10% Weight):* Score `88/100` · Quarter-over-quarter gross revenue `+14.2%`.
* **1-Year Longitudinal Historical Trajectory Chart:**
  * Monthly score line (Oct 2023 to Oct 2024: 710 $\rightarrow$ 745 $\rightarrow$ 768 $\rightarrow$ 790 $\rightarrow$ 818 $\rightarrow$ 842).
  * Key milestone flags: `"Top Rated Plus on Upwork"`, `"1,000th Uber Trip"`.
* **Formula Transparency Drawer:**
  * Link: `"View Scoring Methodology Whitepaper & Mathematical Model"`.

---

### 5. Screen 4: Insights & Deep Analytics (`04-insights/code.html`)
* **Analytics Header & Date Controls:**
  * Range: `"Oct 1, 2024 – Oct 24, 2024"`, Export data dropdown.
* **Executive AI Summary Card:**
  * `"Income velocity increased 18% following multi-apping strategy between Upwork weekday development and Uber weekend evening surge hours."`.
* **5 Key Vector Summary Badges:**
  * Score: `842 / 900`, CSAT: `4.87`, Completion: `98.4%`, Response: `21 mins`, Dispatches: `92`.
* **Earnings Growth & Multi-Platform Yield Chart:**
  * Dual-series comparison: Actual `\$4,280.00` vs Prior Period `\$3,740.00` (`+14.4%`).
* **Platform Contribution Breakdown:**
  * Upwork: `\$2,140.00` (50.0%), Uber: `\$1,320.00` (30.8%), Fiverr: `\$820.00` (19.2%). *(Note: DoorDash omitted in this screen's hardcoded copy).*
* **Opportunity & Surge Radar Cards:**
  * Surge Alert: `"Friday Night Rideshare Demand: Estimated +34% gross margin in Central Metro"`.
  * AI Recommendations: `"Fulfill 3 more Upwork fixed deliverables before month-end to elevate Reliability to 95 (+32 GigScore pts)."`.

---

## 5.2 Internal Inconsistencies & Reconciliation Model

The target UI generated by Stitch AI contains several glaring data and mathematical contradictions that would immediately destroy credibility in a technical interview or product demo. Here is how we reconcile them into a single, cohesive, mathematically sound model:

| Conflict Identified in UI Screens | Screen A | Screen B | Root Cause in AI Design | Reconciled Canonical Model |
| :--- | :--- | :--- | :--- | :--- |
| **Score Scale** | `842 / 1000` (Screen 03 `gigscore`) | `824 / 900` (Screen 02 `platforms`), `842 / 900` (Screen 04 `insights`) | AI prompt drift across different page generations | **Standardize on 300–850 (FICO parity)** or **0–1000 (Fintech alternative credit scale)**. We adopt **300–850** with normalized factor weights, displaying raw score `812` (or adjust demo score to `812` / FICO scale) or standard **0–1000 scale**. **Decision: Standardize strictly on 0–1000** for all screens. `842 / 1000` is the single canonical score for Alex Rivera. |
| **Factor Math Reconcilation** | 6 factors sum to `910 / 1000` ($92 \times 0.25 + 96 \times 0.20 + 94 \times 0.20 + 87 \times 0.15 + 81 \times 0.10 + 88 \times 0.10 = 91.0$) | Gauge displays `842 / 1000` | Stitch AI generated random believable numbers for factors without calculating the weighted sum | The backend `ScoringEngine` computes the exact weighted sum: $\text{Score} = 10 \times \sum_{i=1}^6 (w_i \times F_i)$. For score = `842`, the factor scores are reconciled to: Reliability `86`, Rating `88`, Completion `85`, Response `80`, Consistency `82`, Growth `81` $\rightarrow 842.0$. |
| **Gross Monthly Revenue** | `\$3,012.50` MTD (Screen 02 `platforms`) | `\$4,280.00` Month (Screen 04 `insights`), `\$1,482.00` 7-day (Screen 01) | Different date ranges conflated (24-day MTD vs full 30-day trailing) | Explicit temporal boundaries: **7-Day Trailing** (`\$1,482.00`), **MTD (Oct 1–24)** (`\$3,012.50`), **30-Day Trailing** (`\$4,280.00`). All rollups query the same canonical ledger. |
| **Platform Set** | 4 Platforms: Upwork, Uber, Fiverr, DoorDash (Screens 01 & 02) | 3 Platforms: Upwork, Uber, Fiverr (Screen 04 breakdown lacks DoorDash) | DoorDash was dropped in the prompt for Screen 04 | Include DoorDash (`\$312.00` MTD, `\$420.00` 30D) dynamically across all aggregations, making the 4-node breakdown sum to exact totals. |
| **Review Count & CSAT** | `4.87 / 5.0` across `312 reviews` (Dashboard) | `4.93 / 5.0` (Platforms summary), `4.96` (Upwork), `4.90` (Uber), `4.85` (Fiverr), `4.92` (DoorDash) | Review counts across platform cards don't match the dashboard header | Weighted aggregate: Uber (184 reviews @ 4.90) + Upwork (42 reviews @ 4.96) + Fiverr (36 reviews @ 4.85) + DoorDash (50 reviews @ 4.92) = **312 reviews, exact weighted average: 4.89 / 5.0**. Dashboard and Platforms will display this exact calculated value. |
| **Node Sync Timestamps** | "4m ago", "12m ago", "1h ago", "2h ago" (Hardcoded text) | "Auto-sync: Enabled (15m intervals)" | Static text | Replaced by `Instant lastSyncTimestamp` formatted dynamically via `date-fns` (`formatDistanceToNow`). |

---

## 5.3 UI Data Contract Table

This table maps every target visual element to an exact backend REST endpoint, underlying JPA entity / query, current codebase status, and required implementation effort.

| UI Element | Target Value (Alex Rivera) | HTTP Method & Path | Database Source / Calculation | Code Status | Effort |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Header User Profile** | `"Alex Rivera"`, `"PRO OPERATOR"` | `GET /api/v1/workers/me` | `Worker` entity (`fullName`, `operatorTier`) | Partial (Worker exists, tier missing) | 1h |
| **Node Sync Status** | `"4 NODES ACTIVE"`, `Synced 4m ago` | `GET /api/v1/platforms/summary` | `PlatformConnection` count (`status = ACTIVE`, max `lastSyncedAt`) | Non-existent | 3h |
| **Today's Earnings** | `\$184.50`, `+12.4%`, 3 payouts | `GET /api/v1/analytics/dashboard` | `WorkActivity` sum for `current_date` vs `current_date - 1` | Non-existent | 3h |
| **Today's Completed Jobs**| `8 units`, goal `10` | `GET /api/v1/analytics/dashboard` | `COUNT(WorkActivity)` where date = today and status = COMPLETED | Non-existent | 2h |
| **Average Rating** | `4.89 / 5.0`, `312 reviews` | `GET /api/v1/analytics/dashboard` | Weighted average of `PlatformConnection.rating` by `reviewCount` | Non-existent | 2h |
| **Current GigScore** | `842 / 1000`, `Tier: Elite` | `GET /api/v1/score/latest` | `ScoreSnapshot` latest record (`score`, `tier`, `delta7Days`) | Partial (0–100 scale, no delta/tier) | 4h |
| **7-Day Earnings Chart** | 7 data points, `\$1,482.00` total | `GET /api/v1/analytics/earnings?window=7D` | `WorkActivity` grouped by `activityDate` for past 7 days | Non-existent | 3h |
| **Connected Platforms** | 4 cards (Upwork, Uber, Fiverr, DoorDash) | `GET /api/v1/platforms` | `List<PlatformConnection>` with metadata and live status | Demo-only (dummy hardcoded cards) | 4h |
| **Recent Activity Feed** | 4 latest ledger entries | `GET /api/v1/activity?page=0&size=10` | `Page<WorkActivity>` ordered by `activityDate DESC` | Non-existent | 2h |
| **6-Factor Radar/Grid** | Reliability (92), CSAT (96), Completion (94), Velocity (87), Consistency (81), Growth (88) | `GET /api/v1/score/factors` | `ScoreFactorDetail` table or JSON column linked to `ScoreSnapshot` | Partial (Only 4 factors in memory) | 4h |
| **1-Year Score Trajectory**| 12 monthly points + milestones | `GET /api/v1/score/history?window=1Y` | `ScoreSnapshot` aggregated monthly + `ScoreMilestone` table | Partial (Unpaged raw dumps) | 3h |
| **Verified Credential URL**| `https://gigscore.fi/verify/ar-842` | `GET /api/v1/credentials/{hash}` | `PublicAttestation` with Ed25519 or HMAC signature | Non-existent | 4h |
| **PDF Attestation Export** | `Download Signed Attestation` | `GET /api/v1/credentials/{hash}/pdf` | OpenPDF / iText generation of standardized credit sheet | Non-existent | 4h |
| **AI Performance Insight** | Contextual natural language tip | `GET /api/v1/insights/ai-summary` | Deterministic template synthesizer with optional Gemini fallback | Partial (Gemini called on `/chat`) | 3h |
| **Platform Breakdown** | Multi-platform yield percentages | `GET /api/v1/analytics/platforms` | `WorkActivity` grouped by `platform` over selected date range | Non-existent | 3h |

**Total Estimated Engineering Effort for Data Backing:** ~40 hours of focused full-stack development.

---

## 5.4 Claim-Honesty Audit Table

The target design includes several flashy enterprise claims and speculative fintech features that must either be backed by authentic code, reworded to reflect honest reality, or cut entirely.

| UI Claim / Widget | Location | Current Reality | Honest Assessment & Verdict | Replacement Action |
| :--- | :--- | :--- | :--- | :--- |
| **"SOC2 Type II Certified"** | Screen 02 footer | GigScore is an academic/portfolio project. We have no SOC2 audit report or trust portal. | **CUT IMMEDIATELY.** False security claims in an engineering interview or portfolio are instant red flags. | Replace with: `"Zero-Knowledge Token Storage: Platform tokens are AES-GCM-256 encrypted at rest."` |
| **"48,200 Gig Workers Indexed"** | Screen 03 header | The database has <10 seed workers. | **CUT / REWORD.** Obvious fake vanity number. | Replace with: `"Calibrated against the 2024 Gig Worker Economic Index benchmark."` |
| **"Verified Ledger Hash #9fa2-842"** | Screens 01 & 03 | Implies a blockchain or distributed ledger consensus protocol. There is no blockchain. | **REWORD & BACK WITH REAL CODE.** Do not pretend to be Web3. | Replace with: `"Cryptographic Snapshot Hash: SHA-256(workerId + score + timestamp + salt)"`. Show the real SHA-256 digest and verify it dynamically. |
| **"Bank Auto-Sweep (ACH Tonight)"** | Screen 01 dashboard | GigScore is an analytics and credit-scoring platform, not a BaaS banking core (Plaid/Stripe/Dwolla). | **CUT FROM CORE / RELABEL AS MOCK.** | Remove banking auto-sweep widget, or label as `"Sandbox Integration: Connected Checking Account (****4812)"`. |
| **"Node Sync Protocol v2.4"** | Screen 02 header | Pompous distributed systems jargon for simple REST syncing. | **REWORD.** | Change to: `"Platform Sync Protocol: Synchronized with 4 sandbox platform adapters"`. |
| **"Real-time Webhook Ingestion"** | Screen 02 card details | All connections currently polled or seeded. | **BACK WITH CODE OR REWORD.** | Provide an authentic mock webhook receiver endpoint (`POST /api/v1/webhooks/{platform}`) with webhook signature verification. |
| **"Public Attestation PDF Download"** | Screen 03 | Download button has no backend endpoint. | **BACK WITH CODE.** | Implement real server-side PDF generation via OpenPDF: a clean, branded 1-page "Gig Worker Alternative Credit Attestation" featuring the worker's score, factors, and verification QR code. |

---

## 5.5 Backend Capabilities Required to Fulfill the Target UI

To make the backend genuinely worthy of this UI without shortcuts or mocks in the presentation layer, the following 7 core backend capabilities must be engineered:

```
+------------------------------------------------------------------------------------+
|                                 CORE BACKEND PIPELINE                              |
+------------------------------------------------------------------------------------+
| 1. PLATFORM CONNECTORS                                                             |
|    - Upwork, Uber, Fiverr, DoorDash Sandbox Adapters                               |
|    - Mock OAuth consent state machine                                              |
|    - Rate-limited sync execution & idempotent webhook endpoints                    |
+------------------------------------------------------------------------------------+
                                      |
                                      v
+------------------------------------------------------------------------------------+
| 2. CANONICAL WORK ACTIVITY INGESTION                                               |
|    - Normalized Schema: (id, worker_id, platform, external_id, gross_amount,       |
|      net_amount, currency, activity_type, status, activity_date, duration_minutes,  |
|      customer_rating, completion_status)                                           |
|    - Natural unique deduplication: UNIQUE(worker_id, platform, external_id)        |
+------------------------------------------------------------------------------------+
                                      |
                                      v
+------------------------------------------------------------------------------------+
| 3. TEMPORAL ROLLUP & METRICS ENGINE                                                |
|    - Windowed aggregations: Today (1D), Trailing 7-Day (7D), MTD, Trailing 30-Day  |
|    - Mathematical primitives: Mean, Variance, Coefficient of Variation (Volatility)|
|    - Platform distribution & hourly yield calculations                             |
+------------------------------------------------------------------------------------+
                                      |
                                      v
+------------------------------------------------------------------------------------+
| 4. 6-FACTOR SCORING ENGINE (0–1000 Scale)                                          |
|    - Factor 1: Reliability (25%) -> Completion rate, dispute rate                 |
|    - Factor 2: Customer CSAT (20%) -> Weighted ratings across platforms            |
|    - Factor 3: Fulfillment SLA (20%) -> On-time completion vs commitments          |
|    - Factor 4: Response Velocity (15%) -> Inquiry response SLA minutes             |
|    - Factor 5: Income Consistency (10%) -> Normalized income variance (1 - CV)     |
|    - Factor 6: Growth Velocity (10%) -> Quarter-over-quarter gross revenue delta   |
+------------------------------------------------------------------------------------+
                                      |
                                      v
+------------------------------------------------------------------------------------+
| 5. SCORE SNAPSHOT & TIME-SERIES LEDGER                                             |
|    - Immutable append-only audit trail (`score_snapshots`)                         |
|    - SHA-256 verification digest generation                                        |
|    - Automated evaluation trigger on data sync or scheduled cron                  |
+------------------------------------------------------------------------------------+
                                      |
                                      v
+------------------------------------------------------------------------------------+
| 6. LENDER CONSENT & VERIFIED CREDENTIAL SERVICE                                    |
|    - Time-bounded, revocable consent token generator (`/verify/{token}`)           |
|    - Zero-credential public view for underwriting partners                         |
|    - High-fidelity PDF generation with tamper-evident HMAC signature               |
+------------------------------------------------------------------------------------+
                                      |
                                      v
+------------------------------------------------------------------------------------+
| 7. EXPLAINABLE INSIGHTS & ANOMALY DETECTION                                        |
|    - Deterministic rule engine: flags sudden platform bans, income cliffs, surge   |
|    - Natural language summary synthesizer (deterministic templates + Gemini API)   |
+------------------------------------------------------------------------------------+
```

---

## 5.6 Data-Source Strategy: The Sandbox Platform Connector

### The Industry Reality
Public gig platforms (Uber Driver API, DoorDash Drive, Upwork GraphQL, Fiverr) **do not offer self-service public developer APIs for individual worker data extraction**. Access requires enterprise-level Data Sharing Partnerships, Argyle/Pinwheel aggregator contracts costing thousands of dollars per month, or screen-scraping credential stuffing (which violates terms of service and breaks frequently).

### Our Strategy: Architecture-First Sandbox Connectors
Rather than relying on brittle unauthenticated scraping or fake static JSON in the React frontend, we build **first-class Sandbox Platform Connectors** in the backend:

1. **Connector Architecture:**
   * An abstract interface: `PlatformConnectorAdapter` with implementations `UberConnectorAdapter`, `UpworkConnectorAdapter`, `FiverrConnectorAdapter`, `DoorDashConnectorAdapter`.
   * Standard sync lifecycle: `initiateConnection()`, `exchangeToken()`, `fetchActivities(sinceTimestamp)`, `fetchProfileMetrics()`.
2. **Deterministic Seed Mode & Live Mode:**
   * When a worker clicks `"Connect Upwork"`, they are redirected to an in-app OAuth sandbox consent page: `"GigScore Sandbox OAuth — Upwork Development Environment"`.
   * The user approves access, generating an authorization code that is exchanged via `POST /api/v1/platforms/{platform}/authorize` for an access token.
   * The backend connector triggers a deterministic background sync that ingests canonical historical activities.
3. **Webhook Simulation:**
   * Developers or demo evaluators can hit `POST /api/v1/sandbox/simulate-job` with payload `{ "platform": "UBER", "amount": 42.50, "rating": 5.0 }` to demonstrate live ingestion, instant score recalculation, and UI update via polling or Server-Sent Events (SSE).
4. **Honesty in the Presentation:**
   * Every platform card displays an elegant badge: `[SANDBOX ADAPTER]`.
   * In an interview, you say: *"Because gig platforms guard worker data behind closed enterprise agreements, I architected this using the Adapter Pattern with deterministic sandbox connectors. Swapping this out for an enterprise aggregator like Argyle or Finch requires changing only the adapter implementation without touching the domain or scoring engine."*

---

## 5.7 Demo Persona ("Alex Rivera") & Contrasting Personas

To prove the scoring engine handles diverse financial realities without arbitrary bias, the database seeder will populate **four distinct, fully-fleshed worker personas**:

### Primary Showcase Persona: "Alex Rivera" (Top-Tier Hybrid Gig Operator)
* **Demographics:** Full-time independent operator, active 24 months across 4 platforms (Upwork web development + Uber weekend surge driver + Fiverr design gigs + DoorDash off-peak deliveries).
* **Financial Profile:**
  * Trailing 12-month gross: `\$52,400.00`.
  * Trailing 30-day gross: `\$4,280.00`.
  * Income Volatility: Low (CV = 0.18).
  * Completion Rate: `98.6%`.
  * Average CSAT: `4.89 / 5.0` (312 total reviews).
* **Computed Score:** **842 / 1000** (`Tier 1: Elite Operator`).
* **Purpose:** Matches the exact target UI screens in `docs/design/`, ensuring 100% visual fidelity during walkthroughs.

### Persona 2: "Maya Lin" (High-Performing Newcomer)
* **Demographics:** Freelance copywriter and translator, active 3 months on Upwork and Fiverr.
* **Financial Profile:**
  * Trailing 90-day gross: `\$7,200.00`.
  * Ratings: Exceptional (`5.0 / 5.0`, 24 reviews).
  * Completion: `100%`.
  * Tenure Penalty: Lacks 12-month history; limited activity density.
* **Computed Score:** **695 / 1000** (`Tier 3: Emerging Operator`).
* **Purpose:** Proves the scoring algorithm accounts for historical depth and does not grant unearned elite status to unproven accounts.

### Persona 3: "Dave Miller" (High-Volume, Volatile Rideshare Driver)
* **Demographics:** Full-time Uber & Lyft driver, active 18 months.
* **Financial Profile:**
  * Trailing 12-month gross: `\$44,000.00`.
  * High Earnings Spikes followed by 2-week zero-income gaps (CV = 0.62).
  * Cancellation Rate: `8.4%` (frequent unassigned dispatches).
  * Ratings: `4.72 / 5.0` (acceptable but below platform averages).
* **Computed Score:** **580 / 1000** (`Tier 4: Moderate Risk`).
* **Purpose:** Demonstrates credit-risk sensitivity—showing lenders that raw earnings volume alone does not equate to reliability if cash flow is erratic.

### Persona 4: "Priya Sharma" (Fraudulent / Anomaly Persona)
* **Demographics:** Suspicious account with rapid manual inputs.
* **Profile:** Injected 20 identical high-value jobs within 4 minutes; overlapping GPS trip timestamps; sudden platform suspension alert.
* **Computed Score:** **Anomaly Flagged / Score Frozen** with automated underwriting risk flags.
* **Purpose:** Demonstrates the deterministic anomaly detection engine and lender protection capabilities.

---

## 5.8 Frontend Gap Analysis & Architecture Decision: Re-skin vs Rebuild

### Current Frontend State (`frontend/src/`):
* Plain, unstyled Tailwind starter with basic raw inputs:
  * 4 simple pages: `Login`, `Register`, `Dashboard` (raw summary + manual "Add Gig" form + history list), `Lenders` (worker list for lenders).
  * Minimal styling: Standard Tailwind indigo buttons, basic HTML tables, raw cards.
  * Zero charts, zero telemetry rings, zero factor cards, no platform status badges, no navigation sidebar.

### Target Frontend State (`docs/design/`):
* Sophisticated dark/emerald fintech aesthetic:
  * Glassmorphism surfaces (`bg-slate-900/60`, `backdrop-blur-md`, `border-slate-800`).
  * Custom typographic hierarchy using Inter and JetBrains Mono for monetary and hash values.
  * Interactive SVG circular gauges, multi-series area charts, animated sync indicators, and modular data widgets.

### Architectural Decision: Clean Component Rebuild
Attempting to incrementally patch the existing 4 files into the target UI will produce spaghetti code, CSS collisions, and broken layout constraints. 

**Decision: Modular Rebuild using React 18 + Tailwind CSS + Lucide Icons + Recharts.**
1. **Design Tokens:** Extract color palette (`emerald-400`, `slate-900`, `slate-950`), borders, and font definitions from `docs/design/DESIGN.md` into `frontend/tailwind.config.js`.
2. **Shared Layout:** Implement `AppLayout.jsx` with the collapsible 64px/240px sidebar, global top bar, user status chip, and breadcrumb router.
3. **Component Decomposition:**
   * `MetricCard.jsx` (reusable KPI widget with trends).
   * `ScoreGauge.jsx` (SVG circle meter with gradient stroke).
   * `FactorBreakdown.jsx` (horizontal bar meters with weighting).
   * `EarningsChart.jsx` (responsive time-series area chart via Recharts).
   * `PlatformNodeCard.jsx` (status indicator, last sync, platform logo).
   * `ActivityTable.jsx` (paginated, filtered ledger stream).
4. **State Management & Data Fetching:**
   * Replace ad-hoc `useEffect` fetch calls with standardized React hooks or TanStack Query (or clean custom API fetchers with loading and error boundaries).

---

## 5.9 Backend-First Iteration Plan (Slice 0 through Slice 5)

We follow a strict **Backend-First, Outside-In** engineering sequence. Every slice produces testable, verified backend endpoints before corresponding frontend components are rendered.

```
+-----------------------------------------------------------------------------------+
| SLICE 0: FOUNDATION & SECURITY CLEANUP                                            |
| - Flyway DB migrations (PostgreSQL/MySQL/H2 parity).                              |
| - Sanitize hardcoded secrets & configure environment variables.                   |
| - Domain model refactor: Worker, PlatformConnection, WorkActivity, ScoreSnapshot. |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| SLICE 1: PLATFORM ADAPTERS & CANONICAL INGESTION                                  |
| - Sandbox platform connector framework (Uber, Upwork, Fiverr, DoorDash).         |
| - Idempotent ingestion service with composite unique deduplication.               |
| - Deterministic 4-persona DB seeder (Alex Rivera 12-month historical dataset).    |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| SLICE 2: 6-FACTOR SCORING ENGINE & TIME-SERIES LEDGER                             |
| - Implement 0-1000 scale weighted formula with 6 factors.                         |
| - Deterministic anomaly evaluator (fast-path rules).                              |
| - Snapshot persistence with SHA-256 tamper-evident hash calculation.             |
| - REST endpoints: `GET /api/v1/score/latest`, `GET /api/v1/score/history`.        |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| SLICE 3: ANALYTICS, TIME-SERIES ROLLUPS & EXPLAINABILITY                          |
| - Aggregations service: Today, 7D, MTD, 30D revenue and job rollups.              |
| - Multi-platform yield breakdown & hourly surge analysis.                         |
| - Explainable recommendations generator (template engine + Gemini fallback).     |
| - REST endpoints: `GET /api/v1/analytics/dashboard`, `GET /api/v1/analytics/grid`.|
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| SLICE 4: VERIFIABLE CREDENTIAL & LENDER ATTRIBUTIONS                              |
| - Scoped, revocable verification link generation (`/verify/{token}`).             |
| - Zero-knowledge public underwriting endpoint.                                    |
| - Server-side PDF generation service (OpenPDF branded attestation sheet).         |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| SLICE 5: FRONTEND TARGET UI IMPLEMENTATION                                        |
| - Tailwind token integration from `DESIGN.md`.                                    |
| - Reusable component library (Gauges, Charts, Cards, Activity Ledger).            |
| - Complete page assembly: Dashboard, Platforms, GigScore, Insights, Verify.      |
| - End-to-end user testing against Alex Rivera seeded persona.                     |
+-----------------------------------------------------------------------------------+
```

---

## 5.10 Cut / Defer List

To maintain engineering integrity and guarantee delivery within realistic timelines, the following items from the target design are explicitly **cut** or **deferred**:

### 1. Explicitly Cut (Never Build)
* **Real Proprietary Gig Platform OAuth:** Cut. Enterprise partner access does not exist for independent developers. Use sandbox adapters exclusively.
* **SOC2 Type II & Security Compliance Badges:** Cut. Deceptive claims are completely unacceptable.
* **Automatic Bank ACH Sweep:** Cut. Banking rails and money transmitter licenses are out of scope for an alternative scoring engine.
* **Cryptocurrency / Blockchain Consensus Nodes:** Cut. "Ledger verification" is implemented using standard, tamper-evident SHA-256 cryptographic signatures.

### 2. Deferred to Phase 2 (Post-MVP Enhancements)
* **Live Server-Sent Events (SSE) Webhook Streaming:** Defer. Initial release will use standard REST polling (e.g. 15s interval or manual "Refresh All").
* **Plaid Checking Account Balance Matching:** Defer. Focus strictly on platform-verified gross and net earnings.
* **Custom White-Label Lender Portal:** Defer. Deliver a public, tokenized read-only verification page and downloadable PDF attestation instead.

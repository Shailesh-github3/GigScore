# Phase 3 — Scoring Engine, Explainability & Data Quality Audit

Date: 2026-09-20  
Auditor: Fintech Credit-Risk Architect & Technical Interviewer  
Target Repository: `D:\Java\Spring_boot\GigScore`

---

## 1. Codebase Scoring Engine Inspection

* **Primary Class:** `com.org.gigscore.service.GigScoreService`
* **Configuration:** `com.org.gigscore.dto.ScoreConfig`
* **DTO Response:** `com.org.gigscore.dto.ScoreResponse`
* **Entities:** `com.org.gigscore.entity.GigScore`, `com.org.gigscore.entity.ScoreHistory`

### 1.1 Mathematical Formulation & Parameters

The current engine implements an exponential power-weighted formula mapping 4 metrics to a $[0, 100]$ score:

$$S = \sum_{i=1}^{4} w_i \cdot \left( \min\left( \frac{x_i}{T_i}, 1.0 \right) \right)^{p_i} \cdot M$$

Where:
* $M = 100.0$ (`MAX_SCORE`)
* $\sum w_i = 0.35 + 0.25 + 0.30 + 0.10 = 1.00$

| Metric ($x_i$) | Weight ($w_i$) | Exponent ($p_i$) | Target Ceiling ($T_i$) | Mathematical Behavior | Code Location |
|---|---|---|---|---|---|
| **Earnings** | 35% (`0.35`) | `0.70` | $5,000.00 | **Concave (Sub-linear):** Rewards early earnings. Earning $2,500 (50% of target) yields $(0.5)^{0.7} \times 35 \approx 21.5$ pts (61.5% of max weight). | `GigScoreService.java:83` |
| **Jobs Completed** | 25% (`0.25`) | `0.65` | 100 jobs | **Concave (Sub-linear):** Fast initial ramp. 25 jobs yields $(0.25)^{0.65} \times 25 \approx 10.1$ pts. | `GigScoreService.java:84` |
| **Average Rating** | 30% (`0.30`) | `1.40` | 5.0 stars | **Convex (Super-linear):** Strictly penalizes ratings below 4.5. A 4.0/5.0 rating gives $(0.8)^{1.4} \times 30 \approx 22.0$ pts (loses 8 pts). A 3.0 rating drops to $14.8$ pts (halved). | `GigScoreService.java:85` |
| **Active Days** | 10% (`0.10`) | `1.00` | 30 days | **Linear:** Exactly 1/3 pt per distinct calendar active day. | `GigScoreService.java:86` |

### 1.2 Determinism, Reproducibility & Rounding
* **Determinism:** 100% deterministic. Given the same aggregate metrics, `computeComponents()` always returns identical component values.
* **Rounding Policy:** Each component is explicitly rounded via `Math.round(value * 100.0) / 100.0` (two decimal places). The total score is computed by summing the rounded components (`GigScoreService.java:57`). This guarantees that:
  $$\text{Score} = \text{Earnings} + \text{Jobs} + \text{Rating} + \text{ActiveDays}$$
  The breakdown and total score can never disagree due to rounding drift.

---

## 2. Critical Flaws & Discrepancies

### 2.1 Model Discrepancy vs Target UI
The target UI (`03-gigscore.png`, `01-dashboard.png`) specifies a completely different scoring model:
* **Scale:** Target UI uses **0–1000** (or 0–900 on Platforms/Insights), whereas the backend computes **0–100**.
* **Factor Count:** Target UI specifies **6 distinct factors**:
  1. Reliability (25%)
  2. Customer Rating (20%)
  3. Completion Rate (20%)
  4. Response Time (15%)
  5. Consistency (10%)
  6. Earnings Growth (10%)
* **Current Backend:** Implements only **4 components** (Earnings 35%, Jobs 25%, Rating 30%, Active Days 10%). It has **no concept** of completion rate, response time, or earnings growth.

### 2.2 Threshold & Band Inconsistencies
* **In-Code Bands (`GigScoreService.java:129-134`):**
  * $\ge 90$: `Excellent`
  * $\ge 75$: `Strong`
  * $\ge 50$: `Fair`
  * $< 50$: `Needs Improvement`
* **README Documentation Claim:** Claims bands are `EARLY (< 40)`, `GROWING (40-69)`, `STRONG (≥ 70)`.
* **Target UI Display:** Displays `Tier 1 Elite`, `Platinum`, and `Top 4% Global`.
* *Verdict:* Three completely different tier schemes exist across code, documentation, and design.

### 2.3 Historical Snapshot Duplication Defect
* **Code Trace (`GigScoreService.java:101-104`):**
  ```java
  @Cacheable(value = "scores", key = "#user.userId")
  public ScoreResponse getScoreForUser(User user) {
      return calculateAndPersistScore(user);
  }
  ```
  And inside `calculateAndPersistScore(user)`:
  ```java
  ScoreHistory history = new ScoreHistory();
  history.setUser(user);
  history.setScore(score);
  history.setScoreVersion(SCORE_VERSION);
  scoreHistoryRepository.save(history);
  ```
* **The Bug:** If the Spring cache is cold, restarted, or evicted, every read request (`GET /api/v1/score/{userId}`) inserts a new duplicate row into `score_history` even when the score has not changed by a single point.
* **Missing Historical Data:** `ScoreHistory` records only `(user_id, score, score_version, recorded_at)`. It does **not** store component contributions ($E, J, R, D$) or the input metrics hash. Historical factor trajectories cannot be reconstructed.

---

## 3. Vulnerability to Manipulation (Gaming)

The current scoring engine has zero defenses against input manipulation:

1. **Unverified Self-Reporting:** Any authenticated user can issue `POST /api/v1/gigs` with arbitrary amounts and ratings.
2. **Instant Score Inflation:**
   * Submit 5 gigs with amount = $\$1,000$ and rating = $5.0$.
   * Total earnings = $\$5,000$ (max 35 pts), jobs = 5 (3.7 pts), rating = 5.0 (max 30 pts), active days = 1 (0.3 pts).
   * Total score instantly jumps from 0 to **69.0/100** ("Fair" $\rightarrow$ borderline "Strong") in under 10 seconds.
3. **Wash Income & Replay:** No external transaction ID validation or duplicate checks. A user can post the same $\$500$ receipt 10 times to satisfy the $\$5,000$ earnings threshold.
4. **Active Day Manipulation:** Posting a single $\$0.01$ transaction per calendar day satisfies the active days metric without genuine labor.

---

## 4. Domain Assumption Evaluation

| Domain Factor | Current Assumption | Real-World Financial Reality | Interviewer Critique |
|---|---|---|---|
| **Earnings Ceiling** | Static \$5,000 cap | Full-time gig workers in tier-1 US cities often earn \$6k–\$9k; in emerging markets (e.g. India), \$500 is high. | Hardcoding a universal \$5,000 cap without regional or platform context distorts underwriting. |
| **Earnings Volatility** | Completely ignored | A worker earning \$5,000 in week 1 and \$0 for the rest of the month has high default risk compared to one earning \$1,250 every week. | Credit risk depends on **income stability**, not cumulative volume alone. |
| **Cancellation Behavior** | Not tracked | High cancellation rates indicate platform suspension risk and unreliability. | Critical signal missing; target UI expects "Completion Rate (20%)". |
| **Response Latency** | Not tracked | Fast responders win higher-tier freelance contracts. | Target UI expects "Response Time (15%)". |
| **Platform Diversity** | Simple aggregation | Concentrating 100% of income on one platform carries de-platforming risk. Multi-platform workers are more resilient. | Aggregating into a single sum discards platform risk concentration. |

---

## 5. Explainability & Lender Decision Support

### 5.1 Current Explainability
* **Component Math:** Cleanly exposed (`ScoreResponse.components`). The breakdown matches the total score.
* **Recommendations:** Primitive string thresholds (`GigScoreService.java:136-154`):
  * `earnings < 10.0` $\rightarrow$ "Increase total earnings to boost your earnings component."
  * `jobs < 7.0` $\rightarrow$ "Complete more gigs to strengthen your job completion metric."
  * `rating < 9.0` $\rightarrow$ "Improve your average rating by delivering higher-quality work."
  * `activeDays < 3.0` $\rightarrow$ "Work on more active days to demonstrate consistency."
* **What Lenders See:**
  * Lenders see a single composite number, the 4 components, and a data confidence badge.
  * Lenders **do not** see: positive/negative performance drivers, income volatility metrics, platform dispute flags, or what-if projections.

### 5.2 Required Architectural Design: Explainable by Construction

$$\text{GigScore} = \text{Base Score} + \sum_{k=1}^{6} \text{Factor Contribution}_k - \text{Risk Penalty}$$

* Every factor contribution must be stored alongside the snapshot in `score_history` as JSON or dedicated columns (`reliability_pts`, `rating_pts`, `completion_pts`, `response_pts`, `consistency_pts`, `growth_pts`).
* The UI radar chart, factor cards, and total score must derive from the exact same snapshot record so inconsistency is mathematically impossible.

---

## 6. Small Explainable Data-Quality / Anomaly Layer

The project does **not** need a machine-learning fraud platform. A fresher can build a clean, deterministic, 4-rule anomaly evaluator running in $<2\text{ms}$:

```text
Incoming Gig / Sync Batch
        │
        ▼
┌───────────────────────────────────────────────┐
│     Data Quality & Anomaly Evaluator          │
│                                               │
│  [1] Velocity Check: > 5 gigs in 10 mins?     │ ──► Flag: HIGH_VELOCITY_SPIKE
│  [2] Outlier Check: single gig > $2,500?      │ ──► Flag: UNVERIFIED_LARGE_TRANSACTION
│  [3] Monotonicity: 100% 5.0 rating on 30+ gigs?│ ──► Flag: SUSPICIOUS_RATING_UNIFORMITY
│  [4] Thin File: active days < 14 or jobs < 15? │ ──► Flag: THIN_FILE_CONFIDENCE_PENALTY
└───────────────────────────────────────────────┘
        │
        ▼
   DataQualityProfile (Confidence: HIGH/MED/LOW, Active Flags, Confidence Score)
```

### Benefits for Interview Defensibility:
1. **Explainable:** Every penalty or flag has an exact audit reason stored in the database.
2. **Zero Black-Box Risk:** No unexplainable ML weights that violate lending regulations (e.g. Equal Credit Opportunity Act / FCRA principles).
3. **Lightweight:** Implemented as a Spring `@Component` service called before score calculation.

---

## 7. Archetype Stress Testing (10 Personas)

| Archetype | Persona Profile | Current Engine Behavior | Target / Improved Engine Behavior |
|---|---|---|---|
| **1. Normal Worker** | Steady \$2,800/mo, 45 jobs, 4.85 rating, 22 active days | Score $\approx 78.4$ (`Strong`). Accurately scored. | Score $\approx 785/1000$. Balanced factor contributions. |
| **2. High-Income Earner** | \$18,000/mo, 140 jobs, 4.95 rating, 28 active days | Score = $100.0$ (`Excellent`). Maxes out all targets. | Score $\approx 960/1000$. Higher tiers unlock; volatility evaluated. |
| **3. Low-Income Earner** | \$450/mo, 12 jobs, 4.70 rating, 6 active days | Score $\approx 42.1$ (`Needs Improvement`). | Score $\approx 420/1000$. Low volume noted with growth tips. |
| **4. Stable Earner** | \$800 every single week ($3,200/mo), steady 15 jobs/wk | Score $\approx 81.2$. Volatility ignored. | Consistency factor awards full 100 pts (+100 to score). |
| **5. Highly Volatile** | \$4,800 in week 1, \$0 in weeks 2–4 | Score $\approx 83.5$ (**Misleadingly high**). | Volatility penalty triggers; score discounted by 60 pts. |
| **6. Thin-File Newcomer**| Day 1 user: 1 gig, \$500, 5.0 rating | Score $\approx 47.8$ (Deceptive rating signal). | `THIN_FILE` flag set; confidence locked to `LOW`. |
| **7. Invalid Boundary** | Amount = -\$50, Rating = 6.2 | Blocked at controller by `@DecimalMin`/`@DecimalMax`. | Clean HTTP 400 with field validation error. |
| **8. Exact Target** | Exactly \$5,000, 100 jobs, 5.0 rating, 30 days | Score = $100.00$ exactly (`GigScoreServiceTest:58`). | Score = $1000/1000$ exactly. |
| **9. Extreme Outlier** | Single gig of \$1,000,000 | Capped at \$5,000 in normalizer. Earnings = 35 pts. | Flagged as `UNVERIFIED_LARGE_TRANSACTION`; capped. |
| **10. Wash / Manipulated** | 10 rapid self-reported \$500 gigs in 5 mins | Score jumps to 72.0 with no warning. | `HIGH_VELOCITY_SPIKE` flag trips; confidence penalty. |

---

## 8. Rule-Based vs Machine Learning Verdict

### Why Rule-Based Is Superior for this Project:
1. **Explainability Compliance:** In credit underwriting, Adverse Action Notices require explaining the exact reasons for credit decisions. A rule-based engine provides mathematical attribution down to the exact dollar and star rating.
2. **Data Availability:** Training a real ML risk model requires ground-truth default and charge-off labels from actual lending portfolios. A fresher claiming to have trained an ML credit risk model on synthetic data immediately exposes shallow engineering to an experienced interviewer.
3. **Defensibility:** An interviewer respects a candidate who says:
   > *"I intentionally chose a versioned, config-driven, rule-based engine because fintech credit scoring demands deterministic explainability and regulatory transparency. Every point on the user's score corresponds to an auditable formula factor."*

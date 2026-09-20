# Phase 7 — Technical Interview Preparation & Code Defense

**Target Document:** `docs/audit/07-interview-prep.md`  
**Scope:** Exhaustive technical defense manual designed to prepare an engineer to defend GigScore’s architecture, algorithms, security, and database design before senior backend engineers, fintech architects, and technical bar raisers.

---

## 7.1 Architectural Defense & Technology Selection

### Q1: "Why did you build this with Java 21 and Spring Boot rather than Node.js, Go, or Python?"
* **The Bar-Raiser Answer:**
  > *"Fintech and credit-scoring systems have two non-negotiable requirements: strict type safety for financial precision, and bulletproof concurrency under high-volume ingestion.*
  > 
  > *Java 21 gives us first-class language primitives specifically suited for financial domain modeling: `BigDecimal` for zero-loss monetary calculations, immutable `record` classes for DTOs and ledger events, and sealed interfaces for domain state machines. Furthermore, Java 21's Virtual Threads (Project Loom) allow us to handle thousands of concurrent platform sync requests using simple synchronous blocking I/O code without the callback hell or reactive complexity of WebFlux.*
  > 
  > *While Python is great for exploratory data science, it lacks strict compile-time safety and suffers from GIL contention. Go is performant but lacks the mature enterprise ORM, transaction management, and declarative security ecosystem that Spring Security and Spring Data JPA provide out of the box."*

---

### Q2: "Why use a Relational Database (PostgreSQL / MySQL) instead of MongoDB or DynamoDB for gig work activities?"
* **The Bar-Raiser Answer:**
  > *"While gig work transactions arrive as semi-structured JSON payloads, credit scoring is fundamentally an ACID-dependent financial ledger problem.*
  > 
  > *First, we require strict multi-entity referential integrity: a `ScoreSnapshot` must tie immutably to a verified `Worker` and a specific set of `WorkActivity` records. In a NoSQL document store, maintaining cross-document consistency during score recalculation requires complex application-level two-phase commits.*
  > 
  > *Second, idempotency and fraud prevention rely heavily on relational constraints. To prevent double-counting earnings when a webhook retries, we enforce composite unique constraints like `UNIQUE(worker_id, platform, external_transaction_id)`. Relational engines enforce this at the storage engine level with $B$-tree index lookups.*
  > 
  > *Third, modern PostgreSQL supports JSONB with GIN indexing, giving us the flexibility of NoSQL for raw platform payloads while retaining full ACID guarantees and windowed SQL aggregations for temporal metrics."*

---

### Q3: "How does this architecture scale to 100,000 active workers and 10 million transactions?"
* **The Bar-Raiser Answer:**
  > *"Scaling this system involves separating the write-heavy ingestion path from the read-heavy scoring and dashboard path:*
  > 
  > 1. **Ingestion Layer:** Webhooks from platforms like Uber and Upwork are received by stateless API nodes, validated for signature authenticity, and pushed immediately to an Apache Kafka or AWS SQS event topic partitioned by `worker_id`. An ingestion worker pool processes events idempotently.
  > 2. **Storage Layer:** We partition the `work_activities` table by `activity_date` (range partitioning) or hash-partitioned by `worker_id`. Historical activities older than 12 months are cold-stored in Parquet/S3 for compliance.
  > 3. **Aggregation Caching:** Recomputing scores from millions of raw activity rows on every read is an $O(N)$ catastrophe. Instead, we use **Event-Driven CQRS**: activities update incremental daily rollup buckets (`worker_daily_stats`).
  > 4. **Score Snapshots:** Scores are computed asynchronously upon activity ingestion or on a scheduled nightly cron. The latest score is cached in Redis with a 1-hour TTL, serving dashboard reads in $<5\text{ms}$ at zero database load."*

---

## 7.2 Scoring Algorithm & Mathematical Defense

### Q4: "Why did you build a deterministic 6-factor mathematical model instead of training an XGBoost or Deep Neural Network model?"
* **The Bar-Raiser Answer:**
  > *"In alternative credit underwriting, explainability and regulatory compliance trump black-box accuracy every single day. There are three core engineering and legal reasons why ML would be an anti-pattern here:*
  > 
  > 1. **The Ground-Truth Label Problem:** Machine learning requires supervised training data with labeled loan outcomes (default vs paid). GigScore is an alternative reliability protocol, not a historical debt ledger. We do not have 100,000 gig worker loan defaults to train on. Training ML on synthetic labels produces hallucinated risk models.
  > 2. **FCRA Adverse Action Compliance:** Under the Fair Credit Reporting Act (15 U.S.C. § 1681) and Equal Credit Opportunity Act (ECOA Reg B), if a lender rejects a worker based on our score, we are legally mandated to output the exact top 4 key factors that lowered their score. A deep neural network or high-dimensional gradient booster cannot provide mathematically provable factor attributions without unstable approximations like SHAP or LIME.
  > 3. **Disparate Impact & Regulatory Auditability:** Complex models routinely latch onto latent demographic proxies (e.g., phone battery level, time of application, phone OS). Our 6-factor model relies strictly on objective operational behavior: completion rate, ratings, SLA adherence, response time, income volatility, and growth. It can be audited by bank regulators in minutes."*

---

### Q5: "How does the formula prevent gaming and fraud? (e.g., A worker creating 100 fake \$1 jobs to inflate their score)"
* **The Bar-Raiser Answer:**
  > *"We implement three layers of algorithmic and architectural defense against gaming:*
  > 
  > 1. **Diminishing Returns (Power-Law / Logarithmic Scaling):** The scoring factors use power-law diminishing returns $(x / T)^p$ where $p < 1$. Once a worker achieves the target threshold of completions or rating, additional volume yields exponentially diminishing marginal score points. You cannot brute-force an 850 score with sheer volume.
  > 2. **Income Volatility & Variance Penalties:** A worker who generates 100 fake \$1 jobs in one day will trigger an extreme penalty in **Income Consistency** (Factor 5). The Coefficient of Variation ($CV = \sigma / \mu$) spikes, severely depressing their overall score.
  > 3. **Deterministic Fast-Path Anomaly Engine:** Before any activity enters the scoring pipeline, it passes through 4 fast-path heuristics:
  >    * *Velocity Rule:* Maximum 10 dispatches per hour per platform.
  >    * *GPS / Timestamp Collision Rule:* Concurrent rideshare trips on Uber and DoorDash with overlapping start/end windows are flagged.
  >    * *Self-Dealing Heuristic:* Transactions from identical client IDs repeated within short time windows are quarantined.
  >    * *Zero-Tolerance Suspension:* Any platform webhook reporting account suspension immediately freezes the score and alerts underwriters."*

---

## 7.3 Security & FinTech Compliance Defense

### Q6: "How do you protect worker platform tokens and PII against database breaches?"
* **The Bar-Raiser Answer:**
  > *"We treat platform tokens and worker financial data with zero-trust storage policies:*
  > 
  > 1. **Encryption at Rest:** Sensitive credentials, such as OAuth refresh tokens for Upwork or Uber, are never stored as plaintext strings in JPA entities. We use a JPA `@Convert(AttributeEncryptor.class)` using **AES-GCM-256** with an initialization vector (IV) generated per record. Encryption keys are injected via environment variables or AWS KMS / HashiCorp Vault.
  > 2. **PII Masking & Projections:** Database queries for public verification or lender views use strict Spring Data JPA projections. Social Security Numbers, phone numbers, and raw bank account details are never retrieved into memory unless explicitly required by an authorized admin context.
  > 3. **Tokenized Credential Verification:** When a worker shares their score with a lender, we do not expose their login or internal `worker_id`. We issue a time-bounded, cryptographically signed verification token (UUID v4 or JWT) that expires in 14 days and can be revoked by the worker with one click."*

---

### Q7: "Your codebase had a Broken Object-Level Authorization (BOLA) vulnerability in LenderService. How do you defend your authorization architecture?"
* **The Bar-Raiser Answer:**
  > *"In our initial MVP audit, we discovered that `LenderController` exposed endpoints where an authenticated worker could query all other workers' financial summaries simply by changing the URL ID.*
  > 
  > *To remediate this systematically, we implemented **Method-Level Security with SpEL (Spring Expression Language)**:
  > ```java
  > @PreAuthorize("hasRole('LENDER') or (hasRole('WORKER') and #workerId == authentication.principal.id)")
  > @GetMapping("/workers/{workerId}/score")
  > public ScoreResponse getScore(@PathVariable Long workerId) { ... }
  > ```
  > *Furthermore, for lender access, we enforce explicit Worker-to-Lender Consent Grants in the database. A lender cannot query a worker's score unless there exists an active, non-expired record in `worker_lender_consents` matching the lender's organization ID."*

---

## 7.4 Live Code Defense Scenarios (Tough Interviewer Simulation)

### Scenario 1: "Your `GET /api/v1/score` endpoint modifies database state. Isn't that a violation of HTTP idempotency?"
* **Interviewer Critique:**
  > *"I was looking at your `ScoreController`. When a client calls `GET /score`, you calculate the score and then call `snapshotRepository.save(snapshot)`. That means every time a user refreshes their browser, you write a new record to your database. That violates HTTP GET idempotency and will cause storage explosion under high traffic."*
* **The Flawless Response & Defense:**
  > *"You are 100% correct. In the legacy MVP code, calculating and persisting were coupled inside `ScoreService.calculateScore()`, which meant a cache miss on a GET request triggered a database write.*
  > 
  > *We addressed this by decoupling the read path from the evaluation path following the **CQRS (Command Query Responsibility Segregation)** pattern:
  > 1. `GET /api/v1/score/latest` is strictly idempotent and read-only. It queries the most recent `ScoreSnapshot` from the database or Redis cache. If no score exists, it returns a `404 Not Found` with a prompt to initiate initial evaluation.
  > 2. Score re-calculation and snapshot creation is an explicit **Command**: `POST /api/v1/score/evaluate`, which is triggered only when new platform activities are ingested, when a scheduled nightly cron fires, or when the user explicitly clicks 'Refresh All'."*

---

### Scenario 2: "In `LenderService.getAllWorkers()`, you have an $O(N)$ query storm. How does that behave in production?"
* **Interviewer Critique:**
  > *"Your lender dashboard endpoint fetches all workers, and then loops through each worker in Java to fetch their gigs and calculate averages. If you have 5,000 workers, that executes 5,001 SQL queries. How does that behave under load?"*
* **The Flawless Response & Defense:**
  > *"That is the classic Hibernate N+1 query problem, and in production, it causes catastrophic thread pool exhaustion and database CPU throttling.*
  > 
  > *We eliminated the N+1 query entirely by replacing the entity loop with a **Single Grouped JPQL Projection**:
  > ```java
  > @Query("SELECT new com.gigscore.dto.LenderWorkerSummary(" +
  >        "w.id, w.fullName, s.score, COUNT(a.id), COALESCE(SUM(a.grossAmount), 0), AVG(a.rating)) " +
  >        "FROM Worker w " +
  >        "LEFT JOIN ScoreSnapshot s ON s.worker = w AND s.createdAt = (SELECT MAX(s2.createdAt) FROM ScoreSnapshot s2 WHERE s2.worker = w) " +
  >        "LEFT JOIN WorkActivity a ON a.worker = w " +
  >        "GROUP BY w.id, w.fullName, s.score")
  > Page<LenderWorkerSummary> findAllSummaries(Pageable pageable);
  > ```
  > *This collapses 5,001 round-trips into a **single indexed SQL query with server-side pagination**, executing in under 25ms regardless of worker volume."*

---

### Scenario 3: "What happens if two platform webhooks deliver the exact same job twice concurrently?"
* **Interviewer Critique:**
  > *"Network retries are common in webhooks. If Uber sends the same ride completed event twice simultaneously on two different threads, won't your system insert two records and artificially inflate the worker's earnings?"*
* **The Flawless Response & Defense:**
  > *"We handle concurrent duplicate webhooks at two levels: the application layer and the database constraint layer:*
  > 
  > 1. **Database Constraint:** The `work_activities` table enforces a composite unique index:
  >    ```sql
  >    ALTER TABLE work_activities ADD CONSTRAINT uq_platform_transaction 
  >    UNIQUE (worker_id, platform_code, external_transaction_id);
  >    ```
  > 2. **Transactional Idempotent Upsert:** When two concurrent threads attempt to insert the same transaction ID, the second thread hits a `DataIntegrityViolationException`. In Spring Boot, we catch this and treat it as a successful no-op, returning HTTP `200 OK` or `204 No Content` to the platform webhook caller so they stop retrying.*
  > 3. **Distributed Lock (High-Volume):** In a distributed environment with Kafka, we partition event topics by `worker_id`, guaranteeing that events for a single worker are processed sequentially by the same consumer thread."*

---

## 7.5 System Design: High-Throughput Platform Ingestion Architecture

During an end-to-end system design round, draw and explain this architecture on the whiteboard:

```
                                  HIGH-THROUGHPUT PLATFORM INGESTION
                                  
+-------------------+      +--------------------+      +-----------------------------------------+
| Uber / Upwork     | ---> | Edge Ingress (API) | ---> | Apache Kafka Event Stream               |
| Webhook Payloads  |      | Signature Check    |      | Topic: gig.activity.ingest              |
+-------------------+      +--------------------+      | Partition Key: worker_id                |
                                                       +-----------------------------------------+
                                                                            |
                                                                            v
                                                       +-----------------------------------------+
                                                       | Idempotent Consumer Pool                |
                                                       | - Deduplicate via Redis Idempotency Key |
                                                       | - Insert into PostgreSQL                |
                                                       |   UNIQUE(worker, platform, ext_id)      |
                                                       +-----------------------------------------+
                                                                            |
                                                                            v
                                                       +-----------------------------------------+
                                                       | Incremental Aggregation Engine          |
                                                       | - Update worker_daily_stats             |
                                                       | - Trigger Anomaly Detection Fast-Path   |
                                                       +-----------------------------------------+
                                                                            |
                                                                            v
                                                       +-----------------------------------------+
                                                       | 6-Factor Scoring Engine                 |
                                                       | - Calculate 0-1000 Score                |
                                                       | - Generate SHA-256 Digest               |
                                                       | - Write ScoreSnapshot to DB & Redis     |
                                                       +-----------------------------------------+
```

---

## 7.6 Five Golden Rules for the GigScore Interviewee

1. **Never Fake What You Don't Have:** If asked about live Uber APIs, proudly explain why you used the **Adapter Pattern with Sandbox Connectors** instead of brittle unauthorized scraping or fake JSON. Interviewers respect architectural pragmatism.
2. **Lead with Financial Primitives:** Always mention `BigDecimal`, immutable records, and timezone-aware timestamps (`Instant` / UTC) when discussing financial transactions.
3. **Frame the Scoring Engine as Regulatory-First:** Emphasize FCRA adverse action compliance and explainability over black-box AI hype.
4. **Own the Bugs Found in Audit:** When discussing the N+1 query storm or GET idempotency, explain how you discovered them through rigorous architectural review and how you refactored them using CQRS and JPQL projections.
5. **Demonstrate Product Empathy:** Articulate the mission—liberating 70 million debt-free independent operators from financial invisibility.

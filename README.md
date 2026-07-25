# GigScore


A full-stack performance tracking platform for gig workers and freelancers. GigScore aggregates work data from multiple platforms, calculates a unified performance score, tracks growth trends, and provides AI-powered improvement suggestions.

---

# Overview

A full-stack application for gig workers and freelancers to track performance across platforms, monitor score trends, and get AI-assisted guidance.

## Features

- **JWT Authentication** -- Register and login with stateless JWT tokens
- **Unified Dashboard** -- Earnings, jobs completed, average rating, and active days in one view
- **Multi-Platform Tracking** -- Log gigs from any platform (Uber, DoorDash, Fiverr, etc.)
- **Score Computation** -- Weighted exponential formula producing a 0-100 performance score
- **Activity Feed** -- Recent gig activity with timestamped entries
- **AI Chat Assistant** -- Gemini-powered guidance on improving your score
- **Interactive API Docs** -- Swagger UI with in-browser JWT authorization
- **Light/Dark Theme** -- Persistent preference on the frontend

Gig workers often work across multiple platforms such as delivery apps and freelancing websites. Their earnings, ratings, and performance metrics are distributed across different platforms.

GigScore solves this problem by providing:

* A centralized performance dashboard
* A unified **GigScore (0-100)** metric
* Historical performance tracking
* AI-powered recommendations using Google Gemini
* Multi-platform gig activity management

| Layer | Technology |
|-------|-----------|
| Frontend | React 18, Vite, Tailwind CSS, Recharts, Axios, React Router |
| Backend | Spring Boot 4, Java 21 |
| Security | Spring Security, JWT (jjwt 0.11.5) |
| Database | MySQL 8, Spring Data JPA, Hibernate |
| AI | Google Gemini API |
| Docs | Springdoc OpenAPI (Swagger UI) |
| Build | Maven, npm |

## Architecture

```
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

Supported platforms:

* Swiggy
* Zomato
* Uber
* Rapido
* Upwork

---

# Features

## Authentication

* User registration and login
* JWT-based authentication
* BCrypt password encryption
* Persistent login session

## Dashboard

Displays:

* Total earnings
* Jobs completed
* Average rating
* Active days
* Recent gig activity

## Gig Management

Users can:

* Add gig activity from different platforms
* Track earnings and ratings
* Maintain platform-wise performance data

## GigScore Calculation

A score from **0-100** calculated using:

* Earnings
* Jobs completed
* Average rating
* Active consistency

## Score Analytics

Includes:

* Current GigScore
* Score breakdown
* Growth trajectory chart
* Performance suggestions

## AI Assistant

Integrated Google Gemini assistant provides:

* Score improvement advice
* Performance analysis
* Personalized recommendations

## Theme Support

* Light mode
* Dark mode
* Persistent user preference

---

# System Architecture

```
                         User
                          |
                          |
                          v

              +----------------------+
              |   React Frontend     |
              |----------------------|
              | Pages                |
              | Components           |
              | Axios Services       |
              | JWT Handling         |
              +----------+-----------+
                         |
                         |
                    REST API
                         |
                         v

              +----------------------+
              |  Spring Boot Backend |
              |----------------------|
              | Controllers          |
              | Services             |
              | JWT Security         |
              | JPA Repositories     |
              +----------+-----------+
                         |
                         |
                         v

              +----------------------+
              |       MySQL          |
              |----------------------|
              | Users                |
              | Gig Data             |
              | Scores               |
              | Activities           |
              +----------------------+

                         |
                         |
                         v

              +----------------------+
              | Google Gemini API    |
              | AI Recommendations   |
              +----------------------+
```

---

```
GigScore/
├── backend/
│   └── gigscore/
│       ├── src/
│       │   └── main/
│       │       ├── java/
│       │       │   └── com/
│       │       │       └── org/
│       │       │           └── gigscore/
│       │       │               ├── config/         # Security, CORS, JWT, OpenAPI configuration
│       │       │               ├── controller/     # REST API controllers
│       │       │               ├── dto/            # Request and response DTOs
│       │       │               ├── entity/         # JPA entities
│       │       │               ├── exception/      # Custom exceptions and global handler
│       │       │               ├── repository/     # Spring Data JPA repositories
│       │       │               ├── security/       # Authentication and CurrentUserResolver
│       │       │               └── service/        # Business logic
│       │       └── resources/
│       │           └── application.properties
│       └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── components/          # Reusable React components
│   │   ├── pages/               # Application pages
│   │   ├── context/             # Authentication and theme context
│   │   └── utils/               # API client and helper functions
│   └── package.json
│
└── README.md
```

---

## Complete User Flow

```text
                    +-----------+
                    | Register  |
                    +-----------+
                          |
                          v
                    +-----------+
                    |  Login    |
                    +-----------+
                          |
                          v
                    +-------------+
                    | Dashboard   |
                    +-------------+
                     /           \
                    /             \
                   v               v
          +-------------+   +--------------+
          |  Add Gig    |   | View Score   |
          +-------------+   +--------------+
                 |                 |
                 v                 v
         +---------------+   +-----------------+
         | Update Data   |   | Calculate Score |
         +---------------+   +-----------------+
                 |
                 v
         +----------------+
         | GigData Table  |
         +----------------+
            /         \
           /           \
          v             v
+----------------+  +-----------------+
| Activity History|  | GigScore Update |
+----------------+  +-----------------+
```

---

# Tech Stack

## Frontend

| Technology   | Purpose                  |
| ------------ | ------------------------ |
| React 18     | Component-based UI       |
| Vite         | Fast frontend build tool |
| Tailwind CSS | Styling and dark mode    |
| React Router | Client-side routing      |
| Axios        | API communication        |
| Recharts     | Score visualization      |

## Backend

| Technology      | Purpose                     |
| --------------- | --------------------------- |
| Java 21         | Backend language            |
| Spring Boot 4   | REST API framework          |
| Spring Security | Authentication and security |
| JWT             | Stateless authentication    |
| Spring Data JPA | Database interaction        |
| MySQL 8         | Relational database         |
| Maven           | Dependency management       |

## External Services

| Service           | Usage                     |
| ----------------- | ------------------------- |
| Google Gemini API | AI-powered chat assistant |

---

# Project Structure

```text
GigScore/
├── frontend/
│   ├── src/
│   │   ├── pages/
│   │   │   ├── Login.jsx
│   │   │   ├── Dashboard.jsx
│   │   │   ├── AddGig.jsx
│   │   │   └── Score.jsx
│   │   │
│   │   ├── components/
│   │   │   ├── Navbar.jsx
│   │   │   ├── Sidebar.jsx
│   │   │   ├── StatCard.jsx
│   │   │   ├── PlatformCard.jsx
│   │   │   └── AiChatWidget.jsx
│   │   │
│   │   └── services/
│   │       ├── httpClient.js
│   │       ├── userService.js
│   │       ├── gigService.js
│   │       └── chatService.js
│   │
│   └── package.json
│
├── backend/
│   └── gigscore/
│       ├── Controller/
│       ├── Service/
│       ├── Repository/
│       ├── Entity/
│       ├── DTO/
│       └── Config/
│
└── README.md
```
---

# Backend Architecture

GigScore follows layered architecture:

```text
+----------------------+
|   Controller Layer   |
+----------------------+
           |
           v
+----------------------+
|    Service Layer     |
|   (Business Logic)   |
+----------------------+
           |
           v
+----------------------+
|   Repository Layer   |
|   (Database Access)  |
+----------------------+
           |
           v
+----------------------+
|     Entity Layer     |
|  (Database Models)   |
+----------------------+
           |
           v
+----------------------+
|    MySQL Database    |
+----------------------+
```

Responsibilities:

## Controller

* Receives HTTP requests
* Validates input
* Returns responses

## Service

Contains:

* Score calculation
* Gig updates
* Authentication logic
* Business rules

## Repository

Handles:

* Database queries
* Entity persistence

---

# Database Design

Database:

```
gigscore
```

Tables:

```
User
 |
 |
 +------ GigData
 |
 |
 +------ GigScore
 |
 |
 +------ Activity
```

## User Table

Stores:

* User information
* Login credentials

## GigData Table

Stores platform-level statistics:

```text
User
│
├── Swiggy
├── Uber
└── Upwork
```

Fields:

* Platform
* Total earnings
* Jobs completed
* Average rating
* Active days

## GigScore Table

Stores:

* Calculated score
* User score history

## Activity Table

Stores:

* Gig events
* Platform
* Amount
* Rating
* Timestamp

---

# GigScore Algorithm

The score is calculated using four factors:

```text
Score =
[
(Earnings Component × 35)
+
(Jobs Component × 25)
+
(Rating Component × 30)
+
(Activity Component × 10)
]
× 100
```

Formula:

```text
Score =
(
(E/5000)^0.70 × 35
+
(J/100)^0.65 × 25
+
(R/5)^1.40 × 30
+
(D/30)^1.00 × 10
)
× 100
```

Where:

```text
E = Total Earnings
J = Jobs Completed
R = Average Rating
D = Active Days
```

## Why Exponents?

### Earnings and Jobs

Exponent < 1:

* Rewards early progress
* Helps new workers improve score quickly

### Rating

Exponent > 1:

* Makes quality more important
* Penalizes poor ratings

---

# API Endpoints

## Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users` | Register user |
| POST | `/api/users/login` | Login user |

---

## Dashboard

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/{userId}` | Get dashboard data |
| POST | `/api/gigs` | Add gig activity |

---

## Score

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/score/{userId}` | Get GigScore |

---

## Activity

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/activity/{userId}` | Recent activities |

---

## AI Assistant

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/chat/ask` | Gemini chat |

---

# Authentication Flow

```text
+------------------------------+
|          User Login          |
+------------------------------+
               |
               v
+------------------------------+
| Backend validates credentials|
+------------------------------+
               |
               v
+------------------------------+
|     JWT Token Generated      |
+------------------------------+
               |
               v
+------------------------------+
| Frontend stores token        |
+------------------------------+
               |
               v
+------------------------------+
| Every API request            |
| Authorization: Bearer <JWT>  |
+------------------------------+
               |
               v
+------------------------------+
| JwtAuthFilter validates token|
+------------------------------+
               |
               v
+------------------------------+
|     Request processed        |
+------------------------------+
```

---

# Local Setup

## Requirements

Install:

* Java 21+
* Maven 3.8+
* Node.js 18+
* MySQL 8+

---

# Database Setup

Create database:

* Java 21+
* Maven 3.8+
* MySQL 8+
* Node.js 18+

## Environment Variables

Copy `backend/gigscore/.env.example` and fill in your values:

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_USERNAME` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | `your_password` |
| `JWT_SECRET` | Secret for JWT signing (min 32 chars) | `a-very-long-random-string-here` |
| `GEMINI_API_KEY` | Google Gemini API key | `AIza...` |
| `GEMINI_MODEL` | Gemini model identifier | `gemini-2.5-flash` |

## Setup

### 1. Create the database

```sql
CREATE DATABASE gigscore;
```

# Backend Setup

Navigate:

```bash
cd backend/gigscore
```

Run:

```bash
mvn spring-boot:run
```

Backend:

```
http://127.0.0.1:8080
```

---

# Frontend Setup

Navigate:

### 2. Run the backend

```bash
cd backend/gigscore
mvn spring-boot:run
```

Backend starts at `http://localhost:8080`.

### 3. Run the frontend

```bash
cd frontend
```

Install:

```bash
npm install
```

Run:

```bash
npm run dev
```

Frontend:

```
http://localhost:5173
```

---

# Environment Variables

Backend requires:

```
DB_USERNAME=
DB_PASSWORD=

JWT_SECRET=

GEMINI_API_KEY=
GEMINI_MODEL=
```

Never commit:

* API keys
* Passwords
* Database credentials
* JWT secrets

---

# Engineering Decisions

## Why React?

* Component reusability
* Large ecosystem
* Efficient UI updates

## Why Spring Boot?

* Production-ready framework
* Built-in security support
* Strong Java ecosystem

Frontend starts at `http://localhost:5173`.

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON spec:

```
http://localhost:8080/v3/api-docs
```

To test authenticated endpoints in Swagger UI, click **Authorize**, paste a JWT token (obtained from login/register), and execute requests.

## API Overview

### Authentication

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/users` | Register a new user | No |
| POST | `/api/users/login` | Login and receive a JWT | No |

### Dashboard & Data

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/users/{userId}` | Get user dashboard with score and gig summaries | Yes |
| POST | `/api/gigs` | Log a new gig event | Yes |
| GET | `/api/activity/{userId}` | Get 5 most recent activities | Yes |

### Score

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/score/{userId}` | Calculate and return gig score (0-100) | Yes |

### AI Chat

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/chat/ask` | Send messages, receive AI guidance | Yes |

## Authentication

All protected endpoints require a JWT Bearer token in the `Authorization` header:

```
Authorization: Bearer <token>
```

Tokens are returned by the register (`POST /api/users`) and login (`POST /api/users/login`) endpoints. Tokens expire after 1 hour.

## Score Formula

The gig score (0-100) combines four weighted components with exponential normalization:

| Component | Weight | Exponent | Target |
|-----------|--------|----------|--------|
| Earnings | 35% | 0.70 | $5,000 |
| Jobs Completed | 25% | 0.65 | 100 |
| Average Rating | 30% | 1.40 | 5.0 |
| Active Days | 10% | 1.00 | 30 |

- Exponents < 1 reward early progress; > 1 apply stricter scaling.
- Each component is normalized to 0-1, raised to its exponent, multiplied by its weight, then summed and scaled to 0-100.

## Build

```bash
# Backend
cd backend/gigscore
mvn clean package

# Frontend
cd frontend
npm run build
```

## Testing

```bash
cd backend/gigscore
mvn test
```

Tests use an H2 in-memory database with a `test` profile. The test configuration is in `src/test/resources/application-test.properties`.

## Known Limitations

- No password reset or email verification flow
- No rate limiting on API endpoints
- No WebSocket real-time updates (dependency present but unused)
- Score history is not persisted across recalculations (overwrites the previous score)
- AI chat has no conversation memory between requests
- CORS is configured for localhost only; production origins need updating in `CorsConfig.java`


## Why MySQL?

* Structured relational data
* ACID transactions
* Strong consistency for financial metrics

## Why JWT?

* Stateless authentication
* Works well with React + REST APIs

---

# Future Improvements

## Security

* Add role-based authorization
* Validate ownership of user resources
* Add rate limiting
* Add HTTPS enforcement

## Performance

* Redis caching for scores
* Background score calculation
* Database optimization

## Frontend

* Replace prop drilling with Context/Zustand
* Add better error boundaries
* Improve loading states

## Backend

* Add automated tests
* Add Flyway database migration
* Add Spring Actuator monitoring

---

# Production Scaling Approach

```
Current:

React
 |
Spring Boot
 |
MySQL


Future:

React CDN

     |
     v

Load Balancer

     |
     v

Multiple Backend Instances

     |
     +---- Redis Cache
     |
     +---- Message Queue
     |
     +---- Database Replicas
```

---

# License

Currently unlicensed.

Add a LICENSE file before open-source distribution.

This project is unlicensed. Add a `LICENSE` file if you plan to distribute it.

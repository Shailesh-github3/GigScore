# GigScore

A full-stack performance tracking platform for gig workers and freelancers. GigScore aggregates work data from multiple platforms, calculates a unified performance score, tracks growth trends, and provides AI-powered improvement suggestions.

---

# Overview

Gig workers often work across multiple platforms such as delivery apps and freelancing websites. Their earnings, ratings, and performance metrics are distributed across different platforms.

GigScore solves this problem by providing:

* A centralized performance dashboard
* A unified **GigScore (0-100)** metric
* Historical performance tracking
* AI-powered recommendations using Google Gemini
* Multi-platform gig activity management

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

# Complete User Flow

```
Register
   |
   v
Login
   |
   v
Dashboard
   |
   +----------------+
   |                |
   v                v
Add Gig         View Score
   |                |
   |                |
   v                v

Update Data     Calculate Score
   |
   |
   v

GigData Table
   |
   |
   +----> Activity History
   |
   +----> GigScore Update

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

```
GigScore/

├── frontend/
│
│   ├── src/
│   │
│   ├── pages/
│   │   ├── Login.jsx
│   │   ├── Dashboard.jsx
│   │   ├── AddGig.jsx
│   │   └── Score.jsx
│   │
│   ├── components/
│   │   ├── Navbar.jsx
│   │   ├── Sidebar.jsx
│   │   ├── StatCard.jsx
│   │   ├── PlatformCard.jsx
│   │   └── AiChatWidget.jsx
│   │
│   ├── services/
│   │   ├── httpClient.js
│   │   ├── userService.js
│   │   ├── gigService.js
│   │   └── chatService.js
│
│
├── backend/
│
│   └── gigscore/
│       │
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

```
Controller Layer

      |
      v

Service Layer
(Business Logic)

      |
      v

Repository Layer
(Database Access)

      |
      v

Entity Layer
(Database Models)

      |
      v

MySQL Database
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

```
User
 |
 +-- Swiggy
 |
 +-- Uber
 |
 +-- Upwork
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

```
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

```
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

```
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

| Method | Endpoint           | Description   |
| ------ | ------------------ | ------------- |
| POST   | `/api/users`       | Register user |
| POST   | `/api/users/login` | Login user    |

---

## Dashboard

| Method | Endpoint              | Description        |
| ------ | --------------------- | ------------------ |
| GET    | `/api/users/{userId}` | Get dashboard data |
| POST   | `/api/gigs`           | Add gig activity   |

---

## Score

| Method | Endpoint          | Description  |
| ------ | ----------------- | ------------ |
| GET    | `/score/{userId}` | Get GigScore |

---

## Activity

| Method | Endpoint                 | Description       |
| ------ | ------------------------ | ----------------- |
| GET    | `/api/activity/{userId}` | Recent activities |

---

## AI Assistant

| Method | Endpoint        | Description |
| ------ | --------------- | ----------- |
| POST   | `/api/chat/ask` | Gemini chat |

---

# Authentication Flow

```
User Login

    |
    v

Backend validates credentials

    |
    v

JWT Token Generated

    |
    v

Frontend stores token

    |
    v

Every API request:

Authorization:
Bearer <JWT>

    |
    v

JwtAuthFilter validates token

    |
    v

Request processed
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

```sql
CREATE DATABASE gigscore;
```

---

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

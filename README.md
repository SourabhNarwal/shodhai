# Shodh-a-Code

A lightweight, real-time coding contest prototype that showcases an end-to-end system: a React-based UI for participating in contests and a Spring Boot backend that validates submissions by compiling and running user code in an isolated Docker environment, persisting results to MongoDB, and surfacing a live leaderboard.

## 1) Overview & Objective
Shodh-a-Code simulates a live contest experience where participants:
- Join a contest using an ID and username
- Read problems, write code, and submit solutions
- Receive asynchronous verdicts (Pending → Running → Accepted/Wrong Answer)
- Watch a live-updating leaderboard

This project demonstrates full-stack engineering across API design, async processing, containerized code execution, and real-time UX.

## 2) Tech Stack
- Frontend: Vite + React + Tailwind CSS, React Router, Monaco Editor
- Backend: Spring Boot (Web, Data MongoDB, Lombok)
- Database: MongoDB
- Execution: Docker (openjdk:17) invoked from Java ProcessBuilder
- Orchestration: docker-compose

## 3) Setup & Run (docker-compose)
Prereqs: Docker + Docker Compose

```bash
# From repository root
docker compose up --build
```

Services:
- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- MongoDB: localhost:27017 (container name: mongo)

Environment (compose):
- `SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/shodh`

Seed Data: On backend start, a sample contest ("Intro Contest"), two problems, and a sample user are inserted if collections are empty.

Local Dev (optional):
- Backend: `cd backend && mvn spring-boot:run`
- Frontend: `cd frontend && npm install && npm run dev`

## 4) API Endpoints
Base URL: `http://localhost:8080`

### GET /api/contests/{contestId}
Returns contest details including a list of problems.

Response 200:
```json
{
  "id": "<contestId>",
  "name": "Intro Contest",
  "startTime": "2025-01-01T00:00:00Z",
  "endTime": "2025-01-01T02:00:00Z",
  "problems": [
    { "id": "<problemId>", "title": "Echo", "description": "..." }
  ]
}
```

### GET /api/contests/{contestId}/leaderboard
Returns aggregated leaderboard (best score per problem, summed per user), sorted desc.

Response 200:
```json
[
  { "userId": "<userId>", "username": "tester", "totalScore": 100 }
]
```

### POST /api/submissions
Accepts a new submission and returns a `submissionId`. The backend processes it asynchronously.

Request:
```json
{
  "userId": "<userId>",
  "problemId": "<problemId>",
  "code": "class Solution { public static void main(String[] a){ System.out.println(\"hello\"); }}",
  "language": "java"
}
```

Response 201:
```json
{ "submissionId": "<id>" }
```

### GET /api/submissions/{submissionId}
Returns latest status and result of a submission.

Response 200:
```json
{
  "id": "<id>",
  "status": "Pending | Running | Accepted | Wrong Answer | Error",
  "result": "OK | Mismatch | Compilation/Runtime Error | System Error",
  "score": 0
}
```

## 5) Design Decisions & Architecture
- Data Model (MongoDB `@Document`): `User`, `Contest`, `Problem`, `Submission` (+ `LeaderboardEntry` DTO model)
- Repositories: Spring Data `MongoRepository` for CRUD and simple queries
- Services:
  - `ContestService`: CRUD, list problems, compute leaderboard by best-per-problem sum
  - `SubmissionService`: CRUD, status updates, submission queries
  - `UserService`: CRUD, get-or-create by username, score accumulation
  - `CodeExecutionService`: Orchestrates Docker execution of user code, compares output, updates submission status and user score
- Execution Flow:
  1) Frontend POSTs a submission → backend persists with Pending
  2) Backend asynchronously compiles/runs code in a container, updates status to Running then Accepted/Wrong Answer/Error
  3) Frontend polls status every few seconds and refreshes the leaderboard periodically

Architecture Diagram (high-level):

```mermaid
flowchart LR
  subgraph Frontend (React)
    UI[Join/Contest UI]\nMonaco Editor --> APIClient
  end

  APIClient -- REST --> B[Spring Boot API]
  B -- MongoRepository --> DB[(MongoDB)]
  B -- ProcessBuilder/Docker --> D[(openjdk:17 container)]
  D -- stdout --> B

  subgraph Backend
    B
  end
```

Security & Isolation:
- Submissions are executed inside a Docker container with a dedicated working directory and time limit. Further sandboxing (seccomp/apparmor/cgroups) and resource controls can be added for production.

Scoring:
- Prototype uses a simple scheme (e.g., Accepted = 100). Extendable to multiple test cases, partial scoring, and penalties.

## 6) Future Improvements & Challenges
- Multi-language support (Python, C++, JS) with per-language images and commands
- Test suite expansion: multiple inputs/outputs per problem with aggregated scoring
- Real-time updates via WebSockets/SSE instead of polling
- AuthN/AuthZ (login, roles) and per-contest user isolation
- Enhanced sandboxing: memory/CPU limits, network isolation, file whitelist
- Observability: structured logs, metrics, tracing, per-submission diagnostics
- CI/CD and automated integration tests (mock Docker runtime)
- Admin tooling: problem authoring UI, contest management, bulk imports

---

References: See `requirements.md` for the original brief, evaluation criteria, and deliverables.

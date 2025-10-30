# 🧠 Shodh-a-Code  
*A lightweight, real-time coding contest platform demonstrating full-stack system design.*

---

## 🎯 Overview & Objective

**Shodh-a-Code** simulates a live coding contest experience where participants can:

- 🧑‍💻 Join a contest using an ID and username  
- ✍️ Read problems, write and submit code  
- ⚙️ Receive asynchronous verdicts (`Pending → Running → Accepted/Wrong Answer`)  
- 🏆 Watch a live-updating leaderboard  

This project demonstrates end-to-end engineering across API design, async processing, containerized code execution, and real-time UX.

---

## 🧩 Tech Stack

| Layer | Technologies |
|-------|---------------|
| **Frontend** | React (Vite) · Tailwind CSS · React Router · Monaco Editor |
| **Backend** | Spring Boot (Web, Data MongoDB, Lombok) |
| **Database** | MongoDB |
| **Code Execution** | Docker (`openjdk:17`) invoked via Java `ProcessBuilder` |
| **Orchestration** | Docker Compose |

---

## ⚙️ Setup & Run (with Docker Compose)

### 🧱 Prerequisites
- Docker + Docker Compose installed

### ▶️ Steps
```bash
# From repository root
docker compose up --build
Services

Service	URL	Container
Frontend	http://localhost:5173
	shodh-frontend
Backend	http://localhost:8080
	shodh-backend
MongoDB	localhost:27017	shodh-mongo

Environment
SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/shodh
Seed Data
On backend startup:

A sample contest (“Intro Contest”)

Two sample problems

One sample user
are inserted automatically if the collections are empty.

🧑‍💻 Local Development (Optional)
# Backend
cd backend
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm run dev

🔗 API Endpoints

Base URL: http://localhost:8080

GET /api/contests/{contestId}

Fetch contest details with problems.

{
  "id": "<contestId>",
  "name": "Intro Contest",
  "startTime": "2025-01-01T00:00:00Z",
  "endTime": "2025-01-01T02:00:00Z",
  "problems": [
    { "id": "<problemId>", "title": "Echo", "description": "..." }
  ]
}

GET /api/contests/{contestId}/leaderboard

Returns leaderboard, sorted by total score.

[
  { "userId": "<userId>", "username": "tester", "totalScore": 100 }
]

POST /api/submissions

Submit code for a problem.

{
  "userId": "<userId>",
  "problemId": "<problemId>",
  "code": "class Solution { public static void main(String[] a){ System.out.println(\"hello\"); }}",
  "language": "java"
}


Response

{ "submissionId": "<id>" }

GET /api/submissions/{submissionId}

Check submission status.

{
  "id": "<id>",
  "status": "Pending | Running | Accepted | Wrong Answer | Error",
  "result": "OK | Mismatch | Compilation/Runtime Error | System Error",
  "score": 0
}

🧱 Design Decisions & Architecture
🧩 Data Model (MongoDB)

User, Contest, Problem, Submission

LeaderboardEntry (DTO for aggregated scores)

🧠 Core Services
Service	Responsibility
ContestService	CRUD, list problems, compute leaderboard
SubmissionService	CRUD, track status, submission queries
UserService	CRUD, get-or-create user, update scores
CodeExecutionService	Execute code in Docker, compare output, update status & score
🔄 Execution Flow

Frontend posts a submission → backend marks it Pending

Backend asynchronously compiles & executes inside a Docker container

Updates status → Running → Accepted/Wrong Answer/Error

Frontend polls submission & leaderboard endpoints for updates

🧭 System Diagram
flowchart LR
  subgraph Frontend [Frontend (React)]
    UI[Join/Contest UI & Monaco Editor] --> APIClient
  end

  APIClient -- REST --> Backend[Spring Boot API]
  Backend -- MongoRepository --> DB[(MongoDB)]
  Backend -- ProcessBuilder/Docker --> DockerExec[(openjdk:17 Container)]
  DockerExec -- stdout --> Backend

🔒 Security & Isolation

Each submission runs inside an isolated Docker container

Temporary directories per run

Future scope: add cgroup/namespace-based sandboxing and resource limits

🧮 Scoring

Accepted = 100 points

Extendable to multi-testcase, partial scoring, and penalty schemes

🚧 Future Improvements
Area	Improvement
Multi-language support	Add Python, C++, JS execution images
Test system	Add multi-input test cases & aggregated scoring
Real-time updates	Replace polling with WebSockets/SSE
Security	Sandboxing with CPU/memory limits
Observability	Logs, metrics, and per-submission diagnostics
CI/CD	Automated testing & container build pipelines
Admin Tools	Contest management dashboard
📚 References

See requirements.md for the original brief, evaluation criteria, and deliverables.

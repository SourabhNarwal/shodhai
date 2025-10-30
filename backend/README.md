🧠 Shodh-a-Code — Real-Time Coding Contest Platform

A full-stack web application that enables students to join live coding contests, solve programming problems, and view real-time leaderboards.
The system features a Spring Boot backend with a live Docker-based code judge, a React frontend, and a MongoDB database — all orchestrated via Docker Compose.

🚀 Tech Stack
Layer	Technology
Frontend	React (Vite) + Tailwind CSS
Backend	Spring Boot (Java 17) + Maven
Database	MongoDB
Containerization	Docker + Docker Compose
⚙️ Setup Instructions
Prerequisites

Docker & Docker Compose installed

Git installed

Steps to Run
# 1️⃣ Clone the repository
git clone https://github.com/<your-username>/shodh-a-code.git
cd shodh-a-code

# 2️⃣ Build and start all services
docker-compose up -d --build

# 3️⃣ Access the app
Frontend: http://localhost:5173  
Backend API: http://localhost:8080


To stop and clean up:

docker-compose down -v

🧩 Application Overview
1. Join Contest

Users enter a Contest ID and Username to join.
POST /api/users/join

2. Solve Problems

Participants view coding problems and submit their solutions.

3. Real-Time Evaluation

Submitted code runs inside a Docker container (via Spring Boot’s ProcessBuilder) for safe, isolated execution.

4. Live Leaderboard

Scores update dynamically using periodic polling from the frontend.

🧱 API Design Overview
Endpoint	Method	Description
/api/contests/{contestId}	GET	Fetch contest details
/api/users/join	POST	Join a contest using username and contestId
/api/submissions	POST	Submit code for a problem
/api/submissions/{submissionId}	GET	Fetch latest submission result/status
/api/contests/{contestId}/leaderboard	GET	Retrieve current contest leaderboard
Example — POST /api/submissions
{
  "username": "John",
  "problemId": "prob1",
  "language": "java",
  "code": "public class Main { public static void main(String[] args){ System.out.println(2+2); }}"
}


Response

{
  "submissionId": "65b4f8d1c9a712345",
  "status": "Pending"
}

🧠 Key Design Choices & Justifications
🧩 Backend Architecture

Modular Spring Boot structure with separate layers for controllers, services, and repositories.

The CodeJudgingService uses ProcessBuilder to execute user code inside a Docker container with timeouts and I/O stream capture.

Asynchronous processing ensures non-blocking user experience.

⚡ Frontend Architecture

Built with React (Vite) for fast builds and Tailwind CSS for clean UI.

Uses state hooks to manage submission state and live leaderboard updates.

Polls APIs every few seconds to simulate real-time updates.

🐳 Docker & DevOps

Each service (frontend, backend, MongoDB) runs in isolated containers.

Docker Compose orchestrates startup order and networking.

Backend connects to MongoDB using service name mongo for seamless inter-container communication.

🧩 Pragmatic Shortcuts & Future Improvements

Shortcut: Used periodic polling instead of WebSockets for real-time updates (simpler but less scalable).
Future Improvement: Replace with WebSocket or Server-Sent Events (SSE) for true real-time updates.

Shortcut: Synchronous Docker code execution in the judging service.
Future Improvement: Offload code runs to a queue-based async worker system (e.g., RabbitMQ or Kafka).

Shortcut: Basic error handling and limited language support.
Future Improvement: Add multi-language execution (Python, C++, JS) and stricter sandboxing.

🧪 Sample Test Data

The database is pre-seeded with:

One sample contest

Three coding problems

Sample users and submissions for demo

🧰 Troubleshooting

If you see Mongo connection errors, ensure:

docker ps  # MongoDB is running


Then restart services:

docker-compose down -v
docker-compose up -d --build


# Secure Task Management System

A production-grade REST API for team task management with JWT authentication,
role-based access control, and comprehensive audit logging.

Built with Java 24, Spring Boot 3.5, MySQL, and Spring Security.

---

## What This Project Does

This is not a basic to-do list. It is a multi-user task management system where:

- Users register and log in securely with BCrypt-hashed passwords
- Tasks can be created, assigned to other users, and tracked through statuses
- Admins have elevated access to manage all tasks and users
- Every action is recorded in an audit trail with timestamps
- JWT access tokens expire in 1 hour — refresh tokens handle silent renewal
- All list endpoints are paginated — no full table scans

---

### Environment Variables

The following environment variables can be set for production:

| Variable | Description | Default |
|---|---|---|
| JWT_SECRET | Secret key for signing JWT tokens | hardcoded default (change in prod) |
| JWT_EXPIRATION | Access token expiry in ms | 3600000 (1 hour) |
| JWT_REFRESH_EXPIRATION | Refresh token expiry in ms | 604800000 (7 days) |
| DB_PASSWORD | MySQL database password | configured in properties |

For local development the defaults in application.properties are used automatically.
For production deployment set these as system environment variables or use a secrets manager.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 24 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Database | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Password Hashing | BCrypt |
| Validation | Jakarta Validation |
| Build Tool | Maven |

---

## Architecture
controller/   → REST endpoints, request/response handling
service/      → Business logic, JWT, audit logging
repository/   → Database queries via Spring Data JPA
model/        → JPA entities (User, Task, AuditLog, RefreshToken)
dto/          → Request and response objects
filter/       → JWT authentication filter
config/       → Spring Security configuration
exception/    → Global exception handling 

---

## Security Features

- **JWT Authentication** — stateless, no server-side sessions
- **Refresh Token Rotation** — new refresh token issued on every use,
  old one invalidated. Detects token theft.
- **BCrypt Password Hashing** — passwords never stored in plain text
- **Role-Based Access Control** — ADMIN and USER roles with
  method-level security via @PreAuthorize
- **Input Validation** — strong password rules, field length limits,
  deadline cannot be in the past
- **Audit Logging** — every create, update, and delete recorded
  with user email and timestamp
- **Global Exception Handling** — consistent JSON error responses
  across all endpoints

---

## API Endpoints

### Authentication
POST /api/auth/register     Register new user
POST /api/auth/login        Login — returns access + refresh token
POST /api/auth/refresh      Get new access token using refresh token
POST /api/auth/logout       Invalidate refresh token

### Tasks (authenticated)

POST   /api/tasks                Create task
GET    /api/tasks/my             My tasks (paginated)
GET    /api/tasks/my/overdue     My overdue tasks
GET    /api/tasks/my/upcoming    My upcoming tasks
GET    /api/tasks/my/stats       My task statistics
GET    /api/tasks/my/search      Search my tasks
PATCH  /api/tasks/{id}/status    Update task status
PUT    /api/tasks/{id}           Update task details
GET    /api/tasks/{id}/activity  Full audit history of a task

### Admin only

GET    /api/tasks              All tasks (paginated, filterable)
GET    /api/tasks/overdue      All overdue tasks
GET    /api/tasks/upcoming     All upcoming tasks
GET    /api/tasks/search       Search all tasks
GET    /api/tasks/stats        System-wide task statistics
DELETE /api/tasks/{id}         Delete task
GET    /api/admin/users        All users
DELETE /api/admin/users/{id}   Delete user
PATCH  /api/admin/users/{id}/role   Change user role
GET    /api/admin/audit-logs   Full audit trail

### Query Parameters (list endpoints)
page        Page number (default: 0)
size        Results per page (default: 10)
sortBy      Field to sort by (default: createdAt)
direction   asc or desc (default: desc)
status      Filter by PENDING / IN_PROGRESS / COMPLETED
keyword     Search term for title and description
days        Upcoming tasks within N days (1-30)

---

## Database Schema
users
id, name, email, password (BCrypt), role, created_at
tasks
id, title, description, status, priority, deadline,
created_by (FK), assigned_to (FK), created_at
audit_logs
id, action, entity_type, entity_id,
performed_by, details, performed_at
refresh_tokens
id, token, user_id (FK), expiry_date, revoked

---

## How to Run Locally

### Prerequisites
- Java 24
- MySQL 8
- Maven (or use included mvnw wrapper)

### Setup

1. Clone the repository
```bash
git clone https://github.com/YOUR_USERNAME/secure-task-manager.git
cd secure-task-manager
```

2. Create MySQL database
```sql
CREATE DATABASE taskmanager_db;
```

3. Configure `src/main/resources/application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/taskmanager_db
spring.datasource.username=root
spring.datasource.password=your_password
jwt.secret=your-secret-key-minimum-32-characters
jwt.expiration=3600000
jwt.refresh-expiration=604800000
```

4. Run the application
```bash
.\mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

---

## Sample API Usage

### Register
```json
POST /api/auth/register
{
  "name": "Priya Sharma",
  "email": "priya@example.com",
  "password": "Priya123!"
}
```

### Login
```json
POST /api/auth/login
{
  "email": "priya@example.com",
  "password": "Priya123!"
}

Response:
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "uuid-string",
  "name": "Priya Sharma",
  "email": "priya@example.com",
  "role": "USER",
  "tokenType": "Bearer"
}
```

### Create Task
```json
POST /api/tasks
Authorization: Bearer {accessToken}

{
  "title": "Review pull request",
  "description": "Review and merge the authentication PR",
  "priority": "HIGH",
  "deadline": "2026-05-01",
  "assignedToId": 2
}
```

---

## Key Design Decisions

**Why JWT over sessions?**
Stateless authentication scales horizontally — any server instance
can verify a token without shared session storage.

**Why BCrypt over MD5/SHA?**
BCrypt is deliberately slow — 300ms per hash makes brute force attacks
computationally infeasible. Each hash includes a unique salt,
defeating rainbow table attacks.

**Why store refresh tokens in the database?**
Pure JWT cannot be invalidated before expiry. Storing refresh tokens
allows immediate invalidation on logout or suspicious activity.

**Why DTOs instead of exposing entities?**
Entities contain internal fields like hashed passwords and foreign keys.
DTOs give precise control over what the API exposes.

---

## What I Learned

- Implementing stateless JWT authentication from scratch
- Designing role-based access control with Spring Security
- Token rotation as a security pattern for detecting theft
- Building pagination and sorting into all list endpoints
- Writing unit tests with Mockito and integration tests with MockMvc
- Global exception handling for consistent API responses
- Audit logging as a real enterprise requirement

---

## Author

**Shravani Kulkarni** — Computer Science Student
GitHub: [Sh0305](https://github.com/Sh0305)

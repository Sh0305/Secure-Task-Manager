# Secure Task Management System

This project was developed to learn backend development using Spring Boot and MySQL. It is a task management application that allows users to create, assign, update, and track tasks while implementing authentication and role-based access control.

The project helped me understand how REST APIs, database operations, authentication mechanisms, and backend application architecture work together in a real-world application.

---

## Features

- User registration and login
- JWT-based authentication
- Role-based access control
- Create, update, assign, and delete tasks
- Track task status and deadlines
- REST API-based backend
- Input validation for user and task data
- Audit logging for important actions
- MySQL database integration

---

## Technologies Used

- Java 24
- Spring Boot
- Spring Security
- JWT Authentication
- MySQL
- Spring Data JPA
- Hibernate
- Maven
- Jakarta Validation
- REST APIs

---

## Project Structure

```text
controller/    -> API endpoints
service/       -> Business logic
repository/    -> Database operations
model/         -> Entity classes
dto/           -> Request and response objects
config/        -> Security configuration
filter/        -> JWT authentication filter
exception/     -> Global exception handling
```

---

## How It Works

1. Users register and log in to the application.
2. Authentication is handled using JWT tokens.
3. Based on their role, users can access different functionalities.
4. Tasks can be created, assigned, updated, and tracked.
5. Application data is stored in a MySQL database.
6. Important actions are recorded for monitoring and auditing purposes.

---

## Installation

Clone the repository:

```bash
git clone https://github.com/Sh0305/Secure-Task-Manager.git
cd Secure-Task-Manager
```

Configure MySQL database settings in:

```properties
src/main/resources/application.properties
```

Install dependencies and run the project:

```bash
./mvnw spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

---

## Main API Functionalities

### Authentication

- Register User
- Login User
- Refresh Token
- Logout User

### Task Management

- Create Task
- Update Task
- Delete Task
- Assign Task
- Track Task Status
- View Task Statistics
- Search Tasks

### Admin Operations

- Manage Users
- View System Data
- Access Audit Logs

---

## What I Learned

Through this project, I gained practical experience with:

- Spring Boot application development
- REST API design and implementation
- Spring Security fundamentals
- JWT-based authentication
- Role-based access control
- Database integration using MySQL
- CRUD operations with Spring Data JPA
- Input validation and exception handling
- Layered backend architecture

---

## My Contribution

I worked on developing and understanding different parts of the application, including task management functionality, API development, authentication workflows, and database integration.

This project helped me understand how backend systems are structured and how different layers of a Spring Boot application interact with each other.

---

## Future Improvements

- Add email notifications for task updates
- Add file attachments to tasks
- Improve dashboard and reporting features
- Add API documentation using Swagger
- Enhance task analytics and visualization

---

## Disclaimer

This project was developed for learning purposes to improve my understanding of backend development concepts using Spring Boot, MySQL, and REST APIs.

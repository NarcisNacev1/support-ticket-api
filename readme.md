# Customer Support Ticketing API

A robust Spring Boot-based API for managing customer support tickets with agent assignments, priority escalation, comments, and feedback. Built with security and validation best practices.

## Features

- **Ticket Management**
  - Create tickets with title, description, category, and priority
  - Update ticket details
  - Delete tickets
  - Get ticket by ID or list all tickets
  - Automatic category defaulting ("unknown")

- **Workflow Features**
  - Assign/reassign tickets to agents
  - Escalate ticket priority (LOW → MEDIUM → HIGH → URGENT)
  - Update ticket status (OPEN → IN_PROGRESS → RESOLVED → CLOSED)
  - SLA tracking with automatic timestamps

- **Collaboration**
  - Add comments to tickets
  - Submit feedback on closed tickets

- **Security**
  - API Key authentication
  - Secure header-based key validation
  - CSRF protection disabled for API endpoints

- **Validation**
  - Request body validation
  - Custom error messages
  - Type safety checks

## Technologies

- **Core**
  - Java 17
  - Spring Boot 3.4.5
  - Spring Data JPA
  - Hibernate Validator

- **Database**
  - PostgreSQL
  - Hibernate ORM
  - Liquibase (recommended for future migrations)

- **Security**
  - Spring Security
  - Custom API Key Filter

- **Documentation**
  - OpenAPI 3.0 (recommended addition)

## API Endpoints

| Method | Endpoint                  | Description                          |
|--------|---------------------------|--------------------------------------|
| POST   | `/tickets`                | Create new ticket                    |
| GET    | `/tickets/{id}`           | Get ticket by ID                     |
| GET    | `/tickets`                | List all tickets                     |
| PATCH  | `/tickets/{id}`           | Update ticket details                |
| PATCH  | `/tickets/{id}/assign`    | Assign ticket to agent               |
| PATCH  | `/tickets/{id}/escalate`  | Escalate ticket priority             |
| PATCH  | `/tickets/{id}/status`    | Update ticket status                 |
| POST   | `/tickets/{id}/comments`  | Add comment to ticket                |
| POST   | `/tickets/{id}/feedback`  | Submit feedback on closed ticket     |
| DELETE | `/tickets/{id}`           | Delete ticket                        |

## Setup Instructions

1. **Prerequisites**
   - Java 17 JDK
   - PostgreSQL 14+
   - Maven

2. **Database Setup**
   ```sql
   CREATE DATABASE support_tickets;
   CREATE USER support_user WITH PASSWORD 'securepassword';
   GRANT ALL PRIVILEGES ON DATABASE support_tickets TO support_user;
   ```

3. **Configuration**
   Create `application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/support_tickets
   spring.datasource.username=support_user
   spring.datasource.password=securepassword
   spring.jpa.hibernate.ddl-auto=update
   api.security.key=your-secure-key-123
   ```

4. **Build & Run**
   ```bash
   mvn clean install
   java -jar target/support-ticket-api-0.0.1-SNAPSHOT.jar
   ```

## Example Requests

**Create Ticket**
```http
POST /tickets
Content-Type: application/json
X-API-KEY: your-secure-key-123

{
  "title": "Server Downtime",
  "description": "Production server unresponsive",
  "priority": "HIGH",
  "category": "Infrastructure"
}
```

**Add Comment**
```http
POST /tickets/5/comments
Content-Type: application/json
X-API-KEY: your-secure-key-123

{
  "content": "Investigating network issues",
  "author": "admin_john"
}
```

**Escalate Priority**
```http
PATCH /tickets/5/escalate
X-API-KEY: your-secure-key-123
```

## Error Handling

**Sample Error Response**
```json
{
  "message": "Validation failed",
  "details": [
    "title: must not be blank",
    "priority: must not be null"
  ]
}
```

**Common Status Codes**
- 400 Bad Request - Invalid input
- 401 Unauthorized - Missing/invalid API key
- 404 Not Found - Resource not found
- 500 Internal Server Error - Database issues

## Security

- All endpoints require valid API key in `X-API-KEY` header
- Configure secret key in `application.properties`:
  ```properties
  api.security.key=your-production-key
  ```
- Recommended to rotate keys regularly

## Future Improvements

1. Add Swagger/OpenAPI documentation
2. Implement role-based access control
3. Add email notifications
4. Implement ticket categories as enum
5. Add pagination for ticket listings
6. Implement rate limiting

## Acknowledgments

This project was developed with assistance from AI to resolve technical challenges including:
- JPA entity relationships
- Spring Security configuration
- Validation error handling
- API response standardization


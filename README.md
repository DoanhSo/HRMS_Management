# HR Management System (HRMS)

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)

## Overview
A comprehensive, enterprise-grade Human Resources Management System built with Spring Boot 4.1.0 and Java 21. Designed with a package-by-feature architecture, it ensures scalability and maintainability while handling core HR operations including employee management, attendance tracking, leave requests, and payroll processing.

## Technology Stack

| Category | Technology |
|---|---|
| **Core Framework** | Java 21, Spring Boot 4.1.0 |
| **Data Access** | Spring Data JPA, Hibernate |
| **Database** | MySQL (V1), Redis (V2) |
| **Database Migration** | Flyway |
| **Security** | Spring Security, JWT |
| **Mapping & Boilerplate** | MapStruct, Lombok |
| **API Documentation** | OpenAPI 3 / Swagger UI |
| **Testing** | JUnit 5, Mockito |
| **Deployment** | Docker, Docker Compose |

## Version Plan

*   **Version 1 (Current):** Core HR features (Auth, Employee, Department, Position, Attendance, Leave, Payroll). MySQL-only architecture.
*   **Version 2 (Planned):** Performance and scalability improvements. Introduces Redis for caching and refresh token storage.
*   **Version 3 (Planned):** Advanced features including Email notifications, Scheduled tasks, Excel/PDF export, comprehensive Audit Logging, and File Uploads.

## Module Overview

*   **`auth`:** Authentication, authorization, JWT management, and RBAC (Role-Based Access Control).
*   **`employee`:** Employee lifecycle management, profiles, and basic information.
*   **`department`:** Department structures and hierarchies.
*   **`position`:** Job titles, levels, and position management.
*   **`attendance`:** Daily attendance tracking and configuration-driven rules.
*   **`leave`:** Leave requests, approval workflows, and balance tracking.
*   **`payroll`:** Strategy/Rule engine-driven salary calculation and payroll periods.
*   **`dashboard`:** Analytics and key metrics for HR admins.
*   **`common`:** Shared utilities, security configurations, global exception handling, and auditing.

## Project Structure

```text
com.ng_doanh.hr_management_system
├── auth/           # Auth controllers, services, security configs
├── employee/       # Employee management
├── department/     # Department management
├── position/       # Position management
├── attendance/     # Attendance tracking
├── leave/          # Leave management
├── payroll/        # Payroll processing
├── dashboard/      # Dashboard & Analytics
└── common/         # Cross-cutting concerns
    ├── config/
    ├── security/
    ├── exception/
    ├── audit/
    ├── validation/
    └── utils/
```

## Prerequisites

*   Java 21 Development Kit (JDK)
*   Maven 3.8+
*   MySQL 8.0+
*   Docker & Docker Compose (Optional, for containerized deployment)

## Getting Started

### 1. Database Setup
Create a MySQL database:
```sql
CREATE DATABASE hrms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configure Environment
Set the following environment variables or update `src/main/resources/application.yml`:

| Variable | Description | Default |
|---|---|---|
| `DB_HOST` | Database host | `localhost` |
| `DB_PORT` | Database port | `3306` |
| `DB_NAME` | Database name | `hrms_db` |
| `DB_USERNAME` | Database user | `root` |
| `DB_PASSWORD` | Database password | `password` |
| `JWT_SECRET` | JWT signing key | (Needs strong base64 key) |
| `JWT_EXPIRATION` | Token expiry (ms) | `3600000` (1 hour) |

### 3. Build and Run
```bash
# Clean and package
mvn clean package -DskipTests

# Run the application
java -jar target/hr-management-system-0.0.1-SNAPSHOT.jar
```
Or using Maven:
```bash
mvn spring-boot:run
```

### Using Docker
```bash
docker-compose up -d --build
```

## API Documentation
Once the application is running, access the Swagger UI interface at:
`http://localhost:8080/swagger-ui.html`

All API responses follow a standardized format:
```json
{
  "code": 1000,
  "message": "Success",
  "success": true,
  "data": { },
  "timestamp": "2023-10-27T10:00:00Z",
  "path": "/api/v1/resource"
}
```

## Contributing
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License
This project is licensed under the MIT License - see the LICENSE file for details.

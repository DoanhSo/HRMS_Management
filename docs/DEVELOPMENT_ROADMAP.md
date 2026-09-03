# HR Management System - Development Roadmap

This document outlines the phased development roadmap for the HR Management System (HRMS) built with Spring Boot 4.1.0 and Java 21. The roadmap is structured to deliver a fully functional Version 1 (MySQL only) before progressing to advanced caching (Version 2) and extended enterprise features (Version 3).

---

## Phase 1: Project Initialization
**Estimated Effort:** 2-3 Days
**Dependencies:** None

**Objectives:** Set up project structure, configure Spring Boot, database, Docker
**Database:** Flyway setup, initial migration scripts

**Tasks:**
- Configure `pom.xml` (add missing dependencies: MapStruct, Flyway, SpringDoc OpenAPI)
- *Note:* Remove `spring-boot-starter-data-redis` from `pom.xml` for V1
- Set up `application.yaml` with profiles (`dev`, `test`, `prod`)
- Configure Docker Compose (MySQL)
- Set up Flyway migration directory (`db/migration`)
- Create base package structure (all feature packages + common packages)
- Implement `ApiResponse<T>` wrapper for standardizing all API responses
- Implement `ErrorCode` enum
- Implement `GlobalExceptionHandler`
- Implement base entity (`BaseEntity` with `id`, `createdAt`, `updatedAt`)
- Configure Swagger/OpenAPI for API documentation

**Testing Checklist:**
- [ ] Verify application starts successfully
- [ ] Verify Flyway runs and executes initial scripts
- [ ] Verify Swagger UI is accessible and configured

---

## Phase 2: Authentication & Authorization
**Estimated Effort:** 3-4 Days
**Dependencies:** Phase 1

**Objectives:** JWT auth, RBAC, login/logout/refresh
**Database:** `V1__create_auth_tables.sql` (users, roles, permissions, role_permissions, user_roles, refresh_tokens)
**APIs:** `POST /login`, `POST /logout`, `POST /refresh-token`, `POST /change-password`
**DTOs:** `LoginRequest`, `TokenResponse`, `RefreshTokenRequest`, `ChangePasswordRequest`
**Services:** `AuthService`, `JwtService`, `RefreshTokenService`, `CustomUserDetailsService`

**Business Rules:**
- JWT generation/validation
- BCrypt password encoding
- Refresh token rotation
- Account lockout after multiple failed login attempts

**Testing Checklist:**
- [ ] Login with valid credentials returns tokens
- [ ] Login with invalid credentials returns 401
- [ ] Refresh token returns new access token
- [ ] Expired access token returns 403
- [ ] Logout revokes refresh token
- [ ] Change password works
- [ ] Account locks after N failed attempts

---

## Phase 3: Employee Module
**Estimated Effort:** 3 Days
**Dependencies:** Phase 2

**Objectives:** Full CRUD, profile management
**Database:** `V2__create_employee_table.sql`
**APIs:** `GET/POST/PUT/DELETE /employees`, `GET/PUT /employees/me`
**DTOs:** `EmployeeCreateRequest`, `EmployeeUpdateRequest`, `EmployeeResponse`, `EmployeeDetailResponse`
**Services:** `EmployeeService`

**Business Rules:**
- Employee code auto-generation
- Status transitions (e.g., Active, On Leave, Terminated)
- Self-profile access and modification

**Testing Checklist:**
- [ ] CRUD operations work correctly
- [ ] Pagination and search work
- [ ] Employee code is unique and auto-generated
- [ ] Only HR/Admin can create/update/delete employees
- [ ] Employee can view/update own profile
- [ ] Validation works (required fields, email format, etc.)

---

## Phase 4: Department Module
**Estimated Effort:** 2 Days
**Dependencies:** Phase 3

**Objectives:** Department CRUD with hierarchy support
**Database:** `V3__create_department_table.sql`
**APIs:** `GET/POST/PUT/DELETE /departments`, `GET /departments/{id}/employees`
**DTOs:** `DepartmentCreateRequest`, `DepartmentUpdateRequest`, `DepartmentResponse`
**Services:** `DepartmentService`

**Business Rules:**
- Unique department code
- Manager assignment
- Parent-child hierarchy support

**Testing Checklist:**
- [ ] CRUD operations
- [ ] List employees in department
- [ ] Assign manager
- [ ] Department hierarchy works
- [ ] Cannot delete department with employees

---

## Phase 5: Position Module
**Estimated Effort:** 2 Days
**Dependencies:** Phase 3

**Objectives:** Position CRUD with salary ranges
**Database:** `V4__create_position_table.sql`
**APIs:** `GET/POST/PUT/DELETE /positions`
**DTOs:** `PositionCreateRequest`, `PositionUpdateRequest`, `PositionResponse`
**Services:** `PositionService`

**Business Rules:**
- Salary range validation (min ≤ basic ≤ max)
- Unique position code

**Testing Checklist:**
- [ ] CRUD operations
- [ ] Salary range validation
- [ ] Cannot delete position with assigned employees

---

## Phase 6: Attendance Module
**Estimated Effort:** 4-5 Days
**Dependencies:** Phase 3

**Objectives:** Check-in/out, attendance tracking, reporting
**Database:** `V5__create_attendance_tables.sql` (attendance_records, attendance_config)
**APIs:** `POST /check-in`, `POST /check-out`, `GET /history`, `GET /daily-report`, `GET /monthly-report`
**DTOs:** `CheckInRequest`, `CheckOutRequest`, `AttendanceResponse`, `DailyReportResponse`, `MonthlyReportResponse`
**Services:** `AttendanceService`, `AttendanceConfigService`

**Business Rules:**
- All attendance rules from `ATTENDANCE_DESIGN.md`

**Testing Checklist:**
- [ ] Check-in records correctly
- [ ] Duplicate check-in rejected
- [ ] Late arrival detected and late_minutes calculated
- [ ] Check-out calculates work_hours and overtime
- [ ] Early leave detected
- [ ] Daily report accurate
- [ ] Monthly report aggregates correctly
- [ ] Config changes affect calculations

---

## Phase 7: Leave Module
**Estimated Effort:** 3-4 Days
**Dependencies:** Phase 6

**Objectives:** Leave request workflow, balance management
**Database:** `V6__create_leave_tables.sql` (leave_types, leave_balances, leave_requests)
**APIs:** `POST/GET /requests`, `PUT /approve`, `PUT /reject`, `GET /balances`, `GET /types`
**DTOs:** `LeaveCreateRequest`, `LeaveResponse`, `LeaveBalanceResponse`
**Services:** `LeaveService`, `LeaveBalanceService`

**Business Rules:**
- Balance check before approval
- Manager/HR approval
- Leave-attendance integration

**Testing Checklist:**
- [ ] Submit leave request
- [ ] Cannot exceed leave balance
- [ ] Manager can approve/reject
- [ ] Balance updated on approval
- [ ] Balance restored on cancellation
- [ ] Leave reflected in attendance

---

## Phase 8: Payroll Module
**Estimated Effort:** 5-6 Days
**Dependencies:** Phase 6, Phase 7

**Objectives:** Salary calculation engine, payroll management
**Database:** `V7__create_payroll_tables.sql` (payroll_periods, payroll_records, salary_rules)
**APIs:** Full payroll API set from `API_DESIGN.md`
**DTOs:** `PayrollPeriodRequest`, `PayrollRecordResponse`, `SalaryRuleRequest`, `SalaryRuleResponse`
**Services:** `PayrollService`, `SalaryCalculationService`, `SalaryRuleService`

**Business Rules:**
- All payroll rules from `PAYROLL_DESIGN.md`

**Testing Checklist:**
- [ ] Create payroll period
- [ ] Calculate salary for all employees
- [ ] Salary rules applied correctly
- [ ] Overtime calculated from attendance
- [ ] Deductions for absences/late
- [ ] Tax calculation works
- [ ] Insurance deductions correct
- [ ] Net salary = gross - deductions

---

## Phase 9: Dashboard Module
**Estimated Effort:** 2 Days
**Dependencies:** Phase 3-8

**Objectives:** Aggregated statistics
**Database:** No new tables (queries existing data)
**APIs:** `GET /summary`, `GET /attendance-overview`, `GET /department-stats`, `GET /payroll-summary`
**DTOs:** `DashboardSummaryResponse`, `AttendanceOverviewResponse`, `DepartmentStatsResponse`, `PayrollSummaryResponse`
**Services:** `DashboardService`

**Business Rules:**
- Data aggregation
- Access control (admin/HR see all, manager sees department)

**Testing Checklist:**
- [ ] Summary returns correct counts
- [ ] Attendance overview matches records
- [ ] Department stats accurate
- [ ] Payroll summary aggregates correctly
- [ ] Access control works per role

---

## Phase 10: Testing & Quality
**Estimated Effort:** 4-5 Days
**Dependencies:** All V1 Modules

**Objectives:** Comprehensive testing, code quality

**Tasks:**
- Unit tests for all services (target: 80%+ coverage)
- Integration tests for repositories
- Controller tests with `MockMvc`
- Security tests (authentication, authorization)
- Edge case tests
- Performance testing for payroll calculation

---

## Phase 11: Optimization & Polish
**Estimated Effort:** 3 Days
**Dependencies:** Phase 10

**Objectives:** Performance, documentation, deployment

**Tasks:**
- Database query optimization (N+1, indexing)
- API response time profiling
- Swagger documentation polish
- Docker Compose for full stack
- README finalization
- Code review and refactoring

---

## Version 2: Redis Integration
**Estimated Effort:** 3-4 Days
**Prerequisites:** Version 1 fully complete
**Objectives:** Add Redis for caching and token management

**Tasks:**
- Add `spring-boot-starter-data-redis` back to `pom.xml`
- Add Redis to Docker Compose
- Implement `RedisTokenStore` (swap out `JdbcTokenStore`)
- Add `@Cacheable` to dashboard queries
- Add `@Cacheable` to department/position lists
- Move JWT blacklist to Redis
- Move refresh tokens to Redis
- Performance comparison before/after

---

## Version 3: Advanced Features
**Estimated Effort:** 5-7 Days
**Prerequisites:** Version 2 complete
**Objectives:** Extend capabilities with external integrations and background jobs

**Tasks:**
- **Email Notifications:** (Spring Mail) for leave approvals, payroll generation, etc.
- **Scheduled Tasks:** (Spring Scheduler) for marking absent employees, expiry cleanup
- **Excel Export:** (Apache POI) for attendance and payroll reports
- **PDF Export:** (iText/JasperReports) for salary slips
- **Audit Logging:** Custom annotation + AOP for entity tracking
- **File Upload:** MinIO integration for profile pictures and document attachments

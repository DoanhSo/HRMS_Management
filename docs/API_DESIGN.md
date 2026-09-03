# API Design Document - HR Management System (HRMS)

This document details the API specifications for the HR Management System.

## 1. API Response Standard

All API responses follow a standard `ApiResponse<T>` wrapper to ensure consistency.

```java
public class ApiResponse<T> {
    private int code;
    private String message;
    private boolean success;
    private T data;
    private String timestamp;
    private String path;
}
```

### Response Codes

| Code | Type | Description |
|------|------|-------------|
| 1000 | Success | Request was successful |
| 1001 | Success | Resource created successfully |
| 1002 | Success | Resource updated successfully |
| 1003 | Success | Resource deleted successfully |
| 2001 | Client Error | Validation Error |
| 2002 | Client Error | Resource Not Found |
| 2003 | Client Error | Duplicate Resource |
| 3001 | Auth Error | Unauthorized |
| 3002 | Auth Error | Forbidden |
| 3003 | Auth Error | Token Expired |
| 3004 | Auth Error | Invalid Token |
| 4001 | Server Error | Internal Server Error |
| 4002 | Server Error | Service Unavailable |

### Pagination Standard

For endpoints returning collections of items, a paginated response format is used within the `data` field:

```json
{
  "code": 1000,
  "message": "Success",
  "success": true,
  "data": {
    "content": [
      // array of objects
    ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  },
  "timestamp": "2023-10-25T10:00:00Z",
  "path": "/api/v1/resource"
}
```

## 2. Authentication

All endpoints EXCEPT `/api/v1/auth/login` and `/api/v1/auth/refresh-token` require a Bearer JWT token in the `Authorization` header.

```
Authorization: Bearer <your_jwt_token_here>
```

---

## 3. API Endpoints by Module

### 3.1 Auth Module (`/api/v1/auth`)

#### 1. Login
- **Method & Path**: `POST /login`
- **Description**: Authenticates a user and returns access and refresh tokens.
- **Required Role**: Public
- **Request Body**:
  ```json
  {
    "username": "admin",
    "password": "password123"
  }
  ```
- **Response**: `TokenResponse`
  ```json
  {
    "code": 1000,
    "message": "Success",
    "success": true,
    "data": {
      "accessToken": "eyJhbG...",
      "refreshToken": "d7a8s9..."
    }
  }
  ```

#### 2. Logout
- **Method & Path**: `POST /logout`
- **Description**: Revokes the refresh token and invalidates the access token.
- **Required Role**: Any Authenticated User
- **Response**: Void

#### 3. Refresh Token
- **Method & Path**: `POST /refresh-token`
- **Description**: Issues a new access token using a valid refresh token.
- **Required Role**: Public
- **Request Body**:
  ```json
  {
    "refreshToken": "d7a8s9..."
  }
  ```
- **Response**: `TokenResponse`

#### 4. Change Password
- **Method & Path**: `POST /change-password`
- **Description**: Updates the current user's password.
- **Required Role**: Any Authenticated User
- **Request Body**:
  ```json
  {
    "oldPassword": "old",
    "newPassword": "new"
  }
  ```
- **Response**: Void

---

### 3.2 Employee Module (`/api/v1/employees`)

#### 1. List Employees
- **Method & Path**: `GET /`
- **Description**: Retrieves a paginated list of employees.
- **Query Params**: `page`, `size`, `search`, `status`, `departmentId`
- **Required Role**: HR, ADMIN, MANAGER
- **Response**: Paginated `EmployeeResponse`

#### 2. Get Employee
- **Method & Path**: `GET /{id}`
- **Description**: Retrieves details of a single employee.
- **Required Role**: HR, ADMIN, MANAGER
- **Response**: `EmployeeResponse`

#### 3. Create Employee
- **Method & Path**: `POST /`
- **Description**: Creates a new employee record.
- **Required Role**: HR, ADMIN
- **Request Body**: `EmployeeCreateRequest`
- **Response**: `EmployeeResponse`

#### 4. Update Employee
- **Method & Path**: `PUT /{id}`
- **Description**: Updates an existing employee.
- **Required Role**: HR, ADMIN
- **Request Body**: `EmployeeUpdateRequest`
- **Response**: `EmployeeResponse`

#### 5. Delete Employee
- **Method & Path**: `DELETE /{id}`
- **Description**: Soft deletes an employee.
- **Required Role**: ADMIN
- **Response**: Void

#### 6. Get Current Profile
- **Method & Path**: `GET /me`
- **Description**: Retrieves the logged-in user's profile.
- **Required Role**: Any Authenticated User
- **Response**: `EmployeeResponse`

#### 7. Update Current Profile
- **Method & Path**: `PUT /me`
- **Description**: Updates the logged-in user's profile.
- **Required Role**: Any Authenticated User
- **Response**: `EmployeeResponse`

---

### 3.3 Department Module (`/api/v1/departments`)

#### 1. List Departments
- **Method & Path**: `GET /`
- **Description**: Paginated list of departments.
- **Required Role**: Any Authenticated User
- **Response**: Paginated `DepartmentResponse`

#### 2. Get Department
- **Method & Path**: `GET /{id}`
- **Description**: Single department details with employee count.
- **Required Role**: Any Authenticated User
- **Response**: `DepartmentResponse`

#### 3. Create Department
- **Method & Path**: `POST /`
- **Description**: Creates a new department.
- **Required Role**: ADMIN
- **Response**: `DepartmentResponse`

#### 4. Update Department
- **Method & Path**: `PUT /{id}`
- **Description**: Updates a department.
- **Required Role**: ADMIN
- **Response**: `DepartmentResponse`

#### 5. Delete Department
- **Method & Path**: `DELETE /{id}`
- **Description**: Soft deletes a department.
- **Required Role**: ADMIN
- **Response**: Void

#### 6. Get Employees in Department
- **Method & Path**: `GET /{id}/employees`
- **Description**: Lists all employees belonging to the department.
- **Required Role**: HR, ADMIN, MANAGER
- **Response**: List of `EmployeeResponse`

---

### 3.4 Position Module (`/api/v1/positions`)

#### 1. List Positions
- **Method & Path**: `GET /`
- **Description**: Paginated list of positions.
- **Required Role**: Any Authenticated User

#### 2. Get Position
- **Method & Path**: `GET /{id}`
- **Description**: Single position details.
- **Required Role**: Any Authenticated User

#### 3. Create Position
- **Method & Path**: `POST /`
- **Description**: Creates a new position.
- **Required Role**: ADMIN, HR

#### 4. Update Position
- **Method & Path**: `PUT /{id}`
- **Description**: Updates a position.
- **Required Role**: ADMIN, HR

#### 5. Delete Position
- **Method & Path**: `DELETE /{id}`
- **Description**: Soft deletes a position.
- **Required Role**: ADMIN

---

### 3.5 Attendance Module (`/api/v1/attendances`)

#### 1. Check In
- **Method & Path**: `POST /check-in`
- **Description**: Records an employee check-in.
- **Required Role**: EMPLOYEE

#### 2. Check Out
- **Method & Path**: `POST /check-out`
- **Description**: Records an employee check-out.
- **Required Role**: EMPLOYEE

#### 3. Today's Attendance
- **Method & Path**: `GET /today`
- **Description**: Current user's attendance record for today.
- **Required Role**: EMPLOYEE

#### 4. Attendance History
- **Method & Path**: `GET /history`
- **Description**: Current user's attendance history (supports date range).
- **Query Params**: `startDate`, `endDate`, `page`, `size`
- **Required Role**: EMPLOYEE

#### 5. Employee History
- **Method & Path**: `GET /employees/{id}/history`
- **Description**: Specific employee's attendance history.
- **Required Role**: HR, MANAGER, ADMIN

#### 6. Daily Report
- **Method & Path**: `GET /daily-report`
- **Description**: Daily attendance report across all employees.
- **Query Params**: `date`
- **Required Role**: HR, ADMIN

#### 7. Monthly Report
- **Method & Path**: `GET /monthly-report`
- **Description**: Monthly attendance report.
- **Query Params**: `month`, `year`
- **Required Role**: HR, ADMIN

---

### 3.6 Leave Module (`/api/v1/leaves`)

#### 1. Create Request
- **Method & Path**: `POST /requests`
- **Description**: Submits a leave request.
- **Required Role**: EMPLOYEE

#### 2. List Own Requests
- **Method & Path**: `GET /requests`
- **Description**: Lists current user's leave requests.
- **Required Role**: EMPLOYEE

#### 3. Get Request Details
- **Method & Path**: `GET /requests/{id}`
- **Description**: Gets details of a specific request.
- **Required Role**: ANY (if owner, MANAGER, HR, ADMIN)

#### 4. Cancel Request
- **Method & Path**: `PUT /requests/{id}/cancel`
- **Description**: Cancels an active request (before approval).
- **Required Role**: Owner of the request

#### 5. Approve Request
- **Method & Path**: `PUT /requests/{id}/approve`
- **Description**: Approves a leave request.
- **Required Role**: MANAGER, HR, ADMIN

#### 6. Reject Request
- **Method & Path**: `PUT /requests/{id}/reject`
- **Description**: Rejects a leave request.
- **Request Body**: `RejectRequest` (with reason)
- **Required Role**: MANAGER, HR, ADMIN

#### 7. Leave Balances
- **Method & Path**: `GET /balances`
- **Description**: Current user's leave balances.
- **Required Role**: EMPLOYEE

#### 8. Employee Leave Balances
- **Method & Path**: `GET /balances/{employeeId}`
- **Description**: An employee's leave balances.
- **Required Role**: HR, ADMIN

#### 9. List Leave Types
- **Method & Path**: `GET /types`
- **Description**: Lists all configured leave types.
- **Required Role**: Any Authenticated User

---

### 3.7 Payroll Module (`/api/v1/payroll`)

#### 1. Create Payroll Period
- **Method & Path**: `POST /periods`
- **Description**: Initiates a new payroll period.
- **Required Role**: HR, ADMIN

#### 2. List Payroll Periods
- **Method & Path**: `GET /periods`
- **Description**: Lists past and present payroll periods.
- **Required Role**: HR, ADMIN, MANAGER

#### 3. Calculate Payroll
- **Method & Path**: `POST /periods/{id}/calculate`
- **Description**: Triggers salary calculation for the period.
- **Required Role**: HR, ADMIN

#### 4. Get Records for Period
- **Method & Path**: `GET /periods/{id}/records`
- **Description**: Details of salary records within a period.
- **Required Role**: HR, ADMIN, MANAGER

#### 5. Single Payroll Record
- **Method & Path**: `GET /records/{id}`
- **Description**: Gets specific salary record details.
- **Required Role**: HR, ADMIN, Owner

#### 6. Current User's Payroll History
- **Method & Path**: `GET /my-records`
- **Description**: Lists payroll history for the logged-in user.
- **Required Role**: EMPLOYEE

#### 7. Confirm Record
- **Method & Path**: `PUT /records/{id}/confirm`
- **Description**: Finalizes a payroll record for payout.
- **Required Role**: HR, ADMIN

#### 8. List Salary Rules
- **Method & Path**: `GET /salary-rules`
- **Description**: Lists formulas and rules for salary calculations.
- **Required Role**: HR, ADMIN

#### 9. Create Salary Rule
- **Method & Path**: `POST /salary-rules`
- **Description**: Defines a new calculation rule.
- **Required Role**: ADMIN

#### 10. Update Salary Rule
- **Method & Path**: `PUT /salary-rules/{id}`
- **Description**: Updates an existing rule.
- **Required Role**: ADMIN

---

### 3.8 Dashboard Module (`/api/v1/dashboard`)

#### 1. Summary
- **Method & Path**: `GET /summary`
- **Description**: General metrics (employee count, attendance, on leave, monthly payroll totals).
- **Required Role**: HR, ADMIN, MANAGER

#### 2. Attendance Overview
- **Method & Path**: `GET /attendance-overview`
- **Description**: Breakdown of today's attendance stats.
- **Required Role**: HR, ADMIN, MANAGER

#### 3. Department Stats
- **Method & Path**: `GET /department-stats`
- **Description**: Metrics grouped by department.
- **Required Role**: HR, ADMIN, MANAGER

#### 4. Payroll Summary
- **Method & Path**: `GET /payroll-summary`
- **Description**: Monthly payroll trends and totals.
- **Required Role**: HR, ADMIN, MANAGER

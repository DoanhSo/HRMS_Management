# Entity Relationship Diagram (ERD)

## 1. Module-Level Dependency Diagram

This diagram illustrates the high-level dependencies between the core modules in the HRMS application.

```mermaid
graph TD
    Auth[Auth Module]
    Emp[Employee Module]
    Dept[Department Module]
    Pos[Position Module]
    Att[Attendance Module]
    Leave[Leave Module]
    Pay[Payroll Module]

    Emp --> Auth
    Emp --> Dept
    Emp --> Pos
    Dept --> Emp
    Pos --> Dept
    Att --> Emp
    Leave --> Emp
    Pay --> Emp
    Pay --> Att
    Pay --> Leave
```

---

## 2. Full Database ER Diagram

The following Mermaid ER diagram shows the tables, columns, and relationship cardinalities across all modules.

```mermaid
erDiagram
    %% Auth Module
    users {
        BIGINT id PK
        VARCHAR username UK
        VARCHAR email UK
        VARCHAR password
        BOOLEAN enabled
        BOOLEAN account_non_locked
        INT failed_login_attempts
        DATETIME lock_time
    }
    roles {
        BIGINT id PK
        VARCHAR name UK
        VARCHAR description
    }
    permissions {
        BIGINT id PK
        VARCHAR name UK
        VARCHAR module
    }
    role_permissions {
        BIGINT role_id PK,FK
        BIGINT permission_id PK,FK
    }
    user_roles {
        BIGINT user_id PK,FK
        BIGINT role_id PK,FK
    }
    refresh_tokens {
        BIGINT id PK
        VARCHAR token UK
        BIGINT user_id FK
        DATETIME expiry_date
        BOOLEAN revoked
    }

    %% Employee Module
    employees {
        BIGINT id PK
        VARCHAR employee_code UK
        BIGINT user_id UK,FK
        VARCHAR first_name
        VARCHAR last_name
        DATE date_of_birth
        ENUM gender
        VARCHAR phone
        TEXT address
        DATE hire_date
        DATE termination_date
        ENUM employment_status
        BIGINT department_id FK
        BIGINT position_id FK
        BIGINT manager_id FK
        VARCHAR profile_picture_url
    }

    %% Department Module
    departments {
        BIGINT id PK
        VARCHAR name UK
        VARCHAR code UK
        TEXT description
        BIGINT manager_id FK
        BIGINT parent_department_id FK
        BOOLEAN active
    }

    %% Position Module
    positions {
        BIGINT id PK
        VARCHAR title
        VARCHAR code UK
        TEXT description
        BIGINT department_id FK
        DECIMAL basic_salary
        DECIMAL min_salary
        DECIMAL max_salary
        BOOLEAN active
    }

    %% Attendance Module
    attendance_records {
        BIGINT id PK
        BIGINT employee_id FK
        DATE date
        TIME check_in_time
        TIME check_out_time
        ENUM status
        INT late_minutes
        INT early_leave_minutes
        DECIMAL overtime_hours
        DECIMAL work_hours
        TEXT notes
    }
    attendance_config {
        BIGINT id PK
        VARCHAR config_key UK
        VARCHAR config_value
        TEXT description
        VARCHAR data_type
        BOOLEAN active
    }

    %% Leave Module
    leave_types {
        BIGINT id PK
        VARCHAR name UK
        VARCHAR code UK
        INT max_days_per_year
        BOOLEAN is_paid
        BOOLEAN carry_forward
        INT max_carry_forward_days
        BOOLEAN active
    }
    leave_balances {
        BIGINT id PK
        BIGINT employee_id FK
        BIGINT leave_type_id FK
        INT year
        DECIMAL total_days
        DECIMAL used_days
        DECIMAL remaining_days
    }
    leave_requests {
        BIGINT id PK
        BIGINT employee_id FK
        BIGINT leave_type_id FK
        DATE start_date
        DATE end_date
        DECIMAL total_days
        TEXT reason
        ENUM status
        BIGINT approved_by FK
        DATETIME approved_at
        TEXT rejection_reason
    }

    %% Payroll Module
    payroll_periods {
        BIGINT id PK
        VARCHAR name
        INT month
        INT year
        DATE start_date
        DATE end_date
        ENUM status
    }
    payroll_records {
        BIGINT id PK
        BIGINT employee_id FK
        BIGINT payroll_period_id FK
        DECIMAL basic_salary
        DECIMAL total_allowances
        DECIMAL overtime_pay
        DECIMAL gross_salary
        DECIMAL total_deductions
        DECIMAL net_salary
        INT work_days
        ENUM status
    }
    salary_rules {
        BIGINT id PK
        VARCHAR name
        VARCHAR code UK
        ENUM rule_type
        ENUM calculation_type
        DECIMAL value
        VARCHAR percentage_of
        JSON conditions
        BOOLEAN active
    }

    %% Relationships
    users ||--o{ refresh_tokens : "has"
    users ||--o{ user_roles : "assigned to"
    roles ||--o{ user_roles : "includes"
    roles ||--o{ role_permissions : "granted"
    permissions ||--o{ role_permissions : "assigned"
    
    users ||--o| employees : "mapped to"
    departments ||--o{ departments : "parent of"
    departments ||--o| employees : "managed by"
    positions ||--o{ departments : "belongs to"
    
    employees ||--o{ employees : "manages"
    employees ||--o{ attendance_records : "logs"
    employees ||--o{ leave_balances : "owns"
    employees ||--o{ leave_requests : "requests"
    employees ||--o{ payroll_records : "receives"
    employees }o--|| departments : "works in"
    employees }o--|| positions : "holds"
    
    leave_types ||--o{ leave_balances : "defines"
    leave_types ||--o{ leave_requests : "categorizes"
    leave_requests }o--o| employees : "approved by"
    
    payroll_periods ||--o{ payroll_records : "processes"
```

---

## 3. Relationships Summary Table

| Entity (Source) | Relationship | Entity (Target) | Cardinality | Notes |
| :--- | :--- | :--- | :--- | :--- |
| `users` | One-to-One | `employees` | 1:1 | An employee profile corresponds to exactly one user account. |
| `users` | One-to-Many | `refresh_tokens` | 1:N | A user can have multiple active devices/tokens. |
| `users` | Many-to-Many | `roles` | M:N | Resolved via `user_roles`. |
| `roles` | Many-to-Many | `permissions` | M:N | Resolved via `role_permissions`. |
| `departments` | One-to-One | `employees` | 1:1 | An employee can manage a department. |
| `departments` | One-to-Many | `departments` | 1:N | Self-referencing to represent hierarchy (parent_department_id). |
| `departments` | One-to-Many | `positions` | 1:N | Positions are department-specific. |
| `employees` | One-to-Many | `employees` | 1:N | Self-referencing to represent manager-subordinate relationship. |
| `employees` | Many-to-One | `departments` | N:1 | Employees belong to one department. |
| `employees` | Many-to-One | `positions` | N:1 | Employees hold one position. |
| `employees` | One-to-Many | `attendance_records` | 1:N | Daily logs for each employee. |
| `employees` | One-to-Many | `leave_balances` | 1:N | Quota per leave type per year. |
| `employees` | One-to-Many | `leave_requests` | 1:N | Submitted time-off requests. |
| `employees` | One-to-Many | `payroll_records` | 1:N | Historical processed salaries. |
| `leave_types` | One-to-Many | `leave_balances` | 1:N | Leave rules define how balances behave. |
| `payroll_periods` | One-to-Many | `payroll_records` | 1:N | Monthly run groups multiple employee pay slips. |

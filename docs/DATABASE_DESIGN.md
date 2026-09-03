# Database Design Document

## 1. Introduction
This document outlines the database schema for the HR Management System (HRMS). It covers all tables across the core modules: Authentication, Employee, Department, Position, Attendance, Leave, and Payroll.

## 2. Global Strategies & Conventions

### Naming Conventions
- **Tables**: lowercase, plural, words separated by underscores (`snake_case`). Example: `users`, `leave_requests`.
- **Columns**: lowercase, singular, words separated by underscores (`snake_case`). Example: `first_name`, `created_at`.
- **Primary Keys**: `id` for surrogate keys.
- **Foreign Keys**: `[table_singular_name]_id`. Example: `employee_id`, `department_id`.
- **Indexes**: `idx_[table_name]_[column_name]`. Example: `idx_users_username`.
- **Unique Constraints**: `uk_[table_name]_[column_name]`. Example: `uk_users_email`.

### Audit Columns Strategy
Most tables in the system include standard audit columns to track data lifecycle:
- `created_at` (DATETIME): Timestamp when the record was inserted.
- `updated_at` (DATETIME): Timestamp when the record was last updated.
- `created_by` (BIGINT): User ID who created the record (where applicable).
- `updated_by` (BIGINT): User ID who last updated the record (where applicable).

### Soft Delete Strategy
Instead of physical deletion, a soft delete approach is generally not universally applied via a `deleted` flag. Instead:
- Entities like `users`, `departments`, `positions`, `attendance_config`, `leave_types`, and `salary_rules` use an `active` or `enabled` boolean flag.
- Historical data (attendance, payroll, leaves) is retained immutably or status-managed (e.g., `CANCELLED`).
- This ensures referential integrity for reporting and historical auditing.

### Data Type Choices Rationale
- **Primary Keys**: `BIGINT` with `AUTO_INCREMENT` to support large datasets.
- **Monetary/Salary Values**: `DECIMAL(15,2)` to prevent floating-point inaccuracies and support large values.
- **Percentages/Rates**: `DECIMAL(15,4)` or `DECIMAL(4,2)` for higher precision.
- **Statuses/Types**: `ENUM` for fixed predefined values or `VARCHAR` with constraints if extensibility is preferred.
- **Strings**: `VARCHAR` for short text (emails, names), `TEXT` for longer descriptions, notes, or JSON data.

### Indexing Strategy
- **Primary Keys**: Automatically indexed.
- **Foreign Keys**: Explicitly indexed to optimize `JOIN` operations.
- **Unique Columns**: Unique indexes are applied.
- **Search/Filter Columns**: Frequently filtered columns (e.g., `status`, `date`, `active`) have B-Tree indexes.
- **Composite Indexes**: Used for common multi-column queries, such as `(employee_id, date)` in attendance or `(employee_id, payroll_period_id)` in payroll.

---

## 3. Module Tables Details

### 3.1 Auth Module

#### `users`
- **Purpose**: Stores application user credentials and account status.
- **Columns**:
  - `id` (BIGINT PK AUTO_INCREMENT)
  - `username` (VARCHAR 50 UNIQUE NOT NULL)
  - `email` (VARCHAR 100 UNIQUE NOT NULL)
  - `password` (VARCHAR 255 NOT NULL)
  - `enabled` (BOOLEAN DEFAULT true)
  - `account_non_locked` (BOOLEAN DEFAULT true)
  - `failed_login_attempts` (INT DEFAULT 0)
  - `lock_time` (DATETIME)
  - `created_at` (DATETIME), `updated_at` (DATETIME)
  - `created_by` (BIGINT), `updated_by` (BIGINT)
- **Indexes**: `idx_users_username`, `idx_users_email`.

#### `roles`
- **Purpose**: Defines system roles (e.g., ROLE_ADMIN, ROLE_HR, ROLE_EMPLOYEE).
- **Columns**:
  - `id` (BIGINT PK)
  - `name` (VARCHAR 50 UNIQUE NOT NULL)
  - `description` (VARCHAR 255)
  - `created_at` (DATETIME)

#### `permissions`
- **Purpose**: Defines granular system permissions.
- **Columns**:
  - `id` (BIGINT PK)
  - `name` (VARCHAR 100 UNIQUE NOT NULL)
  - `description` (VARCHAR 255)
  - `module` (VARCHAR 50)
  - `created_at` (DATETIME)

#### `role_permissions`
- **Purpose**: Join table resolving many-to-many relationship between roles and permissions.
- **Columns**:
  - `role_id` (BIGINT PK, FK to roles)
  - `permission_id` (BIGINT PK, FK to permissions)

#### `user_roles`
- **Purpose**: Join table resolving many-to-many relationship between users and roles.
- **Columns**:
  - `user_id` (BIGINT PK, FK to users)
  - `role_id` (BIGINT PK, FK to roles)

#### `refresh_tokens`
- **Purpose**: Stores JWT refresh tokens (V1 MySQL implementation).
- **Columns**:
  - `id` (BIGINT PK)
  - `token` (VARCHAR 500 UNIQUE NOT NULL)
  - `user_id` (BIGINT FK to users)
  - `expiry_date` (DATETIME NOT NULL)
  - `revoked` (BOOLEAN DEFAULT false)
  - `created_at` (DATETIME)
- **Indexes**: `idx_refresh_tokens_token`, `idx_refresh_tokens_user`.

### 3.2 Employee Module

#### `employees`
- **Purpose**: Core employee profile information.
- **Columns**:
  - `id` (BIGINT PK AUTO_INCREMENT)
  - `employee_code` (VARCHAR 20 UNIQUE NOT NULL)
  - `user_id` (BIGINT UNIQUE FK to users)
  - `first_name` (VARCHAR 50 NOT NULL)
  - `last_name` (VARCHAR 50 NOT NULL)
  - `date_of_birth` (DATE)
  - `gender` (ENUM: MALE, FEMALE, OTHER)
  - `phone` (VARCHAR 20)
  - `address` (TEXT)
  - `hire_date` (DATE NOT NULL)
  - `termination_date` (DATE)
  - `employment_status` (ENUM: ACTIVE, ON_LEAVE, TERMINATED, PROBATION)
  - `department_id` (BIGINT FK to departments)
  - `position_id` (BIGINT FK to positions)
  - `manager_id` (BIGINT FK to employees) - self-referential
  - `profile_picture_url` (VARCHAR 500)
  - `created_at` (DATETIME), `updated_at` (DATETIME)
- **Indexes**: `idx_emp_code`, `idx_emp_status`, `idx_emp_dept`, `idx_emp_manager`.

### 3.3 Department Module

#### `departments`
- **Purpose**: Organizational units within the company.
- **Columns**:
  - `id` (BIGINT PK)
  - `name` (VARCHAR 100 UNIQUE NOT NULL)
  - `code` (VARCHAR 20 UNIQUE NOT NULL)
  - `description` (TEXT)
  - `manager_id` (BIGINT FK to employees)
  - `parent_department_id` (BIGINT FK to departments) - self-referential hierarchy
  - `active` (BOOLEAN DEFAULT true)
  - `created_at` (DATETIME), `updated_at` (DATETIME)

### 3.4 Position Module

#### `positions`
- **Purpose**: Job titles and salary bands.
- **Columns**:
  - `id` (BIGINT PK)
  - `title` (VARCHAR 100 NOT NULL)
  - `code` (VARCHAR 20 UNIQUE NOT NULL)
  - `description` (TEXT)
  - `department_id` (BIGINT FK to departments)
  - `basic_salary` (DECIMAL 15,2 NOT NULL)
  - `min_salary` (DECIMAL 15,2)
  - `max_salary` (DECIMAL 15,2)
  - `active` (BOOLEAN DEFAULT true)
  - `created_at` (DATETIME), `updated_at` (DATETIME)

### 3.5 Attendance Module

#### `attendance_records`
- **Purpose**: Daily clock-in/clock-out tracking.
- **Columns**:
  - `id` (BIGINT PK)
  - `employee_id` (BIGINT NOT NULL FK to employees)
  - `date` (DATE NOT NULL)
  - `check_in_time` (TIME)
  - `check_out_time` (TIME)
  - `status` (ENUM: PRESENT, ABSENT, LATE, HALF_DAY, ON_LEAVE, HOLIDAY)
  - `late_minutes` (INT DEFAULT 0)
  - `early_leave_minutes` (INT DEFAULT 0)
  - `overtime_hours` (DECIMAL 4,2 DEFAULT 0)
  - `work_hours` (DECIMAL 4,2)
  - `notes` (TEXT)
  - `created_at` (DATETIME), `updated_at` (DATETIME)
- **Unique**: `(employee_id, date)`
- **Indexes**: `idx_attendance_emp_date`

#### `attendance_config`
- **Purpose**: Configuration keys for attendance rules.
- **Columns**:
  - `id` (BIGINT PK)
  - `config_key` (VARCHAR 100 UNIQUE NOT NULL)
  - `config_value` (VARCHAR 255 NOT NULL)
  - `description` (TEXT)
  - `data_type` (VARCHAR 20)
  - `active` (BOOLEAN DEFAULT true)

### 3.6 Leave Module

#### `leave_types`
- **Purpose**: Defines categories of leaves (Annual, Sick, etc.).
- **Columns**:
  - `id` (BIGINT PK)
  - `name` (VARCHAR 50 UNIQUE NOT NULL)
  - `code` (VARCHAR 20 UNIQUE)
  - `description` (TEXT)
  - `max_days_per_year` (INT NOT NULL)
  - `is_paid` (BOOLEAN DEFAULT true)
  - `carry_forward` (BOOLEAN DEFAULT false)
  - `max_carry_forward_days` (INT DEFAULT 0)
  - `active` (BOOLEAN DEFAULT true)
  - `created_at` (DATETIME)

#### `leave_balances`
- **Purpose**: Tracks employee leave quotas per year.
- **Columns**:
  - `id` (BIGINT PK)
  - `employee_id` (BIGINT NOT NULL FK to employees)
  - `leave_type_id` (BIGINT NOT NULL FK to leave_types)
  - `year` (INT NOT NULL)
  - `total_days` (DECIMAL 4,1)
  - `used_days` (DECIMAL 4,1 DEFAULT 0)
  - `remaining_days` (DECIMAL 4,1)
  - `created_at` (DATETIME), `updated_at` (DATETIME)
- **Unique**: `(employee_id, leave_type_id, year)`

#### `leave_requests`
- **Purpose**: Employee requests for time off.
- **Columns**:
  - `id` (BIGINT PK)
  - `employee_id` (BIGINT NOT NULL FK to employees)
  - `leave_type_id` (BIGINT NOT NULL FK to leave_types)
  - `start_date` (DATE NOT NULL)
  - `end_date` (DATE NOT NULL)
  - `total_days` (DECIMAL 4,1 NOT NULL)
  - `reason` (TEXT NOT NULL)
  - `status` (ENUM: PENDING, APPROVED, REJECTED, CANCELLED)
  - `approved_by` (BIGINT FK to employees)
  - `approved_at` (DATETIME)
  - `rejection_reason` (TEXT)
  - `created_at` (DATETIME), `updated_at` (DATETIME)
- **Indexes**: `idx_leave_req_emp`, `idx_leave_req_status`.

### 3.7 Payroll Module

#### `payroll_periods`
- **Purpose**: Defines payroll cycles (usually monthly).
- **Columns**:
  - `id` (BIGINT PK)
  - `name` (VARCHAR 50)
  - `month` (INT NOT NULL)
  - `year` (INT NOT NULL)
  - `start_date` (DATE NOT NULL)
  - `end_date` (DATE NOT NULL)
  - `status` (ENUM: DRAFT, PROCESSING, COMPLETED, CANCELLED)
  - `created_at` (DATETIME), `updated_at` (DATETIME)
- **Unique**: `(month, year)`

#### `payroll_records`
- **Purpose**: Processed salary details for employees.
- **Columns**:
  - `id` (BIGINT PK)
  - `employee_id` (BIGINT NOT NULL FK to employees)
  - `payroll_period_id` (BIGINT NOT NULL FK to payroll_periods)
  - `basic_salary` (DECIMAL 15,2)
  - `total_allowances` (DECIMAL 15,2 DEFAULT 0)
  - `overtime_pay` (DECIMAL 15,2 DEFAULT 0)
  - `gross_salary` (DECIMAL 15,2)
  - `insurance_deduction` (DECIMAL 15,2 DEFAULT 0)
  - `tax_deduction` (DECIMAL 15,2 DEFAULT 0)
  - `other_deductions` (DECIMAL 15,2 DEFAULT 0)
  - `total_deductions` (DECIMAL 15,2)
  - `net_salary` (DECIMAL 15,2)
  - `work_days` (INT)
  - `absent_days` (INT DEFAULT 0)
  - `late_count` (INT DEFAULT 0)
  - `overtime_hours` (DECIMAL 5,2 DEFAULT 0)
  - `status` (ENUM: DRAFT, CONFIRMED, PAID)
  - `notes` (TEXT)
  - `created_at` (DATETIME), `updated_at` (DATETIME)
- **Unique**: `(employee_id, payroll_period_id)`

#### `salary_rules`
- **Purpose**: Configurable engine rules for calculating allowances, deductions, and taxes.
- **Columns**:
  - `id` (BIGINT PK)
  - `name` (VARCHAR 100 NOT NULL)
  - `code` (VARCHAR 50 UNIQUE NOT NULL)
  - `rule_type` (ENUM: ALLOWANCE, DEDUCTION, TAX, INSURANCE, OVERTIME, BONUS)
  - `calculation_type` (ENUM: FIXED, PERCENTAGE, FORMULA)
  - `value` (DECIMAL 15,4)
  - `percentage_of` (VARCHAR 50)
  - `description` (TEXT)
  - `priority` (INT DEFAULT 0)
  - `active` (BOOLEAN DEFAULT true)
  - `conditions` (JSON)
  - `created_at` (DATETIME), `updated_at` (DATETIME)

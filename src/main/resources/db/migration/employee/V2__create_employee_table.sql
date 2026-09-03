-- =============================================================================
-- Flyway Migration: V2__create_employee_table.sql
-- Module: Employee
-- =============================================================================

CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(20) NOT NULL UNIQUE,
    user_id BIGINT UNIQUE NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NULL,
    gender VARCHAR(10) NULL,
    phone VARCHAR(20) NULL,
    address TEXT NULL,
    hire_date DATE NOT NULL,
    termination_date DATE NULL,
    employment_status VARCHAR(20) NOT NULL DEFAULT 'PROBATION',
    department_id BIGINT NULL,
    position_id BIGINT NULL,
    manager_id BIGINT NULL,
    profile_picture_url VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT fk_emp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_emp_manager FOREIGN KEY (manager_id) REFERENCES employees(id) ON DELETE SET NULL
);

CREATE INDEX idx_emp_code ON employees(employee_code);
CREATE INDEX idx_emp_status ON employees(employment_status);
CREATE INDEX idx_emp_dept ON employees(department_id);

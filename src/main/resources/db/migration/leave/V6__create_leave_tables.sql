-- =============================================================================
-- Flyway Migration: V6__create_leave_tables.sql
-- Module: Leave Management (Leave Types, Balances, Requests)
-- =============================================================================

CREATE TABLE leave_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(30) NOT NULL UNIQUE,
    description TEXT NULL,
    paid BOOLEAN NOT NULL DEFAULT TRUE,
    default_days_per_year INT NOT NULL DEFAULT 12,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL
);

CREATE TABLE leave_balances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    year INT NOT NULL,
    total_days DECIMAL(4,1) NOT NULL DEFAULT 12.0,
    used_days DECIMAL(4,1) NOT NULL DEFAULT 0.0,
    remaining_days DECIMAL(4,1) NOT NULL DEFAULT 12.0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT fk_lb_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_lb_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types(id) ON DELETE CASCADE,
    CONSTRAINT uk_emp_leave_year UNIQUE (employee_id, leave_type_id, year)
);

CREATE TABLE leave_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days DECIMAL(4,1) NOT NULL,
    reason TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approver_id BIGINT NULL,
    rejection_reason TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT fk_lr_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_lr_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types(id) ON DELETE CASCADE,
    CONSTRAINT fk_lr_approver FOREIGN KEY (approver_id) REFERENCES employees(id) ON DELETE SET NULL
);

-- Seed Default Leave Types
INSERT INTO leave_types (id, name, code, description, paid, default_days_per_year) VALUES
(1, 'Annual Leave', 'ANNUAL', 'Standard paid annual leave', TRUE, 12),
(2, 'Sick Leave', 'SICK', 'Paid leave for illness with medical certificate', TRUE, 5),
(3, 'Maternity Leave', 'MATERNITY', 'Maternity leave for female employees', TRUE, 180),
(4, 'Unpaid Leave', 'UNPAID', 'Leave without pay', FALSE, 0);

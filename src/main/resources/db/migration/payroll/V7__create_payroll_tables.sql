-- =============================================================================
-- Flyway Migration: V7__create_payroll_tables.sql
-- Module: Payroll Management (Payroll Periods & Payslips)
-- =============================================================================

CREATE TABLE payroll_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    working_days INT NOT NULL DEFAULT 22,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT uk_payroll_year_month UNIQUE (year, month)
);

CREATE TABLE payslips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payroll_period_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    basic_salary DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    actual_work_days DECIMAL(4,1) NOT NULL DEFAULT 0.0,
    gross_salary DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    allowances DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    deductions DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    tax DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    net_salary DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'CALCULATED',
    notes TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT fk_ps_period FOREIGN KEY (payroll_period_id) REFERENCES payroll_periods(id) ON DELETE CASCADE,
    CONSTRAINT fk_ps_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT uk_period_emp UNIQUE (payroll_period_id, employee_id)
);

CREATE INDEX idx_ps_period ON payslips(payroll_period_id);
CREATE INDEX idx_ps_emp ON payslips(employee_id);

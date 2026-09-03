-- =============================================================================
-- Flyway Migration: V5__create_attendance_table.sql
-- Module: Attendance
-- =============================================================================

CREATE TABLE attendances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    check_in DATETIME NULL,
    check_out DATETIME NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PRESENT',
    total_work_hours DECIMAL(4,2) NOT NULL DEFAULT 0.00,
    notes TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT fk_att_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT uk_emp_work_date UNIQUE (employee_id, work_date)
);

CREATE INDEX idx_att_date ON attendances(work_date);
CREATE INDEX idx_att_status ON attendances(status);

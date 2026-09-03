-- =============================================================================
-- Flyway Migration: V4__create_position_table.sql
-- Module: Position
-- =============================================================================

CREATE TABLE positions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT NULL,
    department_id BIGINT NULL,
    basic_salary DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    min_salary DECIMAL(15,2) NULL,
    max_salary DECIMAL(15,2) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT fk_pos_dept FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

CREATE INDEX idx_pos_code ON positions(code);
CREATE INDEX idx_pos_dept ON positions(department_id);
CREATE INDEX idx_pos_active ON positions(active);

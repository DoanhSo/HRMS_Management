-- =============================================================================
-- Flyway Migration: V11__create_performance_kpi_tables.sql
-- Module: Performance Appraisal, KPI Evaluation & Salary Scales
-- =============================================================================

-- 1. Tiêu chí đánh giá KPI
CREATE TABLE kpi_criteria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    department_id BIGINT NULL,
    weight INT NOT NULL DEFAULT 20,
    target_description TEXT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT fk_kpi_crit_dept FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- 2. Phiếu đánh giá KPI định kỳ của nhân viên
CREATE TABLE kpi_evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    period_month INT NOT NULL,
    period_year INT NOT NULL,
    evaluator_id BIGINT NULL,
    total_score DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    rating VARCHAR(10) NOT NULL DEFAULT 'C',
    kpi_coefficient DECIMAL(3,2) NOT NULL DEFAULT 1.00,
    bonus_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    feedback TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT fk_kpi_eval_emp FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_kpi_eval_evaluator FOREIGN KEY (evaluator_id) REFERENCES employees(id) ON DELETE SET NULL,
    CONSTRAINT uk_emp_period_kpi UNIQUE (employee_id, period_year, period_month)
);

-- 3. Điểm chi tiết từng tiêu chí trong phiếu KPI
CREATE TABLE kpi_evaluation_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kpi_evaluation_id BIGINT NOT NULL,
    kpi_criteria_id BIGINT NOT NULL,
    score DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    weight INT NOT NULL DEFAULT 20,
    comments TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT fk_kpi_dtl_eval FOREIGN KEY (kpi_evaluation_id) REFERENCES kpi_evaluations(id) ON DELETE CASCADE,
    CONSTRAINT fk_kpi_dtl_crit FOREIGN KEY (kpi_criteria_id) REFERENCES kpi_criteria(id) ON DELETE CASCADE
);

-- 4. Thang bảng lương và hệ số lương
CREATE TABLE salary_scales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(100) NOT NULL,
    position_id BIGINT NULL,
    coefficient DECIMAL(4,2) NOT NULL DEFAULT 1.00,
    base_salary DECIMAL(15,2) NOT NULL DEFAULT 5000000.00,
    standard_bonus DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT fk_scale_pos FOREIGN KEY (position_id) REFERENCES positions(id) ON DELETE SET NULL
);

-- Seed Initial KPI Criteria
INSERT INTO kpi_criteria (id, code, name, department_id, weight, target_description, active) VALUES
(1, 'KPI_WORK_QUALITY', 'Chất lượng công việc & Độ chính xác', NULL, 30, 'Hoàn thành công việc đúng tiêu chuẩn, không có lỗi nghiêm trọng', TRUE),
(2, 'KPI_DEADLINE', 'Tiến độ hoàn thành nhiệm vụ', NULL, 25, 'Hoàn thành 100% đúng hạn (On-time delivery)', TRUE),
(3, 'KPI_PRODUCTIVITY', 'Năng suất & Khối lượng công việc', NULL, 25, 'Đạt hoặc vượt định mức KPI số lượng đề ra', TRUE),
(4, 'KPI_TEAMWORK', 'Kỷ luật & Tinh thần đồng đội', NULL, 20, 'Tuân thủ nội quy, hỗ trợ đồng nghiệp, đóng góp tích cực', TRUE);

-- Seed Initial Salary Scales
INSERT INTO salary_scales (id, code, title, coefficient, base_salary, standard_bonus, active) VALUES
(1, 'SCALE_DEV_L1', 'Kỹ sư Phần mềm - Bậc 1', 1.00, 15000000.00, 2000000.00, TRUE),
(2, 'SCALE_DEV_L2', 'Kỹ sư Phần mềm - Bậc 2', 1.30, 15000000.00, 3000000.00, TRUE),
(3, 'SCALE_DEV_SR', 'Kỹ sư Phần mềm Cao cấp', 1.80, 15000000.00, 5000000.00, TRUE),
(4, 'SCALE_HR_L1', 'Chuyên viên Nhân sự - Bậc 1', 1.00, 12000000.00, 1500000.00, TRUE),
(5, 'SCALE_MGR_L1', 'Trưởng phòng / Quản lý - Bậc 1', 2.00, 15000000.00, 8000000.00, TRUE);

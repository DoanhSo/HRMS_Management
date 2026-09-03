-- =============================================================================
-- Flyway Migration: V12__add_performance_indexes.sql
-- Module: Database Query Optimization & Composite Indexes
-- =============================================================================

-- 1. Tối ưu tìm kiếm & lọc nhân viên theo phòng ban, trạng thái và ngày vào làm
CREATE INDEX idx_emp_search_perf ON employees(department_id, employment_status, hire_date);

-- 2. Tối ưu tính toán ngày công lương (Đếm nhanh theo khoảng ngày và loại trừ ABSENT)
CREATE INDEX idx_att_emp_date_status ON attendances(employee_id, work_date, status);

-- 3. Tối ưu xuất báo cáo chấm công & lọc theo ngày làm việc
CREATE INDEX idx_att_date_dept ON attendances(work_date, employee_id);

-- 4. Tối ưu tìm kiếm phiếu lương theo kỳ và phòng ban
CREATE INDEX idx_ps_period_dept ON payslips(payroll_period_id, employee_id, status);

-- 5. Tối ưu kiểm tra trùng lịch nghỉ phép & duyệt đơn
CREATE INDEX idx_lr_emp_status_date ON leave_requests(employee_id, status, start_date, end_date);

-- 6. Tối ưu tra cứu KPI tức thì khi tính lương
CREATE INDEX idx_kpi_emp_period_status ON kpi_evaluations(employee_id, period_year, period_month, status);

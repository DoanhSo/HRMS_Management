-- =============================================================================
-- Flyway Migration: V10__seed_sample_data.sql
-- Module: Full Sample Dataset (Users, Employees, Departments, Positions, Leaves, Attendances, Payroll, Notifications, Audit)
-- =============================================================================

-- 1. Insert Additional Roles & Users (Default password for all demo accounts: Admin@123)
INSERT INTO users (id, username, email, password, enabled, account_non_locked, failed_login_attempts) VALUES
(2, 'hr_manager', 'hr@hrms.com', '$2a$10$O2WLrs1Exotc9JDIMTQMue7bEiRVvAJ4jz6Ix/d4D8B1aLIK8y5.i', TRUE, TRUE, 0),
(3, 'tech_lead', 'lead.tech@hrms.com', '$2a$10$O2WLrs1Exotc9JDIMTQMue7bEiRVvAJ4jz6Ix/d4D8B1aLIK8y5.i', TRUE, TRUE, 0),
(4, 'dev_senior', 'dev.senior@hrms.com', '$2a$10$O2WLrs1Exotc9JDIMTQMue7bEiRVvAJ4jz6Ix/d4D8B1aLIK8y5.i', TRUE, TRUE, 0),
(5, 'dev_junior', 'dev.junior@hrms.com', '$2a$10$O2WLrs1Exotc9JDIMTQMue7bEiRVvAJ4jz6Ix/d4D8B1aLIK8y5.i', TRUE, TRUE, 0),
(6, 'accountant', 'accountant@hrms.com', '$2a$10$O2WLrs1Exotc9JDIMTQMue7bEiRVvAJ4jz6Ix/d4D8B1aLIK8y5.i', TRUE, TRUE, 0),
(7, 'sales_exec', 'sales@hrms.com', '$2a$10$O2WLrs1Exotc9JDIMTQMue7bEiRVvAJ4jz6Ix/d4D8B1aLIK8y5.i', TRUE, TRUE, 0)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- Map User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(2, 2), -- hr_manager -> ROLE_HR
(3, 3), -- tech_lead -> ROLE_MANAGER
(4, 4), -- dev_senior -> ROLE_EMPLOYEE
(5, 4), -- dev_junior -> ROLE_EMPLOYEE
(6, 4), -- accountant -> ROLE_EMPLOYEE
(7, 4)  -- sales_exec -> ROLE_EMPLOYEE
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 2. Insert Departments
INSERT INTO departments (id, name, code, description, active) VALUES
(1, 'Ban Giám Đốc', 'BOD', 'Ban điều hành và quản lý chiến lược tổng thể doanh nghiệp', TRUE),
(2, 'Phòng Nhân Sự', 'HR', 'Quản lý tuyển dụng, đào tạo, chế độ phúc lợi và nhân sự', TRUE),
(3, 'Phòng Công Nghệ & Phần Mềm', 'IT_DEV', 'Nghiên cứu, phát triển hệ thống và sản phẩm công nghệ', TRUE),
(4, 'Phòng Kế Toán - Tài Chính', 'FIN', 'Quản lý thu chi, ngân sách, quyết toán thuế và bảng lương', TRUE),
(5, 'Phòng Kinh Doanh & Tiếp Thị', 'MKT_SALES', 'Phát triển thị trường, kinh doanh và chăm sóc khách hàng', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3. Insert Positions
INSERT INTO positions (id, title, code, description, department_id, basic_salary, min_salary, max_salary, active) VALUES
(1, 'Tổng Giám Đốc (CEO)', 'BOD_CEO', 'Điều hành mọi hoạt động và định hướng chiến lược doanh nghiệp', 1, 80000000.00, 60000000.00, 120000000.00, TRUE),
(2, 'Trưởng Phòng Nhân Sự', 'HR_MGR', 'Quản lý toàn diện hoạt động nhân sự và chế độ phúc lợi', 2, 30000000.00, 25000000.00, 40000000.00, TRUE),
(3, 'Chuyên Viên Tuyển Dụng & C&B', 'HR_SPEC', 'Thực hiện công tác tuyển dụng, chấm công và tính lương', 2, 16000000.00, 12000000.00, 20000000.00, TRUE),
(4, 'Trưởng Nhóm Kỹ Thuật (Tech Lead)', 'IT_LEAD', 'Chịu trách nhiệm kiến trúc kỹ thuật và dẫn dắt team phát triển', 3, 45000000.00, 35000000.00, 60000000.00, TRUE),
(5, 'Kỹ Sư Phần Mềm Cao Cấp (Senior Dev)', 'IT_DEV_SR', 'Phát triển các module phức tạp và tối ưu hiệu năng', 3, 32000000.00, 25000000.00, 42000000.00, TRUE),
(6, 'Kỹ Sư Phần Mềm (Junior Dev)', 'IT_DEV_JR', 'Tham gia lập trình tính năng và viết unit test', 3, 15000000.00, 10000000.00, 18000000.00, TRUE),
(7, 'Kế Toán Trưởng', 'FIN_CHIEF', 'Quản trị báo cáo tài chính, quyết toán và ngân sách', 4, 28000000.00, 22000000.00, 35000000.00, TRUE),
(8, 'Chuyên Viên Kinh Doanh (Sales Executive)', 'SALES_EXEC', 'Tìm kiếm đối tác và mở rộng kênh bán hàng', 5, 14000000.00, 10000000.00, 25000000.00, TRUE)
ON DUPLICATE KEY UPDATE title = VALUES(title);

-- 4. Insert Employees
INSERT INTO employees (id, employee_code, user_id, first_name, last_name, date_of_birth, gender, phone, address, hire_date, employment_status, department_id, position_id, manager_id) VALUES
(1, 'EMP-00001', 1, 'Nguyễn', 'Quốc Doanh', '1990-03-15', 'MALE', '0912345678', 'Tòa nhà Landmark 81, TP. Hồ Chí Minh', '2022-01-01', 'ACTIVE', 1, 1, NULL),
(2, 'EMP-00002', 2, 'Trần', 'Thị Mai', '1993-08-20', 'FEMALE', '0987654321', 'Số 123 Đường Cầu Giấy, Hà Nội', '2022-03-15', 'ACTIVE', 2, 2, 1),
(3, 'EMP-00003', 3, 'Lê', 'Hoàng Long', '1991-11-05', 'MALE', '0933221100', 'Số 45 Nguyễn Huệ, Quận 1, TP. HCM', '2022-05-01', 'ACTIVE', 3, 4, 1),
(4, 'EMP-00004', 4, 'Phạm', 'Quốc Hùng', '1995-06-12', 'MALE', '0944556677', 'Số 88 Lê Duẩn, Quận Hoàn Kiếm, Hà Nội', '2023-02-15', 'ACTIVE', 3, 5, 3),
(5, 'EMP-00005', 5, 'Hoàng', 'Minh Tuấn', '1998-09-25', 'MALE', '0977889900', 'Số 12 Quang Trung, Hà Đông, Hà Nội', '2024-06-01', 'PROBATION', 3, 6, 3),
(6, 'EMP-00006', 6, 'Đặng', 'Thu Thảo', '1994-04-18', 'FEMALE', '0901122334', 'Số 68 Võ Văn Tần, Quận 3, TP. HCM', '2022-08-10', 'ACTIVE', 4, 7, 1),
(7, 'EMP-00007', 7, 'Vũ', 'Đức Anh', '1996-12-30', 'MALE', '0966778899', 'Số 99 Trần Duy Hưng, Cầu Giấy, Hà Nội', '2023-11-01', 'ACTIVE', 5, 8, 1)
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name);

-- 5. Update Department Managers
UPDATE departments SET manager_id = 1 WHERE id = 1;
UPDATE departments SET manager_id = 2 WHERE id = 2;
UPDATE departments SET manager_id = 3 WHERE id = 3;
UPDATE departments SET manager_id = 6 WHERE id = 4;
UPDATE departments SET manager_id = 7 WHERE id = 5;

-- 6. Insert Leave Balances for 2026
INSERT INTO leave_balances (employee_id, leave_type_id, year, total_days, used_days, remaining_days) VALUES
(1, 1, 2026, 12.0, 2.0, 10.0),
(1, 2, 2026, 5.0, 0.0, 5.0),
(2, 1, 2026, 12.0, 3.0, 9.0),
(2, 2, 2026, 5.0, 1.0, 4.0),
(3, 1, 2026, 12.0, 1.0, 11.0),
(3, 2, 2026, 5.0, 0.0, 5.0),
(4, 1, 2026, 12.0, 4.0, 8.0),
(4, 2, 2026, 5.0, 0.0, 5.0),
(5, 1, 2026, 12.0, 0.0, 12.0),
(5, 2, 2026, 5.0, 0.0, 5.0),
(6, 1, 2026, 12.0, 2.0, 10.0),
(6, 2, 2026, 5.0, 0.0, 5.0),
(7, 1, 2026, 12.0, 1.0, 11.0),
(7, 2, 2026, 5.0, 0.0, 5.0)
ON DUPLICATE KEY UPDATE remaining_days = VALUES(remaining_days);

-- 7. Insert Leave Requests
INSERT INTO leave_requests (id, employee_id, leave_type_id, start_date, end_date, total_days, reason, status, approver_id) VALUES
(1, 4, 1, '2026-07-10', '2026-07-12', 3.0, 'Nghỉ du lịch hè cùng gia đình', 'APPROVED', 3),
(2, 2, 1, '2026-08-01', '2026-08-02', 2.0, 'Giải quyết việc cá nhân gia đình', 'APPROVED', 1),
(3, 4, 1, '2026-08-25', '2026-08-26', 2.0, 'Nghỉ phép thường niên', 'PENDING', 3),
(4, 5, 2, '2026-08-15', '2026-08-15', 1.0, 'Bị sốt rét có giấy khám bác sĩ', 'APPROVED', 3),
(5, 7, 1, '2026-08-28', '2026-08-29', 2.0, 'Đi đám cưới bạn thân ở quê', 'PENDING', 1)
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- 8. Insert Attendances for August 2026
INSERT INTO attendances (employee_id, work_date, check_in, check_out, status, total_work_hours, notes) VALUES
(1, '2026-08-18', '2026-08-18 08:25:00', '2026-08-18 17:35:00', 'PRESENT', 8.0, 'Đúng giờ'),
(1, '2026-08-19', '2026-08-19 08:30:00', '2026-08-19 17:40:00', 'PRESENT', 8.0, 'Đúng giờ'),
(2, '2026-08-18', '2026-08-18 08:15:00', '2026-08-18 17:30:00', 'PRESENT', 8.0, 'Đúng giờ'),
(2, '2026-08-19', '2026-08-19 08:45:00', '2026-08-19 17:30:00', 'LATE', 7.5, 'Đi muộn 15 phút do kẹt xe'),
(3, '2026-08-18', '2026-08-18 08:30:00', '2026-08-18 18:00:00', 'PRESENT', 8.5, 'Làm thêm 30 phút'),
(3, '2026-08-19', '2026-08-19 08:20:00', '2026-08-19 17:30:00', 'PRESENT', 8.0, 'Đúng giờ'),
(4, '2026-08-18', '2026-08-18 08:28:00', '2026-08-18 17:32:00', 'PRESENT', 8.0, 'Đúng giờ'),
(4, '2026-08-19', '2026-08-19 08:30:00', '2026-08-19 16:30:00', 'EARLY_LEAVE', 7.0, 'Về sớm 1 tiếng xin phép lead'),
(5, '2026-08-18', '2026-08-18 08:30:00', '2026-08-18 17:30:00', 'PRESENT', 8.0, 'Đúng giờ'),
(5, '2026-08-19', '2026-08-19 08:25:00', '2026-08-19 17:35:00', 'PRESENT', 8.0, 'Đúng giờ'),
(6, '2026-08-18', '2026-08-18 08:20:00', '2026-08-18 17:30:00', 'PRESENT', 8.0, 'Đúng giờ'),
(6, '2026-08-19', '2026-08-19 08:30:00', '2026-08-19 17:30:00', 'PRESENT', 8.0, 'Đúng giờ'),
(7, '2026-08-18', '2026-08-18 08:50:00', '2026-08-18 17:40:00', 'LATE', 7.5, 'Gặp khách hàng buổi sáng'),
(7, '2026-08-19', '2026-08-19 08:30:00', '2026-08-19 17:30:00', 'PRESENT', 8.0, 'Đúng giờ')
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- 9. Insert Payroll Periods & Payslips
INSERT INTO payroll_periods (id, name, year, month, start_date, end_date, working_days, status) VALUES
(1, 'Kỳ Lương Tháng 07/2026', 2026, 7, '2026-07-01', '2026-07-31', 22, 'PAID'),
(2, 'Kỳ Lương Tháng 08/2026', 2026, 8, '2026-08-01', '2026-08-31', 22, 'CALCULATED')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO payslips (id, payroll_period_id, employee_id, basic_salary, actual_work_days, gross_salary, allowances, deductions, tax, net_salary, status, notes) VALUES
(1, 1, 1, 80000000.00, 22.0, 80000000.00, 10000000.00, 8400000.00, 12500000.00, 69100000.00, 'PAID', 'Đã thanh toán qua VCB'),
(2, 1, 2, 30000000.00, 22.0, 30000000.00, 3000000.00, 3150000.00, 2450000.00, 27400000.00, 'PAID', 'Đã thanh toán qua Techcombank'),
(3, 1, 3, 45000000.00, 22.0, 45000000.00, 5000000.00, 4725000.00, 5100000.00, 40175000.00, 'PAID', 'Đã thanh toán qua ACB'),
(4, 1, 4, 32000000.00, 22.0, 32000000.00, 2500000.00, 3360000.00, 2800000.00, 28340000.00, 'PAID', 'Đã thanh toán qua TPBank'),
(5, 1, 5, 15000000.00, 22.0, 15000000.00, 1500000.00, 1575000.00, 450000.00, 14475000.00, 'PAID', 'Đã thanh toán qua MBBank'),
(6, 1, 6, 28000000.00, 22.0, 28000000.00, 2000000.00, 2940000.00, 2100000.00, 24960000.00, 'PAID', 'Đã thanh toán qua VietinBank'),
(7, 1, 7, 14000000.00, 22.0, 14000000.00, 5000000.00, 1470000.00, 750000.00, 16780000.00, 'PAID', 'Đã thanh toán kèm hoa hồng'),

(8, 2, 1, 80000000.00, 22.0, 80000000.00, 10000000.00, 8400000.00, 12500000.00, 69100000.00, 'CALCULATED', 'Kỳ lương hiện tại'),
(9, 2, 2, 30000000.00, 22.0, 30000000.00, 3000000.00, 3150000.00, 2450000.00, 27400000.00, 'CALCULATED', 'Kỳ lương hiện tại'),
(10, 2, 3, 45000000.00, 22.0, 45000000.00, 5000000.00, 4725000.00, 5100000.00, 40175000.00, 'CALCULATED', 'Kỳ lương hiện tại'),
(11, 2, 4, 32000000.00, 21.0, 30545454.00, 2500000.00, 3207272.00, 2600000.00, 27238182.00, 'CALCULATED', 'Trừ 1 ngày nghỉ không phép'),
(12, 2, 5, 15000000.00, 22.0, 15000000.00, 1500000.00, 1575000.00, 450000.00, 14475000.00, 'CALCULATED', 'Kỳ lương hiện tại'),
(13, 2, 6, 28000000.00, 22.0, 28000000.00, 2000000.00, 2940000.00, 2100000.00, 24960000.00, 'CALCULATED', 'Kỳ lương hiện tại'),
(14, 2, 7, 14000000.00, 22.0, 14000000.00, 4500000.00, 1470000.00, 680000.00, 16350000.00, 'CALCULATED', 'Kỳ lương hiện tại')
ON DUPLICATE KEY UPDATE gross_salary = VALUES(gross_salary);

-- 10. Insert Notifications
INSERT INTO notifications (id, user_id, type, title, message, link, is_read, created_at) VALUES
(1, 1, 'SYSTEM', 'Chào mừng đến với hệ thống HRMS', 'Hệ thống Quản lý Nhân sự HRMS đã sẵn sàng hoạt động với đầy đủ tính năng.', '/dashboard', FALSE, CURRENT_TIMESTAMP),
(2, 1, 'LEAVE_SUBMITTED', 'Đơn xin nghỉ phép mới cần duyệt', 'Nhân viên Phạm Quốc Hùng vừa gửi đơn xin nghỉ phép ngày 25/08 - 26/08.', '/leave', FALSE, CURRENT_TIMESTAMP),
(3, 4, 'LEAVE_APPROVED', 'Đơn xin nghỉ phép đã được duyệt', 'Đơn xin nghỉ phép từ 10/07 đến 12/07 của bạn đã được phê duyệt.', '/leave', TRUE, '2026-07-10 09:00:00'),
(4, 1, 'PAYSLIP_READY', 'Hoàn tất tính lương Tháng 08/2026', 'Bảng lương Tháng 08/2026 đã được tính toán tự động cho 7 nhân viên. Vui lòng kiểm tra và duyệt.', '/payroll', FALSE, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE type = VALUES(type);

-- 11. Insert Audit Logs
INSERT INTO audit_logs (user_id, username, action, entity_name, entity_id, details, ip_address, created_at) VALUES
(1, 'admin', 'LOGIN', 'AUTH', 1, 'Đăng nhập hệ thống thành công', '127.0.0.1', '2026-08-20 08:00:00'),
(1, 'admin', 'CREATE', 'EMPLOYEE', 1, 'Tạo mới hồ sơ nhân viên EMP-00001 (Nguyễn Quốc Doanh)', '127.0.0.1', '2026-08-20 08:05:00'),
(1, 'admin', 'CALCULATE', 'PAYROLL', 2, 'Tính lương tự động kỳ lương Tháng 08/2026 cho 7 nhân sự', '127.0.0.1', '2026-08-20 08:30:00');

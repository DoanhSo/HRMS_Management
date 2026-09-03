package com.ng_doanh.hr_management_system.report.service;

import com.ng_doanh.hr_management_system.attendance.entity.Attendance;
import com.ng_doanh.hr_management_system.attendance.repository.AttendanceRepository;
import com.ng_doanh.hr_management_system.common.util.ExcelHelper;
import com.ng_doanh.hr_management_system.department.entity.Department;
import com.ng_doanh.hr_management_system.department.repository.DepartmentRepository;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.payroll.entity.PayrollPeriod;
import com.ng_doanh.hr_management_system.payroll.entity.Payslip;
import com.ng_doanh.hr_management_system.position.entity.Position;
import com.ng_doanh.hr_management_system.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final AttendanceRepository attendanceRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    // =========================================================================
    // 1. Employee Export & Template
    // =========================================================================
    public byte[] exportEmployeesToExcel(List<Employee> employees) throws IOException {
        Map<Long, String> deptMap = departmentRepository.findAll().stream()
                .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a));
        Map<Long, String> posMap = positionRepository.findAll().stream()
                .collect(Collectors.toMap(Position::getId, Position::getTitle, (a, b) -> a));

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh Sách Nhân Viên");

            CellStyle headerStyle = ExcelHelper.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelHelper.createDataStyle(workbook);

            String[] columns = {
                    "Mã NV", "Họ đệm", "Tên", "Email", "Số ĐT", "Giới tính",
                    "Ngày sinh", "Phòng ban", "Chức vụ", "Ngày vào làm", "Trạng thái"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Employee emp : employees) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "");
                row.createCell(1).setCellValue(emp.getFirstName() != null ? emp.getFirstName() : "");
                row.createCell(2).setCellValue(emp.getLastName() != null ? emp.getLastName() : "");
                row.createCell(3).setCellValue(emp.getUser() != null ? emp.getUser().getEmail() : "");
                row.createCell(4).setCellValue(emp.getPhone() != null ? emp.getPhone() : "");
                row.createCell(5).setCellValue(emp.getGender() != null ? emp.getGender().name() : "");
                row.createCell(6).setCellValue(emp.getDateOfBirth() != null ? emp.getDateOfBirth().toString() : "");
                row.createCell(7).setCellValue(emp.getDepartmentId() != null ? deptMap.getOrDefault(emp.getDepartmentId(), "") : "");
                row.createCell(8).setCellValue(emp.getPositionId() != null ? posMap.getOrDefault(emp.getPositionId(), "") : "");
                row.createCell(9).setCellValue(emp.getHireDate() != null ? emp.getHireDate().toString() : "");
                row.createCell(10).setCellValue(emp.getEmploymentStatus() != null ? emp.getEmploymentStatus().name() : "");

                for (int c = 0; c < columns.length; c++) {
                    row.getCell(c).setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] generateEmployeeTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Import Template");

            CellStyle headerStyle = ExcelHelper.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelHelper.createDataStyle(workbook);

            String[] columns = {
                    "Mã NV (*)", "Họ đệm (*)", "Tên (*)", "Email (*)", "Số ĐT",
                    "Giới tính (MALE/FEMALE)", "Ngày sinh (YYYY-MM-DD)",
                    "Mã/Tên Phòng Ban (*)", "Mã/Tên Chức Vụ (*)", "Ngày vào làm (YYYY-MM-DD)"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Sample rows
            String[][] sampleData = {
                    {"EMP-00100", "Nguyễn Văn", "An", "an.nguyen@company.com", "0901234567", "MALE", "1995-05-15", "Kỹ Thuật", "Software Engineer", "2026-01-15"},
                    {"EMP-00101", "Trần Thị", "Bình", "binh.tran@company.com", "0912345678", "FEMALE", "1998-08-20", "Nhân Sự", "HR Specialist", "2026-02-01"}
            };

            for (int r = 0; r < sampleData.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < sampleData[r].length; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(sampleData[r][c]);
                    cell.setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    // 2. Attendance Export & Template
    // =========================================================================
    public byte[] exportMonthlyAttendanceToExcel(LocalDate startDate, LocalDate endDate, Long departmentId) throws IOException {
        Page<Attendance> page = attendanceRepository.searchAttendances(null, departmentId, startDate, endDate, null, Pageable.unpaged());
        List<Attendance> attendances = page.getContent();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bảng Chấm Công");

            CellStyle headerStyle = ExcelHelper.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelHelper.createDataStyle(workbook);

            String[] columns = {"ID", "Mã NV", "Họ và Tên", "Ngày làm việc", "Giờ vào", "Giờ ra", "Số giờ làm", "Trạng thái", "Ghi chú"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Attendance attendance : attendances) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(attendance.getId() != null ? attendance.getId() : 0);
                row.createCell(1).setCellValue(attendance.getEmployee() != null ? attendance.getEmployee().getEmployeeCode() : "");
                row.createCell(2).setCellValue(attendance.getEmployee() != null ? attendance.getEmployee().getFirstName() + " " + attendance.getEmployee().getLastName() : "");
                row.createCell(3).setCellValue(attendance.getWorkDate() != null ? attendance.getWorkDate().toString() : "");
                row.createCell(4).setCellValue(attendance.getCheckIn() != null ? attendance.getCheckIn().toLocalTime().toString() : "");
                row.createCell(5).setCellValue(attendance.getCheckOut() != null ? attendance.getCheckOut().toLocalTime().toString() : "");
                row.createCell(6).setCellValue(attendance.getTotalWorkHours() != null ? attendance.getTotalWorkHours().doubleValue() : 0.0);
                row.createCell(7).setCellValue(attendance.getStatus() != null ? attendance.getStatus().name() : "");
                row.createCell(8).setCellValue(attendance.getNotes() != null ? attendance.getNotes() : "");

                for (int c = 0; c < columns.length; c++) {
                    row.getCell(c).setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] generateAttendanceTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Import Chấm Công");

            CellStyle headerStyle = ExcelHelper.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelHelper.createDataStyle(workbook);

            String[] columns = {"Mã NV (*)", "Ngày làm việc (YYYY-MM-DD) (*)", "Giờ vào (HH:mm:ss)", "Giờ ra (HH:mm:ss)", "Ghi chú"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            String[][] sampleData = {
                    {"EMP-00001", "2026-08-20", "08:15:00", "17:35:00", "Chấm công máy quẹt vân tay"},
                    {"EMP-00002", "2026-08-20", "08:45:00", "17:30:00", "Đi muộn do tắc đường"}
            };

            for (int r = 0; r < sampleData.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < sampleData[r].length; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(sampleData[r][c]);
                    cell.setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    // 3. Payroll Export
    // =========================================================================
    public byte[] exportPayrollPeriodToExcel(PayrollPeriod period, List<Payslip> payslips) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bảng Lương " + period.getName());

            CellStyle headerStyle = ExcelHelper.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelHelper.createDataStyle(workbook);
            CellStyle currencyStyle = ExcelHelper.createCurrencyStyle(workbook);

            String[] columns = {
                    "Mã NV", "Họ và Tên", "Lương cơ bản", "Ngày công thực tế",
                    "Phụ cấp", "Tổng thu nhập (Gross)", "Giảm trừ / Khấu trừ", "Thuế TNCN", "Thực lĩnh (Net)", "Trạng thái"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Payslip p : payslips) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(p.getEmployee() != null ? p.getEmployee().getEmployeeCode() : "");
                row.createCell(1).setCellValue(p.getEmployee() != null ? p.getEmployee().getFirstName() + " " + p.getEmployee().getLastName() : "");

                Cell c2 = row.createCell(2); c2.setCellValue(p.getBasicSalary() != null ? p.getBasicSalary().doubleValue() : 0); c2.setCellStyle(currencyStyle);
                Cell c3 = row.createCell(3); c3.setCellValue(p.getActualWorkDays() != null ? p.getActualWorkDays().doubleValue() : 0); c3.setCellStyle(dataStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(p.getAllowances() != null ? p.getAllowances().doubleValue() : 0); c4.setCellStyle(currencyStyle);
                Cell c5 = row.createCell(5); c5.setCellValue(p.getGrossSalary() != null ? p.getGrossSalary().doubleValue() : 0); c5.setCellStyle(currencyStyle);
                Cell c6 = row.createCell(6); c6.setCellValue(p.getDeductions() != null ? p.getDeductions().doubleValue() : 0); c6.setCellStyle(currencyStyle);
                Cell c7 = row.createCell(7); c7.setCellValue(p.getTax() != null ? p.getTax().doubleValue() : 0); c7.setCellStyle(currencyStyle);
                Cell c8 = row.createCell(8); c8.setCellValue(p.getNetSalary() != null ? p.getNetSalary().doubleValue() : 0); c8.setCellStyle(currencyStyle);
                Cell c9 = row.createCell(9); c9.setCellValue(p.getStatus() != null ? p.getStatus().name() : ""); c9.setCellStyle(dataStyle);

                row.getCell(0).setCellStyle(dataStyle);
                row.getCell(1).setCellStyle(dataStyle);
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    // 4. Department & Position Export
    // =========================================================================
    public byte[] exportDepartmentsToExcel(List<Department> departments) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh Sách Phòng Ban");

            CellStyle headerStyle = ExcelHelper.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelHelper.createDataStyle(workbook);

            String[] columns = {"ID", "Mã PB", "Tên Phòng Ban", "Mô tả", "Trưởng bộ phận", "Trạng thái"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Department d : departments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(d.getId() != null ? d.getId() : 0);
                row.createCell(1).setCellValue(d.getCode() != null ? d.getCode() : "");
                row.createCell(2).setCellValue(d.getName() != null ? d.getName() : "");
                row.createCell(3).setCellValue(d.getDescription() != null ? d.getDescription() : "");
                row.createCell(4).setCellValue(d.getManager() != null ? d.getManager().getFirstName() + " " + d.getManager().getLastName() : "Chưa có");
                row.createCell(5).setCellValue(d.isActive() ? "Hoạt động" : "Tạm dừng");

                for (int c = 0; c < columns.length; c++) {
                    row.getCell(c).setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportPositionsToExcel(List<Position> positions) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh Sách Chức Vụ");

            CellStyle headerStyle = ExcelHelper.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelHelper.createDataStyle(workbook);
            CellStyle currencyStyle = ExcelHelper.createCurrencyStyle(workbook);

            String[] columns = {"ID", "Mã Chức Vụ", "Tên Chức Vụ", "Phòng Ban", "Lương Cơ Bản", "Lương Tối Thiểu", "Lương Tối Đa", "Trạng Thái"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Position p : positions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getId() != null ? p.getId() : 0);
                row.createCell(1).setCellValue(p.getCode() != null ? p.getCode() : "");
                row.createCell(2).setCellValue(p.getTitle() != null ? p.getTitle() : "");
                row.createCell(3).setCellValue(p.getDepartment() != null ? p.getDepartment().getName() : "");

                Cell c4 = row.createCell(4); c4.setCellValue(p.getBasicSalary() != null ? p.getBasicSalary().doubleValue() : 0); c4.setCellStyle(currencyStyle);
                Cell c5 = row.createCell(5); c5.setCellValue(p.getMinSalary() != null ? p.getMinSalary().doubleValue() : 0); c5.setCellStyle(currencyStyle);
                Cell c6 = row.createCell(6); c6.setCellValue(p.getMaxSalary() != null ? p.getMaxSalary().doubleValue() : 0); c6.setCellStyle(currencyStyle);

                row.createCell(7).setCellValue(p.isActive() ? "Hoạt động" : "Tạm dừng");

                row.getCell(0).setCellStyle(dataStyle);
                row.getCell(1).setCellStyle(dataStyle);
                row.getCell(2).setCellStyle(dataStyle);
                row.getCell(3).setCellStyle(dataStyle);
                row.getCell(7).setCellStyle(dataStyle);
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}

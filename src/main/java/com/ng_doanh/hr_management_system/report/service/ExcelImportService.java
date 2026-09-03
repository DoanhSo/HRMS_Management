package com.ng_doanh.hr_management_system.report.service;

import com.ng_doanh.hr_management_system.attendance.entity.Attendance;
import com.ng_doanh.hr_management_system.attendance.enums.AttendanceStatus;
import com.ng_doanh.hr_management_system.attendance.repository.AttendanceRepository;
import com.ng_doanh.hr_management_system.auth.entity.Role;
import com.ng_doanh.hr_management_system.auth.entity.User;
import com.ng_doanh.hr_management_system.auth.repository.RoleRepository;
import com.ng_doanh.hr_management_system.auth.repository.UserRepository;
import com.ng_doanh.hr_management_system.common.dto.ImportErrorDetail;
import com.ng_doanh.hr_management_system.common.dto.ImportResultResponse;
import com.ng_doanh.hr_management_system.common.enums.Gender;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.common.util.ExcelHelper;
import com.ng_doanh.hr_management_system.department.entity.Department;
import com.ng_doanh.hr_management_system.department.repository.DepartmentRepository;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.enums.EmploymentStatus;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import com.ng_doanh.hr_management_system.position.entity.Position;
import com.ng_doanh.hr_management_system.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final LocalTime WORK_START_TIME = LocalTime.of(8, 30);
    private static final LocalTime WORK_END_TIME = LocalTime.of(17, 30);

    @Transactional
    public ImportResultResponse importEmployees(MultipartFile file) {
        log.info("Importing employees from Excel file: {}", file.getOriginalFilename());
        validateFile(file);

        List<ImportErrorDetail> errors = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        // Pre-load lookup caches
        Map<String, Department> deptMap = new HashMap<>();
        departmentRepository.findAll().forEach(d -> {
            deptMap.put(d.getCode().toUpperCase(), d);
            deptMap.put(d.getName().toUpperCase(), d);
        });

        Map<String, Position> posMap = new HashMap<>();
        positionRepository.findAll().forEach(p -> {
            posMap.put(p.getCode().toUpperCase(), p);
            posMap.put(p.getTitle().toUpperCase(), p);
        });

        Role employeeRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .or(() -> roleRepository.findByName("EMPLOYEE"))
                .orElse(null);

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            for (int r = 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String empCode = ExcelHelper.getCellValueAsString(row.getCell(0));
                if (empCode.isEmpty()) continue; // Ignore empty rows

                totalRows++;
                int displayRow = r + 1;

                String firstName = ExcelHelper.getCellValueAsString(row.getCell(1));
                String lastName = ExcelHelper.getCellValueAsString(row.getCell(2));
                String email = ExcelHelper.getCellValueAsString(row.getCell(3));
                String phone = ExcelHelper.getCellValueAsString(row.getCell(4));
                String genderStr = ExcelHelper.getCellValueAsString(row.getCell(5));
                LocalDate dob = ExcelHelper.getCellValueAsLocalDate(row.getCell(6));
                String deptStr = ExcelHelper.getCellValueAsString(row.getCell(7));
                String posStr = ExcelHelper.getCellValueAsString(row.getCell(8));
                LocalDate hireDate = ExcelHelper.getCellValueAsLocalDate(row.getCell(9));

                // Validations
                if (firstName.isEmpty() || lastName.isEmpty()) {
                    errors.add(new ImportErrorDetail(displayRow, empCode, "Họ Tên", "Họ và tên không được để trống"));
                    continue;
                }

                if (email.isEmpty() || !email.contains("@")) {
                    errors.add(new ImportErrorDetail(displayRow, empCode, "Email", "Email không hợp lệ"));
                    continue;
                }

                if (employeeRepository.existsByEmployeeCode(empCode)) {
                    errors.add(new ImportErrorDetail(displayRow, empCode, "Mã NV", "Mã nhân viên '" + empCode + "' đã tồn tại trên hệ thống"));
                    continue;
                }

                if (userRepository.existsByEmail(email)) {
                    errors.add(new ImportErrorDetail(displayRow, empCode, "Email", "Email '" + email + "' đã được sử dụng"));
                    continue;
                }

                Department dept = deptMap.get(deptStr.toUpperCase());
                if (dept == null) {
                    errors.add(new ImportErrorDetail(displayRow, empCode, "Phòng Ban", "Không tìm thấy phòng ban '" + deptStr + "'"));
                    continue;
                }

                Position pos = posMap.get(posStr.toUpperCase());
                if (pos == null) {
                    errors.add(new ImportErrorDetail(displayRow, empCode, "Chức Vụ", "Không tìm thấy chức vụ '" + posStr + "'"));
                    continue;
                }

                Gender gender = Gender.MALE;
                if (!genderStr.isEmpty()) {
                    try {
                        gender = Gender.valueOf(genderStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        gender = genderStr.equalsIgnoreCase("Nữ") || genderStr.equalsIgnoreCase("Female") ? Gender.FEMALE : Gender.MALE;
                    }
                }

                if (hireDate == null) hireDate = LocalDate.now();

                // Create User Account for Employee
                String generatedUsername = empCode.toLowerCase().replace("-", "_");
                if (userRepository.existsByUsername(generatedUsername)) {
                    generatedUsername = email.split("@")[0];
                }

                Set<Role> roles = new HashSet<>();
                if (employeeRole != null) roles.add(employeeRole);

                User user = User.builder()
                        .username(generatedUsername)
                        .email(email)
                        .password(passwordEncoder.encode("Admin@123"))
                        .enabled(true)
                        .accountNonLocked(true)
                        .roles(roles)
                        .build();
                User savedUser = userRepository.save(user);

                // Create Employee Record
                Employee employee = Employee.builder()
                        .employeeCode(empCode)
                        .firstName(firstName)
                        .lastName(lastName)
                        .phone(phone)
                        .dateOfBirth(dob)
                        .gender(gender)
                        .departmentId(dept.getId())
                        .positionId(pos.getId())
                        .hireDate(hireDate)
                        .employmentStatus(EmploymentStatus.PROBATION)
                        .user(savedUser)
                        .build();

                employeeRepository.save(employee);
                successCount++;
            }
        } catch (Exception e) {
            log.error("Failed to parse Excel file", e);
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }

        return ImportResultResponse.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .failedCount(errors.size())
                .errors(errors)
                .build();
    }

    @Transactional
    public ImportResultResponse importAttendances(MultipartFile file) {
        log.info("Importing attendances from Excel file: {}", file.getOriginalFilename());
        validateFile(file);

        List<ImportErrorDetail> errors = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            for (int r = 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String empCode = ExcelHelper.getCellValueAsString(row.getCell(0));
                if (empCode.isEmpty()) continue;

                totalRows++;
                int displayRow = r + 1;

                LocalDate workDate = ExcelHelper.getCellValueAsLocalDate(row.getCell(1));
                LocalTime checkInTime = ExcelHelper.getCellValueAsLocalTime(row.getCell(2));
                LocalTime checkOutTime = ExcelHelper.getCellValueAsLocalTime(row.getCell(3));
                String notes = ExcelHelper.getCellValueAsString(row.getCell(4));

                if (workDate == null) {
                    errors.add(new ImportErrorDetail(displayRow, empCode, "Ngày làm việc", "Ngày làm việc không hợp lệ (định dạng chuẩn: YYYY-MM-DD)"));
                    continue;
                }

                Optional<Employee> empOpt = employeeRepository.findByEmployeeCode(empCode);
                if (empOpt.isEmpty()) {
                    errors.add(new ImportErrorDetail(displayRow, empCode, "Mã NV", "Không tìm thấy nhân viên có mã '" + empCode + "'"));
                    continue;
                }

                Employee employee = empOpt.get();

                LocalDateTime checkIn = checkInTime != null ? LocalDateTime.of(workDate, checkInTime) : null;
                LocalDateTime checkOut = checkOutTime != null ? LocalDateTime.of(workDate, checkOutTime) : null;

                BigDecimal totalWorkHours = BigDecimal.ZERO;
                if (checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
                    long minutes = Duration.between(checkIn, checkOut).toMinutes();
                    totalWorkHours = BigDecimal.valueOf(minutes / 60.0).setScale(2, RoundingMode.HALF_UP);
                }

                // Determine Attendance Status
                AttendanceStatus status = AttendanceStatus.PRESENT;
                boolean isLate = checkInTime != null && checkInTime.isAfter(WORK_START_TIME);
                boolean isEarlyLeave = checkOutTime != null && checkOutTime.isBefore(WORK_END_TIME);

                if (isLate && isEarlyLeave) status = AttendanceStatus.LATE_AND_EARLY_LEAVE;
                else if (isLate) status = AttendanceStatus.LATE;
                else if (isEarlyLeave) status = AttendanceStatus.EARLY_LEAVE;
                else if (checkInTime == null && checkOutTime == null) status = AttendanceStatus.ABSENT;

                // Check existing record
                Optional<Attendance> existing = attendanceRepository.findByEmployeeIdAndWorkDate(employee.getId(), workDate);
                Attendance attendance;
                if (existing.isPresent()) {
                    attendance = existing.get();
                    attendance.setCheckIn(checkIn);
                    attendance.setCheckOut(checkOut);
                    attendance.setTotalWorkHours(totalWorkHours);
                    attendance.setStatus(status);
                    if (!notes.isEmpty()) attendance.setNotes(notes);
                } else {
                    attendance = Attendance.builder()
                            .employee(employee)
                            .workDate(workDate)
                            .checkIn(checkIn)
                            .checkOut(checkOut)
                            .totalWorkHours(totalWorkHours)
                            .status(status)
                            .notes(notes.isEmpty() ? "Nhập từ file máy chấm công" : notes)
                            .build();
                }

                attendanceRepository.save(attendance);
                successCount++;
            }
        } catch (Exception e) {
            log.error("Failed to parse Attendance Excel file", e);
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }

        return ImportResultResponse.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .failedCount(errors.size())
                .errors(errors)
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }
    }
}

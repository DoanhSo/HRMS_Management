package com.ng_doanh.hr_management_system.attendance.service.impl;

import com.ng_doanh.hr_management_system.attendance.dto.request.AttendanceManualRequest;
import com.ng_doanh.hr_management_system.attendance.dto.request.CheckInRequest;
import com.ng_doanh.hr_management_system.attendance.dto.request.CheckOutRequest;
import com.ng_doanh.hr_management_system.attendance.dto.response.AttendanceResponse;
import com.ng_doanh.hr_management_system.attendance.entity.Attendance;
import com.ng_doanh.hr_management_system.attendance.enums.AttendanceStatus;
import com.ng_doanh.hr_management_system.attendance.mapper.AttendanceMapper;
import com.ng_doanh.hr_management_system.attendance.repository.AttendanceRepository;
import com.ng_doanh.hr_management_system.attendance.service.AttendanceService;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceMapper attendanceMapper;

    private static final LocalTime STANDARD_START_TIME = LocalTime.of(8, 0);
    private static final LocalTime STANDARD_END_TIME = LocalTime.of(17, 0);

    @Override
    @Transactional
    public AttendanceResponse checkIn(Long userId, CheckInRequest request) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        if (attendanceRepository.findByEmployeeIdAndWorkDate(employee.getId(), today).isPresent()) {
            throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
        }

        AttendanceStatus status = now.toLocalTime().isAfter(STANDARD_START_TIME)
                ? AttendanceStatus.LATE
                : AttendanceStatus.PRESENT;

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .workDate(today)
                .checkIn(now)
                .status(status)
                .notes(request != null ? request.getNotes() : null)
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Check-in successful for employee: {} at {}", employee.getEmployeeCode(), now);

        return attendanceMapper.toResponse(savedAttendance);
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(Long userId, CheckOutRequest request) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employee.getId(), today)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        if (attendance.getCheckOut() != null) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }

        attendance.setCheckOut(now);

        // Calculate hours worked
        Duration duration = Duration.between(attendance.getCheckIn(), now);
        double hours = duration.toMinutes() / 60.0;
        if (hours > 4.0) {
            hours -= 1.0; // Deduct 1 hour for lunch break
        }
        attendance.setTotalWorkHours(BigDecimal.valueOf(Math.max(0, hours)).setScale(2, RoundingMode.HALF_UP));

        // Determine final status
        boolean isEarlyLeave = now.toLocalTime().isBefore(STANDARD_END_TIME);
        if (isEarlyLeave) {
            if (attendance.getStatus() == AttendanceStatus.LATE) {
                attendance.setStatus(AttendanceStatus.LATE_AND_EARLY_LEAVE);
            } else {
                attendance.setStatus(AttendanceStatus.EARLY_LEAVE);
            }
        }

        if (request != null && request.getNotes() != null) {
            attendance.setNotes(attendance.getNotes() != null ? attendance.getNotes() + " | " + request.getNotes() : request.getNotes());
        }

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Check-out successful for employee: {} at {}", employee.getEmployeeCode(), now);

        return attendanceMapper.toResponse(savedAttendance);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getMyAttendanceHistory(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        Page<Attendance> attendancePage = attendanceRepository.findByEmployeeIdAndWorkDateBetween(employee.getId(), start, end, pageable);
        return attendancePage.map(attendanceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> searchAttendances(
            String keyword,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            AttendanceStatus status,
            Pageable pageable
    ) {
        Page<Attendance> attendancePage = attendanceRepository.searchAttendances(keyword, departmentId, startDate, endDate, status, pageable);
        return attendancePage.map(attendanceMapper::toResponse);
    }

    @Override
    @Transactional
    public AttendanceResponse manualAttendanceEntry(AttendanceManualRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employee.getId(), request.getWorkDate())
                .orElseGet(() -> Attendance.builder()
                        .employee(employee)
                        .workDate(request.getWorkDate())
                        .build());

        if (request.getCheckIn() != null) {
            attendance.setCheckIn(request.getCheckIn());
        }
        if (request.getCheckOut() != null) {
            attendance.setCheckOut(request.getCheckOut());
        }
        if (request.getStatus() != null) {
            attendance.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            attendance.setNotes(request.getNotes());
        }

        if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
            Duration duration = Duration.between(attendance.getCheckIn(), attendance.getCheckOut());
            double hours = duration.toMinutes() / 60.0;
            if (hours > 4.0) {
                hours -= 1.0;
            }
            attendance.setTotalWorkHours(BigDecimal.valueOf(Math.max(0, hours)).setScale(2, RoundingMode.HALF_UP));
        }

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Manual attendance entry created/updated for employee: {} on {}", employee.getEmployeeCode(), request.getWorkDate());

        return attendanceMapper.toResponse(savedAttendance);
    }
}

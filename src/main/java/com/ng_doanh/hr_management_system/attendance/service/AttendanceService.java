package com.ng_doanh.hr_management_system.attendance.service;

import com.ng_doanh.hr_management_system.attendance.dto.request.AttendanceManualRequest;
import com.ng_doanh.hr_management_system.attendance.dto.request.CheckInRequest;
import com.ng_doanh.hr_management_system.attendance.dto.request.CheckOutRequest;
import com.ng_doanh.hr_management_system.attendance.dto.response.AttendanceResponse;
import com.ng_doanh.hr_management_system.attendance.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AttendanceService {

    AttendanceResponse checkIn(Long userId, CheckInRequest request);

    AttendanceResponse checkOut(Long userId, CheckOutRequest request);

    Page<AttendanceResponse> getMyAttendanceHistory(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<AttendanceResponse> searchAttendances(
            String keyword,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            AttendanceStatus status,
            Pageable pageable
    );

    AttendanceResponse manualAttendanceEntry(AttendanceManualRequest request);
}

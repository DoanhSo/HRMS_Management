package com.ng_doanh.hr_management_system.attendance.service;

import com.ng_doanh.hr_management_system.attendance.dto.request.AttendanceManualRequest;
import com.ng_doanh.hr_management_system.attendance.dto.request.CheckInRequest;
import com.ng_doanh.hr_management_system.attendance.dto.request.CheckOutRequest;
import com.ng_doanh.hr_management_system.attendance.dto.response.AttendanceResponse;
import com.ng_doanh.hr_management_system.attendance.entity.Attendance;
import com.ng_doanh.hr_management_system.attendance.enums.AttendanceStatus;
import com.ng_doanh.hr_management_system.attendance.mapper.AttendanceMapper;
import com.ng_doanh.hr_management_system.attendance.repository.AttendanceRepository;
import com.ng_doanh.hr_management_system.attendance.service.impl.AttendanceServiceImpl;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceService Unit Tests")
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendanceMapper attendanceMapper;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Employee employee;
    private Attendance attendance;
    private AttendanceResponse attendanceResponse;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .employeeCode("EMP-00001")
                .firstName("Nguyen")
                .lastName("Van A")
                .build();
        employee.setId(1L);

        attendance = Attendance.builder()
                .employee(employee)
                .workDate(LocalDate.now())
                .checkIn(LocalDateTime.now().minusHours(8))
                .status(AttendanceStatus.PRESENT)
                .build();
        attendance.setId(1L);

        attendanceResponse = AttendanceResponse.builder()
                .id(1L)
                .employeeId(1L)
                .employeeCode("EMP-00001")
                .workDate(LocalDate.now())
                .status(AttendanceStatus.PRESENT)
                .build();
    }

    // ===================================================
    // CHECK-IN TESTS
    // ===================================================

    @Test
    @DisplayName("Check-in successfully when not already checked in today")
    void checkIn_Success() {
        CheckInRequest request = new CheckInRequest();
        request.setNotes("On-time check in");

        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        when(attendanceMapper.toResponse(attendance)).thenReturn(attendanceResponse);

        AttendanceResponse result = attendanceService.checkIn(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getEmployeeCode()).isEqualTo("EMP-00001");
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Check-in throws duplicate exception when already checked in today")
    void checkIn_Duplicate_ThrowsException() {
        CheckInRequest request = new CheckInRequest();

        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(attendance));

        assertThatThrownBy(() -> attendanceService.checkIn(1L, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Check-in throws RESOURCE_NOT_FOUND when employee not found for userId")
    void checkIn_EmployeeNotFound_ThrowsException() {
        CheckInRequest request = new CheckInRequest();

        when(employeeRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.checkIn(99L, request))
                .isInstanceOf(BusinessException.class);

        verify(attendanceRepository, never()).save(any());
    }

    // ===================================================
    // CHECK-OUT TESTS
    // ===================================================

    @Test
    @DisplayName("Check-out successfully calculates total work hours")
    void checkOut_Success() {
        CheckOutRequest request = new CheckOutRequest();
        request.setNotes("Finished work");

        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        when(attendanceMapper.toResponse(attendance)).thenReturn(attendanceResponse);

        AttendanceResponse result = attendanceService.checkOut(1L, request);

        assertThat(result).isNotNull();
        assertThat(attendance.getCheckOut()).isNotNull();
        assertThat(attendance.getTotalWorkHours()).isNotNull();
        verify(attendanceRepository).save(attendance);
    }

    @Test
    @DisplayName("Check-out throws RESOURCE_NOT_FOUND when no check-in record exists for today")
    void checkOut_NoCheckIn_ThrowsException() {
        CheckOutRequest request = new CheckOutRequest();

        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.checkOut(1L, request))
                .isInstanceOf(BusinessException.class);

        verify(attendanceRepository, never()).save(any());
    }

    // ===================================================
    // GET MY ATTENDANCE HISTORY TESTS
    // ===================================================

    @Test
    @DisplayName("Get my attendance history returns paginated results filtered by date range")
    void getMyAttendanceHistory_ReturnsPagedResult() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        Page<Attendance> attendancePage = new PageImpl<>(List.of(attendance));

        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndWorkDateBetween(eq(1L), eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(attendancePage);
        when(attendanceMapper.toResponse(attendance)).thenReturn(attendanceResponse);

        Page<AttendanceResponse> result = attendanceService.getMyAttendanceHistory(1L, startDate, endDate, Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmployeeCode()).isEqualTo("EMP-00001");
    }

    @Test
    @DisplayName("Get my attendance history with null dates defaults to last 30 days")
    void getMyAttendanceHistory_NullDates_DefaultsToLast30Days() {
        Page<Attendance> attendancePage = new PageImpl<>(List.of(attendance));

        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndWorkDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(attendancePage);
        when(attendanceMapper.toResponse(attendance)).thenReturn(attendanceResponse);

        Page<AttendanceResponse> result = attendanceService.getMyAttendanceHistory(1L, null, null, Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    // ===================================================
    // SEARCH ATTENDANCES TESTS
    // ===================================================

    @Test
    @DisplayName("Search attendances filters by department and LATE status returns paginated results")
    void searchAttendances_FilterByDepartmentAndStatus() {
        Page<Attendance> attendancePage = new PageImpl<>(List.of(attendance));

        when(attendanceRepository.searchAttendances(
                any(), eq(2L), any(LocalDate.class), any(LocalDate.class),
                eq(AttendanceStatus.LATE), any(Pageable.class)))
                .thenReturn(attendancePage);
        when(attendanceMapper.toResponse(attendance)).thenReturn(attendanceResponse);

        Page<AttendanceResponse> result = attendanceService.searchAttendances(
                null, 2L, LocalDate.now().minusDays(1), LocalDate.now(),
                AttendanceStatus.LATE, Pageable.unpaged()
        );

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Search attendances with no filters returns all records paginated")
    void searchAttendances_NoFilters_ReturnsAllPaged() {
        Attendance lateAttendance = Attendance.builder()
                .employee(employee)
                .workDate(LocalDate.now().minusDays(1))
                .status(AttendanceStatus.LATE)
                .build();
        Page<Attendance> attendancePage = new PageImpl<>(List.of(attendance, lateAttendance));

        when(attendanceRepository.searchAttendances(
                isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(attendancePage);
        when(attendanceMapper.toResponse(any())).thenReturn(attendanceResponse);

        Page<AttendanceResponse> result = attendanceService.searchAttendances(
                null, null, null, null, null, Pageable.unpaged()
        );

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    // ===================================================
    // MANUAL ATTENDANCE ENTRY TESTS
    // ===================================================

    @Test
    @DisplayName("Manual attendance entry creates new record with custom check-in/check-out times")
    void manualAttendanceEntry_Success() {
        LocalDateTime checkIn = LocalDateTime.now().withHour(8).withMinute(0);
        LocalDateTime checkOut = LocalDateTime.now().withHour(17).withMinute(30);

        AttendanceManualRequest request = AttendanceManualRequest.builder()
                .employeeId(1L)
                .workDate(LocalDate.now())
                .checkIn(checkIn)
                .checkOut(checkOut)
                .status(AttendanceStatus.PRESENT)
                .notes("Manual entry by HR")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(1L), eq(LocalDate.now())))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        when(attendanceMapper.toResponse(attendance)).thenReturn(
                AttendanceResponse.builder()
                        .id(1L)
                        .employeeCode("EMP-00001")
                        .totalWorkHours(BigDecimal.valueOf(8.5))
                        .status(AttendanceStatus.PRESENT)
                        .build()
        );

        AttendanceResponse result = attendanceService.manualAttendanceEntry(request);

        assertThat(result).isNotNull();
        assertThat(result.getEmployeeCode()).isEqualTo("EMP-00001");
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Manual attendance entry updates existing record without overriding missing fields")
    void manualAttendanceEntry_UpdatesExistingRecord() {
        AttendanceManualRequest request = AttendanceManualRequest.builder()
                .employeeId(1L)
                .workDate(LocalDate.now())
                .status(AttendanceStatus.ON_LEAVE)
                .notes("Updated to ON_LEAVE")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(1L), eq(LocalDate.now())))
                .thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        when(attendanceMapper.toResponse(attendance)).thenReturn(attendanceResponse);

        AttendanceResponse result = attendanceService.manualAttendanceEntry(request);

        assertThat(result).isNotNull();
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.ON_LEAVE);
        verify(attendanceRepository).save(attendance);
    }

    @Test
    @DisplayName("Manual attendance entry for non-existent employee throws RESOURCE_NOT_FOUND")
    void manualAttendanceEntry_EmployeeNotFound_ThrowsException() {
        AttendanceManualRequest request = AttendanceManualRequest.builder()
                .employeeId(999L)
                .workDate(LocalDate.now())
                .build();

        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.manualAttendanceEntry(request))
                .isInstanceOf(BusinessException.class);

        verify(attendanceRepository, never()).save(any());
    }
}

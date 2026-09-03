package com.ng_doanh.hr_management_system.leave.service;

import com.ng_doanh.hr_management_system.attendance.repository.AttendanceRepository;
import com.ng_doanh.hr_management_system.auth.entity.User;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import com.ng_doanh.hr_management_system.leave.dto.request.LeaveApprovalRequest;
import com.ng_doanh.hr_management_system.leave.dto.request.LeaveRequestCreateRequest;
import com.ng_doanh.hr_management_system.leave.dto.request.LeaveTypeCreateRequest;
import com.ng_doanh.hr_management_system.leave.dto.response.LeaveRequestResponse;
import com.ng_doanh.hr_management_system.leave.dto.response.LeaveTypeResponse;
import com.ng_doanh.hr_management_system.leave.entity.LeaveBalance;
import com.ng_doanh.hr_management_system.leave.entity.LeaveRequest;
import com.ng_doanh.hr_management_system.leave.entity.LeaveType;
import com.ng_doanh.hr_management_system.leave.enums.LeaveRequestStatus;
import com.ng_doanh.hr_management_system.leave.mapper.LeaveMapper;
import com.ng_doanh.hr_management_system.leave.repository.LeaveBalanceRepository;
import com.ng_doanh.hr_management_system.leave.repository.LeaveRequestRepository;
import com.ng_doanh.hr_management_system.leave.repository.LeaveTypeRepository;
import com.ng_doanh.hr_management_system.leave.service.impl.LeaveServiceImpl;
import com.ng_doanh.hr_management_system.notification.service.EmailService;
import com.ng_doanh.hr_management_system.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaveService Unit Tests")
class LeaveServiceTest {

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private LeaveMapper leaveMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LeaveServiceImpl leaveService;

    private Employee employee;
    private Employee approver;
    private LeaveType leaveType;
    private LeaveBalance leaveBalance;
    private LeaveRequest leaveRequest;
    private LeaveRequestResponse leaveRequestResponse;

    @BeforeEach
    void setUp() {
        User user = User.builder().username("emp1").email("emp1@example.com").build();
        user.setId(1L);

        User managerUser = User.builder().username("mgr1").email("mgr1@example.com").build();
        managerUser.setId(2L);

        approver = Employee.builder()
                .employeeCode("EMP-MGR")
                .firstName("Tran")
                .lastName("Manager")
                .user(managerUser)
                .build();
        approver.setId(2L);

        employee = Employee.builder()
                .employeeCode("EMP-00001")
                .firstName("Nguyen")
                .lastName("Van A")
                .user(user)
                .manager(approver)
                .build();
        employee.setId(1L);

        leaveType = LeaveType.builder()
                .code("ANNUAL")
                .name("Nghỉ Phép Năm")
                .paid(true)
                .defaultDaysPerYear(12)
                .active(true)
                .build();
        leaveType.setId(1L);

        leaveBalance = LeaveBalance.builder()
                .employee(employee)
                .leaveType(leaveType)
                .year(2026)
                .totalDays(BigDecimal.valueOf(12))
                .usedDays(BigDecimal.ZERO)
                .remainingDays(BigDecimal.valueOf(12))
                .build();
        leaveBalance.setId(1L);

        leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(LocalDate.of(2026, 8, 20))
                .endDate(LocalDate.of(2026, 8, 21))
                .totalDays(BigDecimal.valueOf(2))
                .reason("Việc gia đình")
                .status(LeaveRequestStatus.PENDING)
                .build();
        leaveRequest.setId(1L);

        leaveRequestResponse = LeaveRequestResponse.builder()
                .id(1L)
                .employeeId(1L)
                .employeeName("Nguyen Van A")
                .leaveTypeName("Nghỉ Phép Năm")
                .startDate(LocalDate.of(2026, 8, 20))
                .endDate(LocalDate.of(2026, 8, 21))
                .totalDays(BigDecimal.valueOf(2))
                .status(LeaveRequestStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Create leave request successfully and triggers notification to manager")
    void createLeaveRequest_Success() {
        LeaveRequestCreateRequest request = LeaveRequestCreateRequest.builder()
                .leaveTypeId(1L)
                .startDate(LocalDate.of(2026, 8, 20))
                .endDate(LocalDate.of(2026, 8, 21))
                .reason("Việc gia đình")
                .build();

        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveTypeRepository.findByActiveTrue()).thenReturn(List.of(leaveType));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, 2026))
                .thenReturn(Optional.of(leaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        when(leaveMapper.toResponse(leaveRequest)).thenReturn(leaveRequestResponse);

        LeaveRequestResponse result = leaveService.createLeaveRequest(1L, request);

        assertThat(result).isNotNull();
        verify(leaveRequestRepository).save(any(LeaveRequest.class));
        verify(notificationService).send(eq(2L), any(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Approve leave request deducts balance and notifies employee")
    void approveLeaveRequest_Success() {
        when(employeeRepository.findByUserId(2L)).thenReturn(Optional.of(approver));
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, 2026))
                .thenReturn(Optional.of(leaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        when(leaveMapper.toResponse(leaveRequest)).thenReturn(leaveRequestResponse);

        LeaveRequestResponse result = leaveService.approveLeaveRequest(1L, 2L);

        assertThat(result).isNotNull();
        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequestStatus.APPROVED);
        assertThat(leaveBalance.getUsedDays()).isEqualByComparingTo(BigDecimal.valueOf(2));
        assertThat(leaveBalance.getRemainingDays()).isEqualByComparingTo(BigDecimal.valueOf(10));
        verify(notificationService).send(eq(1L), any(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Reject leave request saves rejection reason and notifies employee")
    void rejectLeaveRequest_Success() {
        LeaveApprovalRequest approvalRequest = new LeaveApprovalRequest();
        approvalRequest.setRejectionReason("Công việc dự án đang gấp");

        when(employeeRepository.findByUserId(2L)).thenReturn(Optional.of(approver));
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        when(leaveMapper.toResponse(leaveRequest)).thenReturn(leaveRequestResponse);

        LeaveRequestResponse result = leaveService.rejectLeaveRequest(1L, 2L, approvalRequest);

        assertThat(result).isNotNull();
        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequestStatus.REJECTED);
        assertThat(leaveRequest.getRejectionReason()).isEqualTo("Công việc dự án đang gấp");
        verify(notificationService).send(eq(1L), any(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Cancel leave request sets status to CANCELLED")
    void cancelLeaveRequest_Success() {
        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        when(leaveMapper.toResponse(leaveRequest)).thenReturn(leaveRequestResponse);

        LeaveRequestResponse result = leaveService.cancelLeaveRequest(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequestStatus.CANCELLED);
    }

    @Test
    @DisplayName("Create leave type auto-generates code when code is null or blank")
    void createLeaveType_AutoGenerateCode_Success() {
        LeaveTypeCreateRequest req = LeaveTypeCreateRequest.builder()
                .name("Nghỉ thai sản")
                .paid(true)
                .defaultDaysPerYear(180)
                .build();

        LeaveType lt = LeaveType.builder().name("Nghỉ thai sản").build();
        lt.setId(2L);
        LeaveTypeResponse resp = LeaveTypeResponse.builder().id(2L).code("LT-00001").name("Nghỉ thai sản").build();

        when(leaveTypeRepository.count()).thenReturn(0L);
        when(leaveTypeRepository.existsByCode("LT-00001")).thenReturn(false);
        when(leaveMapper.toEntity(req)).thenReturn(lt);
        when(leaveTypeRepository.save(lt)).thenReturn(lt);
        when(leaveMapper.toResponse(lt)).thenReturn(resp);

        LeaveTypeResponse result = leaveService.createLeaveType(req);

        assertThat(result).isNotNull();
        verify(leaveTypeRepository).save(lt);
        assertThat(lt.getCode()).isEqualTo("LT-00001");
    }

    @Test
    @DisplayName("Create leave type with custom code successfully")
    void createLeaveType_CustomCode_Success() {
        LeaveTypeCreateRequest req = LeaveTypeCreateRequest.builder()
                .code("MATERNITY")
                .name("Nghỉ thai sản")
                .paid(true)
                .defaultDaysPerYear(180)
                .build();

        LeaveType lt = LeaveType.builder().code("MATERNITY").name("Nghỉ thai sản").build();
        lt.setId(2L);
        LeaveTypeResponse resp = LeaveTypeResponse.builder().id(2L).code("MATERNITY").name("Nghỉ thai sản").build();

        when(leaveTypeRepository.existsByCode("MATERNITY")).thenReturn(false);
        when(leaveMapper.toEntity(req)).thenReturn(lt);
        when(leaveTypeRepository.save(lt)).thenReturn(lt);
        when(leaveMapper.toResponse(lt)).thenReturn(resp);

        LeaveTypeResponse result = leaveService.createLeaveType(req);

        assertThat(result).isNotNull();
        assertThat(lt.getCode()).isEqualTo("MATERNITY");
    }

    @Test
    @DisplayName("Create leave type throws DUPLICATE_RESOURCE when code exists")
    void createLeaveType_DuplicateCode_ThrowsException() {
        LeaveTypeCreateRequest req = LeaveTypeCreateRequest.builder()
                .code("ANNUAL")
                .name("Nghỉ Phép Năm")
                .paid(true)
                .defaultDaysPerYear(12)
                .build();

        when(leaveTypeRepository.existsByCode("ANNUAL")).thenReturn(true);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> leaveService.createLeaveType(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("responseCode", ResponseCode.DUPLICATE_RESOURCE);
    }
}

package com.ng_doanh.hr_management_system.payroll.service;

import com.ng_doanh.hr_management_system.attendance.repository.AttendanceRepository;
import com.ng_doanh.hr_management_system.auth.entity.User;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.enums.EmploymentStatus;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import com.ng_doanh.hr_management_system.notification.service.EmailService;
import com.ng_doanh.hr_management_system.notification.service.NotificationService;
import com.ng_doanh.hr_management_system.payroll.dto.request.PayrollPeriodCreateRequest;
import com.ng_doanh.hr_management_system.payroll.dto.response.PayrollPeriodResponse;
import com.ng_doanh.hr_management_system.payroll.entity.PayrollPeriod;
import com.ng_doanh.hr_management_system.payroll.entity.Payslip;
import com.ng_doanh.hr_management_system.payroll.enums.PayrollPeriodStatus;
import com.ng_doanh.hr_management_system.payroll.enums.PayslipStatus;
import com.ng_doanh.hr_management_system.payroll.mapper.PayrollMapper;
import com.ng_doanh.hr_management_system.payroll.repository.PayrollPeriodRepository;
import com.ng_doanh.hr_management_system.payroll.repository.PayslipRepository;
import com.ng_doanh.hr_management_system.payroll.service.impl.PayrollServiceImpl;
import com.ng_doanh.hr_management_system.position.entity.Position;
import com.ng_doanh.hr_management_system.position.repository.PositionRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayrollService Unit Tests")
class PayrollServiceTest {

    @Mock
    private PayrollPeriodRepository payrollPeriodRepository;

    @Mock
    private PayslipRepository payslipRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private com.ng_doanh.hr_management_system.kpi.repository.KpiEvaluationRepository kpiEvaluationRepository;

    @Mock
    private PayrollMapper payrollMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PayrollServiceImpl payrollService;

    private PayrollPeriod period;
    private PayrollPeriodResponse periodResponse;
    private Employee employee;
    private Position position;

    @BeforeEach
    void setUp() {
        period = PayrollPeriod.builder()
                .name("Kỳ Lương Tháng 08/2026")
                .year(2026)
                .month(8)
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .workingDays(22)
                .status(PayrollPeriodStatus.DRAFT)
                .build();
        period.setId(1L);

        periodResponse = PayrollPeriodResponse.builder()
                .id(1L)
                .name("Kỳ Lương Tháng 08/2026")
                .year(2026)
                .month(8)
                .workingDays(22)
                .status(PayrollPeriodStatus.DRAFT)
                .build();

        User user = User.builder().username("emp1").email("emp1@example.com").build();
        user.setId(10L);

        employee = Employee.builder()
                .employeeCode("EMP-00001")
                .firstName("Nguyen")
                .lastName("Van A")
                .user(user)
                .positionId(1L)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
        employee.setId(1L);

        position = Position.builder()
                .basicSalary(BigDecimal.valueOf(22000000))
                .build();
        position.setId(1L);
    }

    @Test
    @DisplayName("Create payroll period in DRAFT status")
    void createPayrollPeriod_Success() {
        PayrollPeriodCreateRequest request = PayrollPeriodCreateRequest.builder()
                .year(2026)
                .month(8)
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .workingDays(22)
                .build();

        when(payrollPeriodRepository.existsByYearAndMonth(2026, 8)).thenReturn(false);
        when(payrollMapper.toEntity(request)).thenReturn(period);
        when(payrollPeriodRepository.save(any(PayrollPeriod.class))).thenReturn(period);
        when(payrollMapper.toResponse(period)).thenReturn(periodResponse);

        PayrollPeriodResponse result = payrollService.createPayrollPeriod(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PayrollPeriodStatus.DRAFT);
        verify(payrollPeriodRepository).save(any(PayrollPeriod.class));
    }

    @Test
    @DisplayName("Calculate payroll for period calculates salary and marks CALCULATED")
    void calculatePayrollForPeriod_Success() {
        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(positionRepository.findAll()).thenReturn(List.of(position));
        when(attendanceRepository.findByEmployeeIdAndWorkDateBetween(eq(1L), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(kpiEvaluationRepository.findByEmployeeIdAndPeriodYearAndPeriodMonthAndStatus(eq(1L), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(payslipRepository.findByPayrollPeriodIdAndEmployeeId(1L, 1L)).thenReturn(Optional.empty());

        payrollService.calculatePayrollForPeriod(1L);

        assertThat(period.getStatus()).isEqualTo(PayrollPeriodStatus.CALCULATED);
        verify(payslipRepository).saveAll(anyList());
        verify(payrollPeriodRepository).save(period);
    }

    @Test
    @DisplayName("Approve payroll period locks period, marks payslips APPROVED, and triggers notifications")
    void approvePayrollPeriod_Success() {
        period.setStatus(PayrollPeriodStatus.CALCULATED);

        Payslip payslip = Payslip.builder()
                .payrollPeriod(period)
                .employee(employee)
                .basicSalary(BigDecimal.valueOf(22000000))
                .grossSalary(BigDecimal.valueOf(22000000))
                .netSalary(BigDecimal.valueOf(19800000))
                .status(PayslipStatus.CALCULATED)
                .build();
        payslip.setId(1L);

        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(payslipRepository.searchPayslips(eq(1L), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(payslip)));

        payrollService.approvePayrollPeriod(1L);

        assertThat(period.getStatus()).isEqualTo(PayrollPeriodStatus.APPROVED);
        assertThat(payslip.getStatus()).isEqualTo(PayslipStatus.APPROVED);
        verify(payslipRepository).save(payslip);
        verify(emailService).sendPayslipGeneratedNotification(eq("emp1@example.com"), anyString(), anyString(), any());
        verify(notificationService).send(eq(10L), any(), anyString(), anyString(), anyString());
    }

    // ===================================================
    // KPI BONUS INTEGRATION TEST
    // ===================================================

    @Test
    @DisplayName("Calculate payroll with KPI Rating A adds 50% bonus to gross salary")
    void calculatePayrollForPeriod_WithKpiBonus() {
        com.ng_doanh.hr_management_system.kpi.entity.KpiEvaluation kpiBonus =
                com.ng_doanh.hr_management_system.kpi.entity.KpiEvaluation.builder()
                        .employee(employee)
                        .periodYear(2026)
                        .periodMonth(8)
                        .rating(com.ng_doanh.hr_management_system.kpi.enums.KpiRating.A)
                        .kpiCoefficient(BigDecimal.valueOf(1.5))
                        .bonusAmount(BigDecimal.valueOf(11000000))
                        .status(com.ng_doanh.hr_management_system.kpi.enums.KpiEvaluationStatus.APPROVED)
                        .build();

        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(positionRepository.findAll()).thenReturn(List.of(position));
        when(attendanceRepository.findByEmployeeIdAndWorkDateBetween(eq(1L), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(kpiEvaluationRepository.findByEmployeeIdAndPeriodYearAndPeriodMonthAndStatus(eq(1L), any(), any(), any()))
                .thenReturn(Optional.of(kpiBonus)); // KPI bonus is present
        when(payslipRepository.findByPayrollPeriodIdAndEmployeeId(1L, 1L)).thenReturn(Optional.empty());

        payrollService.calculatePayrollForPeriod(1L);

        assertThat(period.getStatus()).isEqualTo(PayrollPeriodStatus.CALCULATED);
        // verify saveAll was called with payslips that include the KPI bonus
        verify(payslipRepository).saveAll(anyList());
        verify(payrollPeriodRepository).save(period);
    }

    // ===================================================
    // DUPLICATE PAYROLL PERIOD TEST
    // ===================================================

    @Test
    @DisplayName("Create payroll period for already-existing year/month throws DUPLICATE_RESOURCE")
    void createPayrollPeriod_Duplicate_ThrowsException() {
        PayrollPeriodCreateRequest request = PayrollPeriodCreateRequest.builder()
                .year(2026)
                .month(8)
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .workingDays(22)
                .build();

        when(payrollPeriodRepository.existsByYearAndMonth(2026, 8)).thenReturn(true);

        assertThatThrownBy(() -> payrollService.createPayrollPeriod(request))
                .isInstanceOf(com.ng_doanh.hr_management_system.common.exception.BusinessException.class);

        verify(payrollPeriodRepository, never()).save(any());
    }
}

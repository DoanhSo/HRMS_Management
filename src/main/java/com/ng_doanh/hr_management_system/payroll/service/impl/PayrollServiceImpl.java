package com.ng_doanh.hr_management_system.payroll.service.impl;

import com.ng_doanh.hr_management_system.attendance.entity.Attendance;
import com.ng_doanh.hr_management_system.attendance.enums.AttendanceStatus;
import com.ng_doanh.hr_management_system.attendance.repository.AttendanceRepository;
import com.ng_doanh.hr_management_system.audit.annotation.Audited;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.enums.EmploymentStatus;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import com.ng_doanh.hr_management_system.notification.service.EmailService;
import com.ng_doanh.hr_management_system.payroll.dto.request.PayrollPeriodCreateRequest;
import com.ng_doanh.hr_management_system.payroll.dto.response.PayrollPeriodResponse;
import com.ng_doanh.hr_management_system.payroll.dto.response.PayslipResponse;
import com.ng_doanh.hr_management_system.payroll.entity.PayrollPeriod;
import com.ng_doanh.hr_management_system.payroll.entity.Payslip;
import com.ng_doanh.hr_management_system.payroll.enums.PayrollPeriodStatus;
import com.ng_doanh.hr_management_system.payroll.enums.PayslipStatus;
import com.ng_doanh.hr_management_system.payroll.mapper.PayrollMapper;
import com.ng_doanh.hr_management_system.payroll.repository.PayrollPeriodRepository;
import com.ng_doanh.hr_management_system.payroll.repository.PayslipRepository;
import com.ng_doanh.hr_management_system.payroll.service.PayrollService;
import com.ng_doanh.hr_management_system.position.entity.Position;
import com.ng_doanh.hr_management_system.position.repository.PositionRepository;
import com.ng_doanh.hr_management_system.kpi.entity.KpiEvaluation;
import com.ng_doanh.hr_management_system.kpi.enums.KpiEvaluationStatus;
import com.ng_doanh.hr_management_system.kpi.repository.KpiEvaluationRepository;
import com.ng_doanh.hr_management_system.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PayslipRepository payslipRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final AttendanceRepository attendanceRepository;
    private final KpiEvaluationRepository kpiEvaluationRepository;
    private final PayrollMapper payrollMapper;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    @Audited(action = "CREATE_PAYROLL_PERIOD", entity = "PayrollPeriod")
    public PayrollPeriodResponse createPayrollPeriod(PayrollPeriodCreateRequest request) {
        if (payrollPeriodRepository.existsByYearAndMonth(request.getYear(), request.getMonth())) {
            throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
        }

        String name = request.getName() != null && !request.getName().isBlank()
                ? request.getName()
                : String.format("Payroll Period %02d/%d", request.getMonth(), request.getYear());

        PayrollPeriod period = payrollMapper.toEntity(request);
        period.setName(name);
        period.setStatus(PayrollPeriodStatus.DRAFT);

        PayrollPeriod savedPeriod = payrollPeriodRepository.save(period);
        log.info("Payroll period created for {}/{}", request.getMonth(), request.getYear());

        return payrollMapper.toResponse(savedPeriod);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayrollPeriodResponse> getAllPayrollPeriods(Pageable pageable) {
        Page<PayrollPeriod> periods = payrollPeriodRepository.findAllByOrderByYearDescMonthDesc(pageable);
        return periods.map(payrollMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollPeriodResponse getPayrollPeriodById(Long periodId) {
        PayrollPeriod period = payrollPeriodRepository.findById(periodId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        return payrollMapper.toResponse(period);
    }

    @Override
    @Transactional
    @Audited(action = "CALCULATE_PAYROLL", entity = "PayrollPeriod")
    public void calculatePayrollForPeriod(Long periodId) {
        PayrollPeriod period = payrollPeriodRepository.findById(periodId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        if (period.getStatus() == PayrollPeriodStatus.APPROVED || period.getStatus() == PayrollPeriodStatus.PAID) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }

        List<Employee> employees = employeeRepository.findAll().stream()
                .filter(e -> e.getEmploymentStatus() == EmploymentStatus.ACTIVE || e.getEmploymentStatus() == EmploymentStatus.PROBATION)
                .toList();

        // 1. Preload all positions into memory Map (Eliminate N+1 Position queries)
        Map<Long, Position> positionMap = positionRepository.findAll().stream()
                .collect(Collectors.toMap(Position::getId, Function.identity(), (p1, p2) -> p1));

        List<Payslip> payslipsToSave = new ArrayList<>();

        for (Employee employee : employees) {
            BigDecimal basicSalary = BigDecimal.ZERO;
            if (employee.getPositionId() != null && positionMap.containsKey(employee.getPositionId())) {
                basicSalary = positionMap.get(employee.getPositionId()).getBasicSalary();
                if (basicSalary == null) basicSalary = BigDecimal.ZERO;
            }

            // Count actual worked days in period (excluding ABSENT)
            Page<Attendance> attendances = attendanceRepository.findByEmployeeIdAndWorkDateBetween(
                    employee.getId(), period.getStartDate(), period.getEndDate(), Pageable.unpaged()
            );

            long attendedDays = attendances.getContent().stream()
                    .filter(a -> a.getStatus() != AttendanceStatus.ABSENT)
                    .count();

            BigDecimal actualWorkDays = BigDecimal.valueOf(attendedDays);
            BigDecimal workingDays = BigDecimal.valueOf(Math.max(1, period.getWorkingDays()));

            // Formula: baseEarning = basicSalary * (actualWorkDays / workingDays)
            BigDecimal baseEarning = basicSalary.multiply(actualWorkDays)
                    .divide(workingDays, 2, RoundingMode.HALF_UP);

            // 2. Fetch approved KPI bonus for this employee in this period
            BigDecimal kpiBonus = BigDecimal.ZERO;
            Optional<KpiEvaluation> kpiEvaluationOpt = kpiEvaluationRepository
                    .findByEmployeeIdAndPeriodYearAndPeriodMonthAndStatus(
                            employee.getId(), period.getYear(), period.getMonth(), KpiEvaluationStatus.APPROVED
                    );
            if (kpiEvaluationOpt.isPresent()) {
                kpiBonus = kpiEvaluationOpt.get().getBonusAmount();
                if (kpiBonus == null) kpiBonus = BigDecimal.ZERO;
            }

            BigDecimal allowances = BigDecimal.ZERO;
            BigDecimal grossSalary = baseEarning.add(allowances).add(kpiBonus);
            BigDecimal deductions = BigDecimal.ZERO;

            // 10% PIT tax rate calculation placeholder
            BigDecimal tax = grossSalary.multiply(BigDecimal.valueOf(0.10)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal netSalary = grossSalary.subtract(tax).subtract(deductions);

            Payslip payslip = payslipRepository.findByPayrollPeriodIdAndEmployeeId(period.getId(), employee.getId())
                    .orElseGet(() -> Payslip.builder()
                            .payrollPeriod(period)
                            .employee(employee)
                            .build());

            payslip.setBasicSalary(basicSalary);
            payslip.setActualWorkDays(actualWorkDays);
            payslip.setGrossSalary(grossSalary);
            payslip.setAllowances(allowances);
            payslip.setBonus(kpiBonus);
            payslip.setDeductions(deductions);
            payslip.setTax(tax);
            payslip.setNetSalary(netSalary);
            payslip.setStatus(PayslipStatus.CALCULATED);

            payslipsToSave.add(payslip);
        }

        // 3. Batch save all payslips to minimize database roundtrips
        payslipRepository.saveAll(payslipsToSave);

        period.setStatus(PayrollPeriodStatus.CALCULATED);
        payrollPeriodRepository.save(period);
        log.info("Payroll calculation completed for period ID {} with {} employees and KPI bonus integrated", periodId, payslipsToSave.size());
    }

    @Override
    @Transactional
    @Audited(action = "APPROVE_PAYROLL_PERIOD", entity = "PayrollPeriod")
    public void approvePayrollPeriod(Long periodId) {
        PayrollPeriod period = payrollPeriodRepository.findById(periodId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        if (period.getStatus() != PayrollPeriodStatus.CALCULATED) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }

        period.setStatus(PayrollPeriodStatus.APPROVED);
        payrollPeriodRepository.save(period);

        Page<Payslip> payslips = payslipRepository.searchPayslips(periodId, null, null, Pageable.unpaged());
        for (Payslip payslip : payslips.getContent()) {
            payslip.setStatus(PayslipStatus.APPROVED);
            payslipRepository.save(payslip);

            // Send email notification to each employee
            if (payslip.getEmployee() != null && payslip.getEmployee().getUser() != null && payslip.getEmployee().getUser().getEmail() != null) {
                emailService.sendPayslipGeneratedNotification(
                        payslip.getEmployee().getUser().getEmail(),
                        payslip.getEmployee().getFirstName() + " " + payslip.getEmployee().getLastName(),
                        period.getName(),
                        payslip.getNetSalary()
                );
            }

            // Send in-app notification to each employee
            if (payslip.getEmployee() != null && payslip.getEmployee().getUser() != null) {
                notificationService.send(
                        payslip.getEmployee().getUser().getId(),
                        com.ng_doanh.hr_management_system.notification.enums.NotificationType.PAYSLIP_READY,
                        "Phiếu lương đã sẵn sàng",
                        String.format("Phiếu lương %s đã được phê duyệt. Thực lĩnh: %,.0f VNĐ",
                                period.getName(), payslip.getNetSalary()),
                        "/payroll/my-records"
                );
            }
        }

        log.info("Payroll period ID {} approved successfully and emails/notifications sent", periodId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayslipResponse> getMyPayslips(Long userId, Pageable pageable) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        Page<Payslip> payslips = payslipRepository.findByEmployeeId(employee.getId(), pageable);
        return payslips.map(payrollMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayslipResponse> searchPayslips(Long periodId, String keyword, Long departmentId, Pageable pageable) {
        Page<Payslip> payslips = payslipRepository.searchPayslips(periodId, keyword, departmentId, pageable);
        return payslips.map(payrollMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PayslipResponse getPayslipById(Long payslipId) {
        Payslip payslip = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        return payrollMapper.toResponse(payslip);
    }
}

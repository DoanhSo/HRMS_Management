package com.ng_doanh.hr_management_system.dashboard.service.impl;

import com.ng_doanh.hr_management_system.attendance.entity.Attendance;
import com.ng_doanh.hr_management_system.attendance.enums.AttendanceStatus;
import com.ng_doanh.hr_management_system.attendance.repository.AttendanceRepository;
import com.ng_doanh.hr_management_system.dashboard.dto.response.AttendanceOverviewResponse;
import com.ng_doanh.hr_management_system.dashboard.dto.response.DashboardSummaryResponse;
import com.ng_doanh.hr_management_system.dashboard.dto.response.DepartmentStatsResponse;
import com.ng_doanh.hr_management_system.dashboard.dto.response.PayrollSummaryResponse;
import com.ng_doanh.hr_management_system.dashboard.service.DashboardService;
import com.ng_doanh.hr_management_system.department.entity.Department;
import com.ng_doanh.hr_management_system.department.repository.DepartmentRepository;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.enums.EmploymentStatus;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import com.ng_doanh.hr_management_system.payroll.entity.PayrollPeriod;
import com.ng_doanh.hr_management_system.payroll.entity.Payslip;
import com.ng_doanh.hr_management_system.payroll.repository.PayrollPeriodRepository;
import com.ng_doanh.hr_management_system.payroll.repository.PayslipRepository;
import com.ng_doanh.hr_management_system.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PayslipRepository payslipRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        log.info("Fetching dashboard summary from Database");
        List<Employee> employees = employeeRepository.findAll();
        long totalEmployees = employees.size();
        long activeEmployees = employees.stream().filter(e -> e.getEmploymentStatus() == EmploymentStatus.ACTIVE).count();
        long probationEmployees = employees.stream().filter(e -> e.getEmploymentStatus() == EmploymentStatus.PROBATION).count();

        long totalDepartments = departmentRepository.count();
        long totalPositions = positionRepository.count();

        BigDecimal latestMonthlyPayrollCost = BigDecimal.ZERO;
        Page<PayrollPeriod> periodPage = payrollPeriodRepository.findAllByOrderByYearDescMonthDesc(Pageable.ofSize(1));
        if (!periodPage.getContent().isEmpty()) {
            PayrollPeriod latestPeriod = periodPage.getContent().get(0);
            Page<Payslip> payslips = payslipRepository.searchPayslips(latestPeriod.getId(), null, null, Pageable.unpaged());
            latestMonthlyPayrollCost = payslips.getContent().stream()
                    .map(Payslip::getNetSalary)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return DashboardSummaryResponse.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .probationEmployees(probationEmployees)
                .totalDepartments(totalDepartments)
                .totalPositions(totalPositions)
                .latestMonthlyPayrollCost(latestMonthlyPayrollCost)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceOverviewResponse getAttendanceOverview(LocalDate date) {
        log.info("Fetching attendance overview for date {} from Database", date);
        LocalDate targetDate = date != null ? date : LocalDate.now();

        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getEmploymentStatus() == EmploymentStatus.ACTIVE || e.getEmploymentStatus() == EmploymentStatus.PROBATION)
                .toList();

        long totalActive = activeEmployees.size();

        Page<Attendance> attendancesPage = attendanceRepository.searchAttendances(null, null, targetDate, targetDate, null, Pageable.unpaged());
        List<Attendance> attendances = attendancesPage.getContent();

        long presentCount = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long lateCount = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        long earlyLeaveCount = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.EARLY_LEAVE || a.getStatus() == AttendanceStatus.LATE_AND_EARLY_LEAVE).count();
        long onLeaveCount = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ON_LEAVE).count();

        long totalAttended = presentCount + lateCount + earlyLeaveCount;
        long absentCount = Math.max(0, totalActive - (totalAttended + onLeaveCount));

        double rate = totalActive > 0 ? ((double) totalAttended / totalActive) * 100.0 : 0.0;
        BigDecimal roundedRate = BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP);

        return AttendanceOverviewResponse.builder()
                .date(targetDate)
                .totalActiveEmployees(totalActive)
                .presentCount(presentCount)
                .lateCount(lateCount)
                .earlyLeaveCount(earlyLeaveCount)
                .onLeaveCount(onLeaveCount)
                .absentCount(absentCount)
                .attendanceRatePercentage(roundedRate.doubleValue())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentStatsResponse> getDepartmentStats() {
        log.info("Fetching department stats from Database");
        List<Department> departments = departmentRepository.findByActiveTrue();
        List<DepartmentStatsResponse> result = new ArrayList<>();

        for (Department dept : departments) {
            long empCount = employeeRepository.countByDepartmentId(dept.getId());
            String managerName = dept.getManager() != null
                    ? dept.getManager().getFirstName() + " " + dept.getManager().getLastName()
                    : "N/A";

            result.add(DepartmentStatsResponse.builder()
                    .departmentId(dept.getId())
                    .departmentName(dept.getName())
                    .departmentCode(dept.getCode())
                    .managerName(managerName)
                    .employeeCount(empCount)
                    .build());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollSummaryResponse> getPayrollSummary() {
        log.info("Fetching payroll summary from Database");
        Page<PayrollPeriod> periodPage = payrollPeriodRepository.findAllByOrderByYearDescMonthDesc(Pageable.ofSize(12));
        List<PayrollSummaryResponse> result = new ArrayList<>();

        for (PayrollPeriod period : periodPage.getContent()) {
            Page<Payslip> payslips = payslipRepository.searchPayslips(period.getId(), null, null, Pageable.unpaged());
            List<Payslip> payslipList = payslips.getContent();

            long count = payslipList.size();
            BigDecimal gross = payslipList.stream().map(Payslip::getGrossSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tax = payslipList.stream().map(Payslip::getTax).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal net = payslipList.stream().map(Payslip::getNetSalary).reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(PayrollSummaryResponse.builder()
                    .periodId(period.getId())
                    .periodName(period.getName())
                    .year(period.getYear())
                    .month(period.getMonth())
                    .totalEmployeesPaid(count)
                    .totalGrossSalary(gross)
                    .totalTaxDeducted(tax)
                    .totalNetSalary(net)
                    .build());
        }

        return result;
    }
}

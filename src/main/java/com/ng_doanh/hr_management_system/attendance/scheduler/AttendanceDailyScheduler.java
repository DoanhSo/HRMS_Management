package com.ng_doanh.hr_management_system.attendance.scheduler;

import com.ng_doanh.hr_management_system.attendance.entity.Attendance;
import com.ng_doanh.hr_management_system.attendance.enums.AttendanceStatus;
import com.ng_doanh.hr_management_system.attendance.repository.AttendanceRepository;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.enums.EmploymentStatus;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceDailyScheduler {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final com.ng_doanh.hr_management_system.notification.service.NotificationService notificationService;

    /**
     * Runs at 23:59:00 Monday to Friday to mark absent for employees without check-in.
     */
    @Scheduled(cron = "0 59 23 * * MON-FRI")
    @Transactional
    public void markAbsentEmployees() {
        LocalDate today = LocalDate.now();

        // Safety check for weekends
        if (today.getDayOfWeek() == DayOfWeek.SATURDAY || today.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return;
        }

        log.info("Starting daily absent marking scheduler for date: {}", today);

        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getEmploymentStatus() == EmploymentStatus.ACTIVE || e.getEmploymentStatus() == EmploymentStatus.PROBATION)
                .toList();

        int markedCount = 0;
        for (Employee employee : activeEmployees) {
            boolean hasRecord = attendanceRepository.findByEmployeeIdAndWorkDate(employee.getId(), today).isPresent();
            if (!hasRecord) {
                Attendance absentRecord = Attendance.builder()
                        .employee(employee)
                        .workDate(today)
                        .status(AttendanceStatus.ABSENT)
                        .build();

                attendanceRepository.save(absentRecord);
                markedCount++;

                // Notify employee of absent status
                if (employee.getUser() != null) {
                    notificationService.send(
                            employee.getUser().getId(),
                            com.ng_doanh.hr_management_system.notification.enums.NotificationType.ATTENDANCE_ABSENT,
                            "Chưa ghi nhận chấm công",
                            String.format("Hệ thống ghi nhận bạn chưa chấm công ngày %s", today),
                            "/attendance"
                    );
                }
            }
        }

        log.info("Daily absent marking scheduler completed. Marked {} employees as ABSENT for {}", markedCount, today);
    }
}

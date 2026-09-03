package com.ng_doanh.hr_management_system.attendance.dto.request;

import com.ng_doanh.hr_management_system.attendance.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceManualRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Work date is required")
    private LocalDate workDate;

    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    private AttendanceStatus status;

    private String notes;
}

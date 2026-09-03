package com.ng_doanh.hr_management_system.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceOverviewResponse {

    private LocalDate date;
    private long totalActiveEmployees;
    private long presentCount;
    private long lateCount;
    private long earlyLeaveCount;
    private long onLeaveCount;
    private long absentCount;
    private double attendanceRatePercentage;
}

package com.ng_doanh.hr_management_system.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private long totalEmployees;
    private long activeEmployees;
    private long probationEmployees;
    private long totalDepartments;
    private long totalPositions;
    private BigDecimal latestMonthlyPayrollCost;
}

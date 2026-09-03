package com.ng_doanh.hr_management_system.dashboard.service;

import com.ng_doanh.hr_management_system.dashboard.dto.response.AttendanceOverviewResponse;
import com.ng_doanh.hr_management_system.dashboard.dto.response.DashboardSummaryResponse;
import com.ng_doanh.hr_management_system.dashboard.dto.response.DepartmentStatsResponse;
import com.ng_doanh.hr_management_system.dashboard.dto.response.PayrollSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    DashboardSummaryResponse getDashboardSummary();

    AttendanceOverviewResponse getAttendanceOverview(LocalDate date);

    List<DepartmentStatsResponse> getDepartmentStats();

    List<PayrollSummaryResponse> getPayrollSummary();
}

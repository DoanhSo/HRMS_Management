package com.ng_doanh.hr_management_system.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatsResponse {

    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private String managerName;
    private long employeeCount;
}

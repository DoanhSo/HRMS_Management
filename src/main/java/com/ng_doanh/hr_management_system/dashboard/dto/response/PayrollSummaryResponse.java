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
public class PayrollSummaryResponse {

    private Long periodId;
    private String periodName;
    private int year;
    private int month;
    private long totalEmployeesPaid;
    private BigDecimal totalGrossSalary;
    private BigDecimal totalTaxDeducted;
    private BigDecimal totalNetSalary;
}

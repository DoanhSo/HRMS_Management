package com.ng_doanh.hr_management_system.payroll.dto.response;

import com.ng_doanh.hr_management_system.payroll.enums.PayslipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayslipResponse {

    private Long id;
    private Long payrollPeriodId;
    private String payrollPeriodName;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private BigDecimal basicSalary;
    private BigDecimal actualWorkDays;
    private BigDecimal grossSalary;
    private BigDecimal allowances;
    private BigDecimal bonus;
    private BigDecimal deductions;
    private BigDecimal tax;
    private BigDecimal netSalary;
    private PayslipStatus status;
    private String notes;
    private LocalDateTime createdAt;
}

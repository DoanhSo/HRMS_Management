package com.ng_doanh.hr_management_system.payroll.dto.response;

import com.ng_doanh.hr_management_system.payroll.enums.PayrollPeriodStatus;
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
public class PayrollPeriodResponse {

    private Long id;
    private String name;
    private int year;
    private int month;
    private LocalDate startDate;
    private LocalDate endDate;
    private int workingDays;
    private PayrollPeriodStatus status;
    private LocalDateTime createdAt;
}

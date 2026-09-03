package com.ng_doanh.hr_management_system.salary.dto.response;

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
public class SalaryScaleResponse {

    private Long id;
    private String code;
    private String title;
    private Long positionId;
    private String positionTitle;
    private BigDecimal coefficient;
    private BigDecimal baseSalary;
    private BigDecimal standardBonus;
    private BigDecimal calculatedSalary; // baseSalary * coefficient
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

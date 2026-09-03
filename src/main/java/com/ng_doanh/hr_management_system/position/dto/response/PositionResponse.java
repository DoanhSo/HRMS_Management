package com.ng_doanh.hr_management_system.position.dto.response;

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
public class PositionResponse {

    private Long id;
    private String title;
    private String code;
    private String description;
    private Long departmentId;
    private String departmentName;
    private BigDecimal basicSalary;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private long employeeCount;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

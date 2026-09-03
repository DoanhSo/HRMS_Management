package com.ng_doanh.hr_management_system.position.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionCreateRequest {

    @NotBlank(message = "Position title is required")
    private String title;

    @Size(max = 20, message = "Position code must not exceed 20 characters")
    private String code;

    private String description;

    private Long departmentId;

    @NotNull(message = "Basic salary is required")
    @DecimalMin(value = "0.0", message = "Basic salary must be greater than or equal to 0")
    private BigDecimal basicSalary;

    private BigDecimal minSalary;

    private BigDecimal maxSalary;
}

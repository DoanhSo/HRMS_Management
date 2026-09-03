package com.ng_doanh.hr_management_system.position.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class PositionUpdateRequest {

    @Size(max = 20, message = "Position code must not exceed 20 characters")
    private String code;

    @NotBlank(message = "Position title is required")
    private String title;

    private String description;

    private Long departmentId;

    @DecimalMin(value = "0.0", message = "Basic salary must be greater than or equal to 0")
    private BigDecimal basicSalary;

    private BigDecimal minSalary;

    private BigDecimal maxSalary;

    private Boolean active;
}

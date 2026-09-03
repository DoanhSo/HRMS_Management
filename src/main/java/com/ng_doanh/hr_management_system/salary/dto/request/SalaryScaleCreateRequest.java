package com.ng_doanh.hr_management_system.salary.dto.request;

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
public class SalaryScaleCreateRequest {

    @Size(max = 20, message = "Mã ngạch/bậc lương không được vượt quá 20 ký tự")
    private String code;

    @NotBlank(message = "Tên ngạch/bậc lương không được để trống")
    private String title;

    private Long positionId;

    @NotNull(message = "Hệ số lương không được để trống")
    @DecimalMin(value = "0.1", message = "Hệ số lương phải lớn hơn 0")
    private BigDecimal coefficient;

    @NotNull(message = "Mức lương gốc không được để trống")
    @DecimalMin(value = "0.0", message = "Mức lương gốc không được âm")
    private BigDecimal baseSalary;

    @Builder.Default
    private BigDecimal standardBonus = BigDecimal.ZERO;

    @Builder.Default
    private Boolean active = true;
}

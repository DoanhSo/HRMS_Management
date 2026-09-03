package com.ng_doanh.hr_management_system.kpi.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiEvaluationDetailRequest {

    @NotNull(message = "ID tiêu chí không được để trống")
    private Long kpiCriteriaId;

    @NotNull(message = "Điểm số không được để trống")
    @DecimalMin(value = "0.0", message = "Điểm phải từ 0 đến 100")
    @DecimalMax(value = "100.0", message = "Điểm phải từ 0 đến 100")
    private BigDecimal score;

    private String comments;
}

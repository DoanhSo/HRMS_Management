package com.ng_doanh.hr_management_system.kpi.dto.response;

import com.ng_doanh.hr_management_system.kpi.enums.KpiEvaluationStatus;
import com.ng_doanh.hr_management_system.kpi.enums.KpiRating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiEvaluationResponse {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private Integer periodMonth;
    private Integer periodYear;
    private Long evaluatorId;
    private String evaluatorName;
    private BigDecimal totalScore;
    private KpiRating rating;
    private BigDecimal kpiCoefficient;
    private BigDecimal bonusAmount;
    private KpiEvaluationStatus status;
    private String feedback;
    private List<KpiEvaluationDetailResponse> details;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

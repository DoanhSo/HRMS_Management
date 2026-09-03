package com.ng_doanh.hr_management_system.kpi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiEvaluationDetailResponse {

    private Long id;
    private Long kpiCriteriaId;
    private String kpiCriteriaCode;
    private String kpiCriteriaName;
    private BigDecimal score;
    private Integer weight;
    private String comments;
}

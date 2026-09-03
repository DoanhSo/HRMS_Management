package com.ng_doanh.hr_management_system.kpi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiCriteriaResponse {

    private Long id;
    private String code;
    private String name;
    private Long departmentId;
    private String departmentName;
    private Integer weight;
    private String targetDescription;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

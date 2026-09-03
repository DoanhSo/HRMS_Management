package com.ng_doanh.hr_management_system.kpi.mapper;

import com.ng_doanh.hr_management_system.kpi.dto.request.KpiCriteriaCreateRequest;
import com.ng_doanh.hr_management_system.kpi.dto.response.KpiCriteriaResponse;
import com.ng_doanh.hr_management_system.kpi.dto.response.KpiEvaluationDetailResponse;
import com.ng_doanh.hr_management_system.kpi.dto.response.KpiEvaluationResponse;
import com.ng_doanh.hr_management_system.kpi.entity.KpiCriteria;
import com.ng_doanh.hr_management_system.kpi.entity.KpiEvaluation;
import com.ng_doanh.hr_management_system.kpi.entity.KpiEvaluationDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface KpiMapper {

    @Mapping(target = "department", ignore = true)
    KpiCriteria toEntity(KpiCriteriaCreateRequest request);

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    KpiCriteriaResponse toResponse(KpiCriteria entity);

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeCode", source = "employee.employeeCode")
    @Mapping(target = "employeeName", expression = "java(entity.getEmployee() != null ? entity.getEmployee().getFirstName() + \" \" + entity.getEmployee().getLastName() : \"\")")
    @Mapping(target = "evaluatorId", source = "evaluator.id")
    @Mapping(target = "evaluatorName", expression = "java(entity.getEvaluator() != null ? entity.getEvaluator().getFirstName() + \" \" + entity.getEvaluator().getLastName() : \"\")")
    @Mapping(target = "departmentName", ignore = true)
    KpiEvaluationResponse toResponse(KpiEvaluation entity);

    @Mapping(target = "kpiCriteriaId", source = "kpiCriteria.id")
    @Mapping(target = "kpiCriteriaCode", source = "kpiCriteria.code")
    @Mapping(target = "kpiCriteriaName", source = "kpiCriteria.name")
    KpiEvaluationDetailResponse toResponse(KpiEvaluationDetail detail);
}

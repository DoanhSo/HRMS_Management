package com.ng_doanh.hr_management_system.kpi.service;

import com.ng_doanh.hr_management_system.kpi.dto.request.KpiCriteriaCreateRequest;
import com.ng_doanh.hr_management_system.kpi.dto.request.KpiCriteriaUpdateRequest;
import com.ng_doanh.hr_management_system.kpi.dto.request.KpiEvaluationCreateRequest;
import com.ng_doanh.hr_management_system.kpi.dto.response.KpiCriteriaResponse;
import com.ng_doanh.hr_management_system.kpi.dto.response.KpiEvaluationResponse;
import com.ng_doanh.hr_management_system.kpi.enums.KpiEvaluationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface KpiService {

    // --- KPI Criteria ---
    KpiCriteriaResponse createCriteria(KpiCriteriaCreateRequest request);

    KpiCriteriaResponse updateCriteria(Long id, KpiCriteriaUpdateRequest request);

    void deleteCriteria(Long id);

    List<KpiCriteriaResponse> getAllActiveCriteria();

    Page<KpiCriteriaResponse> searchCriteria(String keyword, Long departmentId, Boolean active, Pageable pageable);

    // --- KPI Evaluation ---
    KpiEvaluationResponse createOrUpdateEvaluation(KpiEvaluationCreateRequest request, Long evaluatorUserId);

    KpiEvaluationResponse getEvaluationById(Long id);

    KpiEvaluationResponse getEmployeeEvaluation(Long employeeId, Integer year, Integer month);

    Page<KpiEvaluationResponse> getMyEvaluations(Long userId, Pageable pageable);

    Page<KpiEvaluationResponse> searchEvaluations(Integer year, Integer month, KpiEvaluationStatus status, Long departmentId, String keyword, Pageable pageable);

    KpiEvaluationResponse approveEvaluation(Long id, Long approverUserId, String feedback);

    KpiEvaluationResponse rejectEvaluation(Long id, Long approverUserId, String feedback);
}

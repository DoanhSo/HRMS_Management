package com.ng_doanh.hr_management_system.kpi.service.impl;

import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.common.util.CodeGeneratorUtil;
import com.ng_doanh.hr_management_system.department.entity.Department;
import com.ng_doanh.hr_management_system.department.repository.DepartmentRepository;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import com.ng_doanh.hr_management_system.kpi.dto.request.*;
import com.ng_doanh.hr_management_system.kpi.dto.response.*;
import com.ng_doanh.hr_management_system.kpi.entity.KpiCriteria;
import com.ng_doanh.hr_management_system.kpi.entity.KpiEvaluation;
import com.ng_doanh.hr_management_system.kpi.entity.KpiEvaluationDetail;
import com.ng_doanh.hr_management_system.kpi.enums.KpiEvaluationStatus;
import com.ng_doanh.hr_management_system.kpi.enums.KpiRating;
import com.ng_doanh.hr_management_system.kpi.mapper.KpiMapper;
import com.ng_doanh.hr_management_system.kpi.repository.KpiCriteriaRepository;
import com.ng_doanh.hr_management_system.kpi.repository.KpiEvaluationRepository;
import com.ng_doanh.hr_management_system.kpi.service.KpiService;
import com.ng_doanh.hr_management_system.position.entity.Position;
import com.ng_doanh.hr_management_system.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KpiServiceImpl implements KpiService {

    private final KpiCriteriaRepository kpiCriteriaRepository;
    private final KpiEvaluationRepository kpiEvaluationRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final KpiMapper kpiMapper;

    // =========================================================================
    // 1. KPI Criteria Management
    // =========================================================================

    @Override
    @Transactional
    public KpiCriteriaResponse createCriteria(KpiCriteriaCreateRequest request) {
        String code;
        if (request.getCode() == null || request.getCode().isBlank()) {
            code = CodeGeneratorUtil.generateCode("KPI-", 5, kpiCriteriaRepository.count(), kpiCriteriaRepository::existsByCode);
        } else {
            code = request.getCode().trim();
            if (kpiCriteriaRepository.existsByCode(code)) {
                throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
            }
        }

        KpiCriteria criteria = kpiMapper.toEntity(request);
        criteria.setCode(code);
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
            criteria.setDepartment(dept);
        }

        KpiCriteria saved = kpiCriteriaRepository.save(criteria);
        log.info("KPI Criteria created: {} ({})", saved.getName(), saved.getCode());
        return kpiMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public KpiCriteriaResponse updateCriteria(Long id, KpiCriteriaUpdateRequest request) {
        KpiCriteria criteria = kpiCriteriaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        if (request.getCode() != null && !request.getCode().isBlank()) {
            String newCode = request.getCode().trim();
            if (!newCode.equalsIgnoreCase(criteria.getCode())) {
                if (kpiCriteriaRepository.existsByCode(newCode)) {
                    throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
                }
                criteria.setCode(newCode);
            }
        }

        criteria.setName(request.getName());
        criteria.setWeight(request.getWeight());
        criteria.setTargetDescription(request.getTargetDescription());
        criteria.setActive(request.getActive());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
            criteria.setDepartment(dept);
        } else {
            criteria.setDepartment(null);
        }

        KpiCriteria updated = kpiCriteriaRepository.save(criteria);
        return kpiMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCriteria(Long id) {
        if (!kpiCriteriaRepository.existsById(id)) {
            throw new BusinessException(ResponseCode.RESOURCE_NOT_FOUND);
        }
        kpiCriteriaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiCriteriaResponse> getAllActiveCriteria() {
        return kpiCriteriaRepository.findByActiveTrue().stream()
                .map(kpiMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KpiCriteriaResponse> searchCriteria(String keyword, Long departmentId, Boolean active, Pageable pageable) {
        return kpiCriteriaRepository.searchCriteria(keyword, departmentId, active, pageable)
                .map(kpiMapper::toResponse);
    }

    // =========================================================================
    // 2. KPI Evaluation Management
    // =========================================================================

    @Override
    @Transactional
    public KpiEvaluationResponse createOrUpdateEvaluation(KpiEvaluationCreateRequest request, Long evaluatorUserId) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        Employee evaluator = null;
        if (evaluatorUserId != null) {
            evaluator = employeeRepository.findByUserId(evaluatorUserId).orElse(null);
        }

        KpiEvaluation evaluation = kpiEvaluationRepository
                .findByEmployeeIdAndPeriodYearAndPeriodMonth(employee.getId(), request.getPeriodYear(), request.getPeriodMonth())
                .orElseGet(() -> KpiEvaluation.builder()
                        .employee(employee)
                        .periodYear(request.getPeriodYear())
                        .periodMonth(request.getPeriodMonth())
                        .build());

        if (evaluation.getStatus() == KpiEvaluationStatus.APPROVED) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }

        evaluation.setEvaluator(evaluator);
        evaluation.setFeedback(request.getFeedback());
        evaluation.setStatus(KpiEvaluationStatus.SUBMITTED);

        // Fetch criteria map
        List<Long> criteriaIds = request.getDetails().stream()
                .map(KpiEvaluationDetailRequest::getKpiCriteriaId)
                .toList();
        Map<Long, KpiCriteria> criteriaMap = kpiCriteriaRepository.findAllById(criteriaIds).stream()
                .collect(Collectors.toMap(KpiCriteria::getId, Function.identity()));

        // Calculate total score
        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        int totalWeight = 0;

        List<KpiEvaluationDetail> details = new ArrayList<>();
        for (KpiEvaluationDetailRequest detailReq : request.getDetails()) {
            KpiCriteria criteria = criteriaMap.get(detailReq.getKpiCriteriaId());
            if (criteria == null) continue;

            int weight = criteria.getWeight() != null ? criteria.getWeight() : 20;
            BigDecimal itemWeightedScore = detailReq.getScore()
                    .multiply(BigDecimal.valueOf(weight))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            totalWeightedScore = totalWeightedScore.add(itemWeightedScore);
            totalWeight += weight;

            KpiEvaluationDetail detail = KpiEvaluationDetail.builder()
                    .kpiEvaluation(evaluation)
                    .kpiCriteria(criteria)
                    .score(detailReq.getScore())
                    .weight(weight)
                    .comments(detailReq.getComments())
                    .build();
            details.add(detail);
        }

        evaluation.setTotalScore(totalWeightedScore);

        // Calculate Rating and Coefficient
        KpiRating rating = calculateRating(totalWeightedScore);
        BigDecimal coefficient = calculateCoefficient(rating);
        evaluation.setRating(rating);
        evaluation.setKpiCoefficient(coefficient);

        // Calculate Bonus Amount
        BigDecimal basicSalary = BigDecimal.ZERO;
        if (employee.getPositionId() != null) {
            basicSalary = positionRepository.findById(employee.getPositionId())
                    .map(Position::getBasicSalary)
                    .orElse(BigDecimal.ZERO);
        }

        // Bonus: if coefficient > 1.0 -> bonus = basicSalary * (coefficient - 1.0)
        BigDecimal bonusAmount = BigDecimal.ZERO;
        if (coefficient.compareTo(BigDecimal.ONE) > 0) {
            bonusAmount = basicSalary.multiply(coefficient.subtract(BigDecimal.ONE)).setScale(2, RoundingMode.HALF_UP);
        }
        evaluation.setBonusAmount(bonusAmount);

        evaluation.getDetails().clear();
        evaluation.getDetails().addAll(details);

        KpiEvaluation saved = kpiEvaluationRepository.save(evaluation);
        log.info("KPI Evaluation saved for employee {} ({}/{}): Score={}, Rating={}, Bonus={}",
                employee.getEmployeeCode(), request.getPeriodMonth(), request.getPeriodYear(),
                totalWeightedScore, rating, bonusAmount);

        return kpiMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public KpiEvaluationResponse getEvaluationById(Long id) {
        KpiEvaluation evaluation = kpiEvaluationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
        return kpiMapper.toResponse(evaluation);
    }

    @Override
    @Transactional(readOnly = true)
    public KpiEvaluationResponse getEmployeeEvaluation(Long employeeId, Integer year, Integer month) {
        return kpiEvaluationRepository.findByEmployeeIdAndPeriodYearAndPeriodMonth(employeeId, year, month)
                .map(kpiMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KpiEvaluationResponse> getMyEvaluations(Long userId, Pageable pageable) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        return kpiEvaluationRepository.findByEmployeeIdOrderByPeriodYearDescPeriodMonthDesc(employee.getId(), pageable)
                .map(kpiMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KpiEvaluationResponse> searchEvaluations(
            Integer year, Integer month, KpiEvaluationStatus status, Long departmentId, String keyword, Pageable pageable
    ) {
        return kpiEvaluationRepository.searchEvaluations(year, month, status, departmentId, keyword, pageable)
                .map(kpiMapper::toResponse);
    }

    @Override
    @Transactional
    public KpiEvaluationResponse approveEvaluation(Long id, Long approverUserId, String feedback) {
        KpiEvaluation evaluation = kpiEvaluationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        if (approverUserId != null) {
            employeeRepository.findByUserId(approverUserId).ifPresent(evaluation::setEvaluator);
        }

        evaluation.setStatus(KpiEvaluationStatus.APPROVED);
        if (feedback != null && !feedback.isBlank()) {
            evaluation.setFeedback(feedback);
        }

        KpiEvaluation saved = kpiEvaluationRepository.save(evaluation);
        log.info("KPI Evaluation approved for employee {} ({}/{})",
                evaluation.getEmployee().getEmployeeCode(), evaluation.getPeriodMonth(), evaluation.getPeriodYear());

        return kpiMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public KpiEvaluationResponse rejectEvaluation(Long id, Long approverUserId, String feedback) {
        KpiEvaluation evaluation = kpiEvaluationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        evaluation.setStatus(KpiEvaluationStatus.REJECTED);
        if (feedback != null && !feedback.isBlank()) {
            evaluation.setFeedback(feedback);
        }

        KpiEvaluation saved = kpiEvaluationRepository.save(evaluation);
        return kpiMapper.toResponse(saved);
    }

    private KpiRating calculateRating(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(90.0)) >= 0) return KpiRating.A;
        if (score.compareTo(BigDecimal.valueOf(75.0)) >= 0) return KpiRating.B;
        if (score.compareTo(BigDecimal.valueOf(50.0)) >= 0) return KpiRating.C;
        return KpiRating.D;
    }

    private BigDecimal calculateCoefficient(KpiRating rating) {
        return switch (rating) {
            case A -> BigDecimal.valueOf(1.50);
            case B -> BigDecimal.valueOf(1.20);
            case C -> BigDecimal.valueOf(1.00);
            case D -> BigDecimal.valueOf(0.50);
        };
    }
}

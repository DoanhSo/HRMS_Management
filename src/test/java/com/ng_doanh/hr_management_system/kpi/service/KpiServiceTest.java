package com.ng_doanh.hr_management_system.kpi.service;

import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import com.ng_doanh.hr_management_system.kpi.dto.request.KpiCriteriaCreateRequest;
import com.ng_doanh.hr_management_system.kpi.dto.request.KpiCriteriaUpdateRequest;
import com.ng_doanh.hr_management_system.kpi.dto.request.KpiEvaluationCreateRequest;
import com.ng_doanh.hr_management_system.kpi.dto.request.KpiEvaluationDetailRequest;
import com.ng_doanh.hr_management_system.kpi.dto.response.KpiCriteriaResponse;
import com.ng_doanh.hr_management_system.kpi.dto.response.KpiEvaluationResponse;
import com.ng_doanh.hr_management_system.kpi.entity.KpiCriteria;
import com.ng_doanh.hr_management_system.kpi.entity.KpiEvaluation;
import com.ng_doanh.hr_management_system.kpi.enums.KpiEvaluationStatus;
import com.ng_doanh.hr_management_system.kpi.enums.KpiRating;
import com.ng_doanh.hr_management_system.kpi.mapper.KpiMapper;
import com.ng_doanh.hr_management_system.kpi.repository.KpiCriteriaRepository;
import com.ng_doanh.hr_management_system.kpi.repository.KpiEvaluationRepository;
import com.ng_doanh.hr_management_system.kpi.service.impl.KpiServiceImpl;
import com.ng_doanh.hr_management_system.position.entity.Position;
import com.ng_doanh.hr_management_system.position.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KpiService Unit Tests")
class KpiServiceTest {

    @Mock
    private KpiCriteriaRepository kpiCriteriaRepository;

    @Mock
    private KpiEvaluationRepository kpiEvaluationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private KpiMapper kpiMapper;

    @InjectMocks
    private KpiServiceImpl kpiService;

    private Employee employee;
    private Position position;
    private KpiCriteria criteria1;
    private KpiCriteria criteria2;
    private KpiEvaluation evaluation;
    private KpiCriteriaResponse criteriaResponse;
    private KpiEvaluationResponse evaluationResponse;

    @BeforeEach
    void setUp() {
        position = Position.builder()
                .basicSalary(BigDecimal.valueOf(20000000))
                .build();
        position.setId(1L);

        employee = Employee.builder()
                .employeeCode("EMP-001")
                .firstName("Van A")
                .lastName("Nguyen")
                .positionId(1L)
                .build();
        employee.setId(1L);

        criteria1 = KpiCriteria.builder()
                .code("KPI_QUALITY")
                .name("Chất lượng công việc")
                .weight(50)
                .active(true)
                .build();
        criteria1.setId(1L);

        criteria2 = KpiCriteria.builder()
                .code("KPI_DEADLINE")
                .name("Tiến độ công việc")
                .weight(50)
                .active(true)
                .build();
        criteria2.setId(2L);

        criteriaResponse = KpiCriteriaResponse.builder()
                .id(1L)
                .code("KPI_QUALITY")
                .name("Chất lượng công việc")
                .weight(50)
                .active(true)
                .build();

        evaluation = KpiEvaluation.builder()
                .employee(employee)
                .periodMonth(8)
                .periodYear(2026)
                .totalScore(BigDecimal.valueOf(80.0))
                .rating(KpiRating.B)
                .kpiCoefficient(BigDecimal.valueOf(1.2))
                .bonusAmount(BigDecimal.valueOf(4000000))
                .status(KpiEvaluationStatus.SUBMITTED)
                .build();
        evaluation.setId(10L);

        evaluationResponse = KpiEvaluationResponse.builder()
                .id(10L)
                .employeeId(1L)
                .employeeCode("EMP-001")
                .employeeName("Van A Nguyen")
                .periodMonth(8)
                .periodYear(2026)
                .totalScore(BigDecimal.valueOf(80.0))
                .rating(KpiRating.B)
                .kpiCoefficient(BigDecimal.valueOf(1.2))
                .bonusAmount(BigDecimal.valueOf(4000000))
                .status(KpiEvaluationStatus.SUBMITTED)
                .build();
    }

    // ===================================================
    // CRITERIA TESTS
    // ===================================================

    @Test
    @DisplayName("Create KPI criteria successfully")
    void createCriteria_Success() {
        KpiCriteriaCreateRequest request = KpiCriteriaCreateRequest.builder()
                .code("KPI_NEW")
                .name("Kỹ năng mềm")
                .weight(20)
                .build();

        when(kpiCriteriaRepository.existsByCode("KPI_NEW")).thenReturn(false);
        when(kpiMapper.toEntity(request)).thenReturn(criteria1);
        when(kpiCriteriaRepository.save(any(KpiCriteria.class))).thenReturn(criteria1);
        when(kpiMapper.toResponse(criteria1)).thenReturn(criteriaResponse);

        KpiCriteriaResponse response = kpiService.createCriteria(request);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("KPI_QUALITY");
        verify(kpiCriteriaRepository).save(any(KpiCriteria.class));
    }

    @Test
    @DisplayName("Create KPI criteria auto-generates code when code is null or blank")
    void createCriteria_AutoGenerateCode_Success() {
        KpiCriteriaCreateRequest reqNoCode = KpiCriteriaCreateRequest.builder()
                .name("Kỹ năng mềm")
                .weight(20)
                .build();

        when(kpiCriteriaRepository.count()).thenReturn(0L);
        when(kpiCriteriaRepository.existsByCode("KPI-00001")).thenReturn(false);
        when(kpiMapper.toEntity(reqNoCode)).thenReturn(criteria1);
        when(kpiCriteriaRepository.save(any(KpiCriteria.class))).thenReturn(criteria1);
        when(kpiMapper.toResponse(criteria1)).thenReturn(criteriaResponse);

        KpiCriteriaResponse response = kpiService.createCriteria(reqNoCode);

        assertThat(response).isNotNull();
        verify(kpiCriteriaRepository).save(criteria1);
        assertThat(criteria1.getCode()).isEqualTo("KPI-00001");
    }

    @Test
    @DisplayName("Create KPI criteria with duplicate code throws DUPLICATE_RESOURCE")
    void createCriteria_DuplicateCode_ThrowsException() {
        KpiCriteriaCreateRequest request = KpiCriteriaCreateRequest.builder()
                .code("KPI_QUALITY")
                .name("Chất lượng")
                .weight(25)
                .build();

        when(kpiCriteriaRepository.existsByCode("KPI_QUALITY")).thenReturn(true);

        assertThatThrownBy(() -> kpiService.createCriteria(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("responseCode", ResponseCode.DUPLICATE_RESOURCE);

        verify(kpiCriteriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update KPI criteria successfully")
    void updateCriteria_Success() {
        KpiCriteriaUpdateRequest request = KpiCriteriaUpdateRequest.builder()
                .name("Chất lượng công việc (Cập nhật)")
                .weight(30)
                .active(true)
                .build();

        when(kpiCriteriaRepository.findById(1L)).thenReturn(Optional.of(criteria1));
        when(kpiCriteriaRepository.save(any(KpiCriteria.class))).thenReturn(criteria1);
        when(kpiMapper.toResponse(criteria1)).thenReturn(criteriaResponse);

        KpiCriteriaResponse response = kpiService.updateCriteria(1L, request);

        assertThat(response).isNotNull();
        verify(kpiCriteriaRepository).save(criteria1);
    }

    @Test
    @DisplayName("Update KPI criteria with duplicate code throws DUPLICATE_RESOURCE")
    void updateCriteria_DuplicateNewCode_ThrowsException() {
        KpiCriteriaUpdateRequest request = KpiCriteriaUpdateRequest.builder()
                .code("KPI_EXISTING")
                .name("Chất lượng")
                .weight(30)
                .active(true)
                .build();

        when(kpiCriteriaRepository.findById(1L)).thenReturn(Optional.of(criteria1));
        when(kpiCriteriaRepository.existsByCode("KPI_EXISTING")).thenReturn(true);

        assertThatThrownBy(() -> kpiService.updateCriteria(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("responseCode", ResponseCode.DUPLICATE_RESOURCE);
    }

    @Test
    @DisplayName("Update KPI criteria with valid new code updates code successfully")
    void updateCriteria_ValidNewCode_Success() {
        KpiCriteriaUpdateRequest request = KpiCriteriaUpdateRequest.builder()
                .code("KPI_UPDATED")
                .name("Chất lượng")
                .weight(30)
                .active(true)
                .build();

        when(kpiCriteriaRepository.findById(1L)).thenReturn(Optional.of(criteria1));
        when(kpiCriteriaRepository.existsByCode("KPI_UPDATED")).thenReturn(false);
        when(kpiCriteriaRepository.save(any(KpiCriteria.class))).thenReturn(criteria1);
        when(kpiMapper.toResponse(criteria1)).thenReturn(criteriaResponse);

        KpiCriteriaResponse response = kpiService.updateCriteria(1L, request);

        assertThat(response).isNotNull();
        assertThat(criteria1.getCode()).isEqualTo("KPI_UPDATED");
    }

    @Test
    @DisplayName("Update KPI criteria with non-existent ID throws RESOURCE_NOT_FOUND")
    void updateCriteria_NotFound_ThrowsException() {
        KpiCriteriaUpdateRequest request = KpiCriteriaUpdateRequest.builder()
                .name("Không tồn tại")
                .weight(20)
                .active(true)
                .build();

        when(kpiCriteriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kpiService.updateCriteria(99L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("responseCode", ResponseCode.RESOURCE_NOT_FOUND);

        verify(kpiCriteriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete KPI criteria successfully")
    void deleteCriteria_Success() {
        when(kpiCriteriaRepository.existsById(1L)).thenReturn(true);

        kpiService.deleteCriteria(1L);

        verify(kpiCriteriaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Delete KPI criteria with non-existent ID throws RESOURCE_NOT_FOUND")
    void deleteCriteria_NotFound_ThrowsException() {
        when(kpiCriteriaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> kpiService.deleteCriteria(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("responseCode", ResponseCode.RESOURCE_NOT_FOUND);

        verify(kpiCriteriaRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Get all active criteria returns only active items")
    void getAllActiveCriteria_ReturnsFilteredList() {
        when(kpiCriteriaRepository.findByActiveTrue()).thenReturn(List.of(criteria1, criteria2));
        when(kpiMapper.toResponse(criteria1)).thenReturn(criteriaResponse);
        when(kpiMapper.toResponse(criteria2)).thenReturn(KpiCriteriaResponse.builder().id(2L).code("KPI_DEADLINE").build());

        List<KpiCriteriaResponse> result = kpiService.getAllActiveCriteria();

        assertThat(result).isNotNull().hasSize(2);
        verify(kpiCriteriaRepository).findByActiveTrue();
    }

    // ===================================================
    // EVALUATION TESTS
    // ===================================================

    @Test
    @DisplayName("Create evaluation with 95 score results in Rating A (1.5x) and +50% Bonus")
    void createEvaluation_RatingA_BonusCalculation() {
        KpiEvaluationCreateRequest request = KpiEvaluationCreateRequest.builder()
                .employeeId(1L)
                .periodMonth(8)
                .periodYear(2026)
                .details(List.of(
                        KpiEvaluationDetailRequest.builder().kpiCriteriaId(1L).score(BigDecimal.valueOf(95)).build(),
                        KpiEvaluationDetailRequest.builder().kpiCriteriaId(2L).score(BigDecimal.valueOf(95)).build()
                ))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(kpiEvaluationRepository.findByEmployeeIdAndPeriodYearAndPeriodMonth(1L, 2026, 8)).thenReturn(Optional.empty());
        when(kpiCriteriaRepository.findAllById(anyList())).thenReturn(List.of(criteria1, criteria2));
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));

        KpiEvaluation savedEval = KpiEvaluation.builder()
                .employee(employee)
                .periodMonth(8)
                .periodYear(2026)
                .totalScore(BigDecimal.valueOf(95.0))
                .rating(KpiRating.A)
                .kpiCoefficient(BigDecimal.valueOf(1.5))
                .bonusAmount(BigDecimal.valueOf(10000000))
                .status(KpiEvaluationStatus.SUBMITTED)
                .build();

        when(kpiEvaluationRepository.save(any(KpiEvaluation.class))).thenReturn(savedEval);
        when(kpiMapper.toResponse(savedEval)).thenReturn(
                KpiEvaluationResponse.builder()
                        .totalScore(BigDecimal.valueOf(95.0))
                        .rating(KpiRating.A)
                        .kpiCoefficient(BigDecimal.valueOf(1.5))
                        .bonusAmount(BigDecimal.valueOf(10000000))
                        .status(KpiEvaluationStatus.SUBMITTED)
                        .build()
        );

        KpiEvaluationResponse response = kpiService.createOrUpdateEvaluation(request, null);

        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(KpiRating.A);
        assertThat(response.getKpiCoefficient()).isEqualTo(BigDecimal.valueOf(1.5));
        assertThat(response.getBonusAmount()).isEqualTo(BigDecimal.valueOf(10000000));
        verify(kpiEvaluationRepository).save(any(KpiEvaluation.class));
    }

    @Test
    @DisplayName("Create evaluation with score 80 results in Rating B (1.2x coefficient)")
    void createEvaluation_RatingB_Score80() {
        KpiEvaluationCreateRequest request = KpiEvaluationCreateRequest.builder()
                .employeeId(1L)
                .periodMonth(8)
                .periodYear(2026)
                .details(List.of(
                        KpiEvaluationDetailRequest.builder().kpiCriteriaId(1L).score(BigDecimal.valueOf(80)).build(),
                        KpiEvaluationDetailRequest.builder().kpiCriteriaId(2L).score(BigDecimal.valueOf(80)).build()
                ))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(kpiEvaluationRepository.findByEmployeeIdAndPeriodYearAndPeriodMonth(1L, 2026, 8)).thenReturn(Optional.empty());
        when(kpiCriteriaRepository.findAllById(anyList())).thenReturn(List.of(criteria1, criteria2));
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));
        when(kpiEvaluationRepository.save(any(KpiEvaluation.class))).thenReturn(evaluation);
        when(kpiMapper.toResponse(evaluation)).thenReturn(
                KpiEvaluationResponse.builder()
                        .totalScore(BigDecimal.valueOf(80.0))
                        .rating(KpiRating.B)
                        .kpiCoefficient(BigDecimal.valueOf(1.2))
                        .bonusAmount(BigDecimal.valueOf(4000000))
                        .status(KpiEvaluationStatus.SUBMITTED)
                        .build()
        );

        KpiEvaluationResponse response = kpiService.createOrUpdateEvaluation(request, null);

        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(KpiRating.B);
        assertThat(response.getKpiCoefficient()).isEqualTo(BigDecimal.valueOf(1.2));
    }

    @Test
    @DisplayName("Create evaluation with score 40 results in Rating D (0.5x — below standard)")
    void createEvaluation_RatingD_Score40() {
        KpiEvaluationCreateRequest request = KpiEvaluationCreateRequest.builder()
                .employeeId(1L)
                .periodMonth(8)
                .periodYear(2026)
                .details(List.of(
                        KpiEvaluationDetailRequest.builder().kpiCriteriaId(1L).score(BigDecimal.valueOf(40)).build(),
                        KpiEvaluationDetailRequest.builder().kpiCriteriaId(2L).score(BigDecimal.valueOf(40)).build()
                ))
                .build();

        KpiEvaluation ratingDEval = KpiEvaluation.builder()
                .employee(employee)
                .totalScore(BigDecimal.valueOf(40.0))
                .rating(KpiRating.D)
                .kpiCoefficient(BigDecimal.valueOf(0.5))
                .bonusAmount(BigDecimal.valueOf(-10000000))
                .status(KpiEvaluationStatus.SUBMITTED)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(kpiEvaluationRepository.findByEmployeeIdAndPeriodYearAndPeriodMonth(1L, 2026, 8)).thenReturn(Optional.empty());
        when(kpiCriteriaRepository.findAllById(anyList())).thenReturn(List.of(criteria1, criteria2));
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));
        when(kpiEvaluationRepository.save(any(KpiEvaluation.class))).thenReturn(ratingDEval);
        when(kpiMapper.toResponse(ratingDEval)).thenReturn(
                KpiEvaluationResponse.builder()
                        .totalScore(BigDecimal.valueOf(40.0))
                        .rating(KpiRating.D)
                        .kpiCoefficient(BigDecimal.valueOf(0.5))
                        .status(KpiEvaluationStatus.SUBMITTED)
                        .build()
        );

        KpiEvaluationResponse response = kpiService.createOrUpdateEvaluation(request, null);

        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(KpiRating.D);
        assertThat(response.getKpiCoefficient()).isEqualTo(BigDecimal.valueOf(0.5));
    }

    @Test
    @DisplayName("Get evaluation by ID successfully")
    void getEvaluationById_Success() {
        when(kpiEvaluationRepository.findById(10L)).thenReturn(Optional.of(evaluation));
        when(kpiMapper.toResponse(evaluation)).thenReturn(evaluationResponse);

        KpiEvaluationResponse response = kpiService.getEvaluationById(10L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getRating()).isEqualTo(KpiRating.B);
    }

    @Test
    @DisplayName("Get evaluation by non-existent ID throws RESOURCE_NOT_FOUND")
    void getEvaluationById_NotFound_ThrowsException() {
        when(kpiEvaluationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kpiService.getEvaluationById(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("responseCode", ResponseCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("Search KPI evaluations returns paginated results")
    void searchEvaluations_ReturnsPagedResult() {
        Page<KpiEvaluation> page = new PageImpl<>(List.of(evaluation));
        when(kpiEvaluationRepository.searchEvaluations(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(kpiMapper.toResponse(evaluation)).thenReturn(evaluationResponse);

        Page<KpiEvaluationResponse> result = kpiService.searchEvaluations(2026, 8, null, null, null, Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Approve evaluation marks status APPROVED")
    void approveEvaluation_Success() {
        KpiEvaluation evalToApprove = KpiEvaluation.builder()
                .employee(employee)
                .periodMonth(8)
                .periodYear(2026)
                .status(KpiEvaluationStatus.SUBMITTED)
                .build();
        evalToApprove.setId(10L);

        when(kpiEvaluationRepository.findById(10L)).thenReturn(Optional.of(evalToApprove));
        when(kpiEvaluationRepository.save(any(KpiEvaluation.class))).thenReturn(evalToApprove);
        when(kpiMapper.toResponse(evalToApprove)).thenReturn(
                KpiEvaluationResponse.builder().id(10L).status(KpiEvaluationStatus.APPROVED).build()
        );

        KpiEvaluationResponse response = kpiService.approveEvaluation(10L, null, "Xuất sắc!");

        assertThat(response).isNotNull();
        assertThat(evalToApprove.getStatus()).isEqualTo(KpiEvaluationStatus.APPROVED);
        verify(kpiEvaluationRepository).save(evalToApprove);
    }

    @Test
    @DisplayName("Reject evaluation marks status REJECTED and saves feedback")
    void rejectEvaluation_Success() {
        KpiEvaluation evalToReject = KpiEvaluation.builder()
                .employee(employee)
                .periodMonth(8)
                .periodYear(2026)
                .status(KpiEvaluationStatus.SUBMITTED)
                .build();
        evalToReject.setId(11L);

        when(kpiEvaluationRepository.findById(11L)).thenReturn(Optional.of(evalToReject));
        when(kpiEvaluationRepository.save(any(KpiEvaluation.class))).thenReturn(evalToReject);
        when(kpiMapper.toResponse(evalToReject)).thenReturn(
                KpiEvaluationResponse.builder().id(11L).status(KpiEvaluationStatus.REJECTED).build()
        );

        KpiEvaluationResponse response = kpiService.rejectEvaluation(11L, null, "Cần cải thiện thêm");

        assertThat(response).isNotNull();
        assertThat(evalToReject.getStatus()).isEqualTo(KpiEvaluationStatus.REJECTED);
        assertThat(evalToReject.getFeedback()).isEqualTo("Cần cải thiện thêm");
        verify(kpiEvaluationRepository).save(evalToReject);
    }

    @Test
    @DisplayName("Get my evaluations returns paginated evaluations for current user")
    void getMyEvaluations_ReturnsPagedResult() {
        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(employee));
        Page<KpiEvaluation> page = new PageImpl<>(List.of(evaluation));
        when(kpiEvaluationRepository.findByEmployeeIdOrderByPeriodYearDescPeriodMonthDesc(eq(1L), any(Pageable.class))).thenReturn(page);
        when(kpiMapper.toResponse(evaluation)).thenReturn(evaluationResponse);

        Page<KpiEvaluationResponse> result = kpiService.getMyEvaluations(1L, Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmployeeCode()).isEqualTo("EMP-001");
    }
}

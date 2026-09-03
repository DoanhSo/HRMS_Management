package com.ng_doanh.hr_management_system.salary.service;

import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.position.entity.Position;
import com.ng_doanh.hr_management_system.position.repository.PositionRepository;
import com.ng_doanh.hr_management_system.salary.dto.request.SalaryScaleCreateRequest;
import com.ng_doanh.hr_management_system.salary.dto.request.SalaryScaleUpdateRequest;
import com.ng_doanh.hr_management_system.salary.dto.response.SalaryScaleResponse;
import com.ng_doanh.hr_management_system.salary.entity.SalaryScale;
import com.ng_doanh.hr_management_system.salary.mapper.SalaryScaleMapper;
import com.ng_doanh.hr_management_system.salary.repository.SalaryScaleRepository;
import com.ng_doanh.hr_management_system.salary.service.impl.SalaryScaleServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalaryScaleService Unit Tests")
class SalaryScaleServiceTest {

    @Mock
    private SalaryScaleRepository salaryScaleRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private SalaryScaleMapper salaryScaleMapper;

    @InjectMocks
    private SalaryScaleServiceImpl salaryScaleService;

    private SalaryScale scale;
    private Position position;
    private SalaryScaleResponse response;

    @BeforeEach
    void setUp() {
        position = Position.builder()
                .title("Kỹ Sư Phần Mềm")
                .basicSalary(BigDecimal.valueOf(15000000))
                .build();
        position.setId(1L);

        scale = SalaryScale.builder()
                .code("SCALE_DEV_L1")
                .title("Kỹ Sư Phần Mềm - Bậc 1")
                .position(position)
                .coefficient(BigDecimal.valueOf(1.20))
                .baseSalary(BigDecimal.valueOf(15000000))
                .standardBonus(BigDecimal.valueOf(2000000))
                .active(true)
                .build();
        scale.setId(1L);

        response = SalaryScaleResponse.builder()
                .id(1L)
                .code("SCALE_DEV_L1")
                .title("Kỹ Sư Phần Mềm - Bậc 1")
                .positionId(1L)
                .positionTitle("Kỹ Sư Phần Mềm")
                .coefficient(BigDecimal.valueOf(1.20))
                .baseSalary(BigDecimal.valueOf(15000000))
                .standardBonus(BigDecimal.valueOf(2000000))
                .calculatedSalary(BigDecimal.valueOf(18000000))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Create salary scale successfully")
    void createSalaryScale_Success() {
        SalaryScaleCreateRequest request = SalaryScaleCreateRequest.builder()
                .code("SCALE_DEV_L1")
                .title("Kỹ Sư Phần Mềm - Bậc 1")
                .positionId(1L)
                .coefficient(BigDecimal.valueOf(1.20))
                .baseSalary(BigDecimal.valueOf(15000000))
                .standardBonus(BigDecimal.valueOf(2000000))
                .build();

        when(salaryScaleRepository.existsByCode("SCALE_DEV_L1")).thenReturn(false);
        when(salaryScaleMapper.toEntity(request)).thenReturn(scale);
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));
        when(salaryScaleRepository.save(any(SalaryScale.class))).thenReturn(scale);
        when(salaryScaleMapper.toResponse(scale)).thenReturn(response);

        SalaryScaleResponse result = salaryScaleService.createSalaryScale(request);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("SCALE_DEV_L1");
        assertThat(result.getCalculatedSalary()).isEqualTo(BigDecimal.valueOf(18000000));
        verify(salaryScaleRepository).save(any(SalaryScale.class));
    }

    @Test
    @DisplayName("Create salary scale auto-generates code when code is null or blank")
    void createSalaryScale_AutoGenerateCode_Success() {
        SalaryScaleCreateRequest reqNoCode = SalaryScaleCreateRequest.builder()
                .title("Kỹ Sư Phần Mềm - Bậc 1")
                .positionId(1L)
                .coefficient(BigDecimal.valueOf(1.20))
                .baseSalary(BigDecimal.valueOf(15000000))
                .standardBonus(BigDecimal.valueOf(2000000))
                .build();

        when(salaryScaleRepository.count()).thenReturn(0L);
        when(salaryScaleRepository.existsByCode("SS-00001")).thenReturn(false);
        when(salaryScaleMapper.toEntity(reqNoCode)).thenReturn(scale);
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));
        when(salaryScaleRepository.save(any(SalaryScale.class))).thenReturn(scale);
        when(salaryScaleMapper.toResponse(scale)).thenReturn(response);

        SalaryScaleResponse result = salaryScaleService.createSalaryScale(reqNoCode);

        assertThat(result).isNotNull();
        verify(salaryScaleRepository).save(scale);
        assertThat(scale.getCode()).isEqualTo("SS-00001");
    }

    @Test
    @DisplayName("Create salary scale with duplicate code throws DUPLICATE_RESOURCE")
    void createSalaryScale_DuplicateCode_ThrowsException() {
        SalaryScaleCreateRequest request = SalaryScaleCreateRequest.builder()
                .code("SCALE_DEV_L1")
                .title("Kỹ Sư Phần Mềm - Bậc 1")
                .coefficient(BigDecimal.valueOf(1.20))
                .baseSalary(BigDecimal.valueOf(15000000))
                .build();

        when(salaryScaleRepository.existsByCode("SCALE_DEV_L1")).thenReturn(true);

        assertThatThrownBy(() -> salaryScaleService.createSalaryScale(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("responseCode", ResponseCode.DUPLICATE_RESOURCE);

        verify(salaryScaleRepository, never()).save(any(SalaryScale.class));
    }

    @Test
    @DisplayName("Update salary scale successfully")
    void updateSalaryScale_Success() {
        SalaryScaleUpdateRequest request = SalaryScaleUpdateRequest.builder()
                .title("Kỹ Sư Phần Mềm - Bậc 1 (Đã sửa)")
                .positionId(1L)
                .coefficient(BigDecimal.valueOf(1.30))
                .baseSalary(BigDecimal.valueOf(16000000))
                .standardBonus(BigDecimal.valueOf(2500000))
                .active(true)
                .build();

        when(salaryScaleRepository.findById(1L)).thenReturn(Optional.of(scale));
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));
        when(salaryScaleRepository.save(any(SalaryScale.class))).thenReturn(scale);
        when(salaryScaleMapper.toResponse(scale)).thenReturn(response);

        SalaryScaleResponse result = salaryScaleService.updateSalaryScale(1L, request);

        assertThat(result).isNotNull();
        verify(salaryScaleRepository).save(scale);
    }

    @Test
    @DisplayName("Update salary scale with duplicate code throws DUPLICATE_RESOURCE")
    void updateSalaryScale_DuplicateNewCode_ThrowsException() {
        SalaryScaleUpdateRequest request = SalaryScaleUpdateRequest.builder()
                .code("SCALE_EXISTING")
                .title("Kỹ Sư Phần Mềm")
                .coefficient(BigDecimal.valueOf(1.30))
                .baseSalary(BigDecimal.valueOf(16000000))
                .active(true)
                .build();

        when(salaryScaleRepository.findById(1L)).thenReturn(Optional.of(scale));
        when(salaryScaleRepository.existsByCode("SCALE_EXISTING")).thenReturn(true);

        assertThatThrownBy(() -> salaryScaleService.updateSalaryScale(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("responseCode", ResponseCode.DUPLICATE_RESOURCE);
    }

    @Test
    @DisplayName("Update salary scale with valid new code updates code successfully")
    void updateSalaryScale_ValidNewCode_Success() {
        SalaryScaleUpdateRequest request = SalaryScaleUpdateRequest.builder()
                .code("SCALE_UPDATED")
                .title("Kỹ Sư Phần Mềm")
                .coefficient(BigDecimal.valueOf(1.30))
                .baseSalary(BigDecimal.valueOf(16000000))
                .active(true)
                .build();

        when(salaryScaleRepository.findById(1L)).thenReturn(Optional.of(scale));
        when(salaryScaleRepository.existsByCode("SCALE_UPDATED")).thenReturn(false);
        when(salaryScaleRepository.save(any(SalaryScale.class))).thenReturn(scale);
        when(salaryScaleMapper.toResponse(scale)).thenReturn(response);

        SalaryScaleResponse result = salaryScaleService.updateSalaryScale(1L, request);

        assertThat(result).isNotNull();
        assertThat(scale.getCode()).isEqualTo("SCALE_UPDATED");
    }

    @Test
    @DisplayName("Delete salary scale successfully")
    void deleteSalaryScale_Success() {
        when(salaryScaleRepository.existsById(1L)).thenReturn(true);

        salaryScaleService.deleteSalaryScale(1L);

        verify(salaryScaleRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Delete non-existent salary scale throws RESOURCE_NOT_FOUND")
    void deleteSalaryScale_NotFound_ThrowsException() {
        when(salaryScaleRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> salaryScaleService.deleteSalaryScale(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("responseCode", ResponseCode.RESOURCE_NOT_FOUND);

        verify(salaryScaleRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Search salary scales returns paginated list")
    void searchSalaryScales_Success() {
        Page<SalaryScale> page = new PageImpl<>(List.of(scale));
        when(salaryScaleRepository.searchSalaryScales(any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(salaryScaleMapper.toResponse(scale)).thenReturn(response);

        Page<SalaryScaleResponse> result = salaryScaleService.searchSalaryScales("DEV", 1L, true, Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    // ===================================================
    // GET BY ID TESTS
    // ===================================================

    @Test
    @DisplayName("Get salary scale by ID successfully returns scale details")
    void getSalaryScaleById_Success() {
        when(salaryScaleRepository.findById(1L)).thenReturn(Optional.of(scale));
        when(salaryScaleMapper.toResponse(scale)).thenReturn(response);

        SalaryScaleResponse result = salaryScaleService.getSalaryScaleById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("SCALE_DEV_L1");
        assertThat(result.getCalculatedSalary()).isEqualByComparingTo(BigDecimal.valueOf(18000000));
    }

    @Test
    @DisplayName("Get salary scale by non-existent ID throws RESOURCE_NOT_FOUND")
    void getSalaryScaleById_NotFound_ThrowsException() {
        when(salaryScaleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> salaryScaleService.getSalaryScaleById(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("responseCode", ResponseCode.RESOURCE_NOT_FOUND);
    }

    // ===================================================
    // GET ALL ACTIVE SALARY SCALES TESTS
    // ===================================================

    @Test
    @DisplayName("Get all active salary scales returns only active items")
    void getAllActiveSalaryScales_ReturnsFilteredList() {
        SalaryScale scale2 = SalaryScale.builder()
                .code("SS-DEV-02")
                .title("Kỹ Sư Cao Cấp")
                .coefficient(BigDecimal.valueOf(1.5))
                .active(true)
                .build();
        scale2.setId(2L);

        SalaryScaleResponse response2 = SalaryScaleResponse.builder()
                .id(2L).code("SS-DEV-02").active(true).build();

        when(salaryScaleRepository.findByActiveTrue()).thenReturn(List.of(scale, scale2));
        when(salaryScaleMapper.toResponse(scale)).thenReturn(response);
        when(salaryScaleMapper.toResponse(scale2)).thenReturn(response2);

        List<SalaryScaleResponse> result = salaryScaleService.getAllActiveSalaryScales();

        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.stream().allMatch(SalaryScaleResponse::getActive)).isTrue();
    }

    @Test
    @DisplayName("Calculated salary formula: baseSalary × coefficient is correct")
    void salaryScale_CalculatedSalary_FormulaVerification() {
        // baseSalary = 15_000_000, coefficient = 1.2 → calculatedSalary = 18_000_000
        assertThat(response.getCalculatedSalary())
                .isEqualByComparingTo(
                        response.getBaseSalary().multiply(BigDecimal.valueOf(1.2))
                );
    }
}

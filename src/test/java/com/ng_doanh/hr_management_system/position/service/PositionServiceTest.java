package com.ng_doanh.hr_management_system.position.service;

import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.department.repository.DepartmentRepository;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import com.ng_doanh.hr_management_system.position.dto.request.PositionCreateRequest;
import com.ng_doanh.hr_management_system.position.dto.request.PositionUpdateRequest;
import com.ng_doanh.hr_management_system.position.dto.response.PositionResponse;
import com.ng_doanh.hr_management_system.position.entity.Position;
import com.ng_doanh.hr_management_system.position.mapper.PositionMapper;
import com.ng_doanh.hr_management_system.position.repository.PositionRepository;
import com.ng_doanh.hr_management_system.position.service.impl.PositionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PositionService Unit Tests")
class PositionServiceTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PositionMapper positionMapper;

    @InjectMocks
    private PositionServiceImpl positionService;

    private Position position;
    private PositionResponse positionResponse;
    private PositionCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        position = Position.builder()
                .code("DEV-SR")
                .title("Senior Developer")
                .basicSalary(BigDecimal.valueOf(25000000))
                .minSalary(BigDecimal.valueOf(20000000))
                .maxSalary(BigDecimal.valueOf(35000000))
                .active(true)
                .build();
        position.setId(1L);

        positionResponse = PositionResponse.builder()
                .id(1L)
                .code("DEV-SR")
                .title("Senior Developer")
                .basicSalary(BigDecimal.valueOf(25000000))
                .minSalary(BigDecimal.valueOf(20000000))
                .maxSalary(BigDecimal.valueOf(35000000))
                .active(true)
                .build();

        createRequest = PositionCreateRequest.builder()
                .code("DEV-SR")
                .title("Senior Developer")
                .basicSalary(BigDecimal.valueOf(25000000))
                .minSalary(BigDecimal.valueOf(20000000))
                .maxSalary(BigDecimal.valueOf(35000000))
                .build();
    }

    @Test
    @DisplayName("Create position successfully when code does not exist")
    void createPosition_Success() {
        when(positionRepository.existsByCode("DEV-SR")).thenReturn(false);
        when(positionMapper.toEntity(createRequest)).thenReturn(position);
        when(positionRepository.save(any(Position.class))).thenReturn(position);
        when(positionMapper.toResponse(position)).thenReturn(positionResponse);
        when(employeeRepository.countByPositionId(1L)).thenReturn(0L);

        PositionResponse result = positionService.createPosition(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("DEV-SR");
        assertThat(result.getTitle()).isEqualTo("Senior Developer");
        verify(positionRepository).save(any(Position.class));
    }

    @Test
    @DisplayName("Create position auto-generates code when code is null or blank")
    void createPosition_AutoGenerateCode_Success() {
        PositionCreateRequest reqNoCode = PositionCreateRequest.builder()
                .title("Senior Developer")
                .basicSalary(BigDecimal.valueOf(25000000))
                .minSalary(BigDecimal.valueOf(20000000))
                .maxSalary(BigDecimal.valueOf(35000000))
                .build();

        when(positionRepository.count()).thenReturn(0L);
        when(positionRepository.existsByCode("POS-00001")).thenReturn(false);
        when(positionMapper.toEntity(reqNoCode)).thenReturn(position);
        when(positionRepository.save(any(Position.class))).thenReturn(position);
        when(positionMapper.toResponse(position)).thenReturn(positionResponse);
        when(employeeRepository.countByPositionId(1L)).thenReturn(0L);

        PositionResponse result = positionService.createPosition(reqNoCode);

        assertThat(result).isNotNull();
        verify(positionRepository).save(position);
        assertThat(position.getCode()).isEqualTo("POS-00001");
    }

    @Test
    @DisplayName("Create position throws duplicate exception when code exists")
    void createPosition_DuplicateCode() {
        when(positionRepository.existsByCode("DEV-SR")).thenReturn(true);

        assertThatThrownBy(() -> positionService.createPosition(createRequest))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Update position with duplicate new code throws DUPLICATE_RESOURCE")
    void updatePosition_DuplicateNewCode_ThrowsException() {
        PositionUpdateRequest updateRequest = PositionUpdateRequest.builder()
                .code("NEW_POS")
                .title("Senior Developer")
                .basicSalary(BigDecimal.valueOf(25000000))
                .build();

        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));
        when(positionRepository.existsByCode("NEW_POS")).thenReturn(true);

        assertThatThrownBy(() -> positionService.updatePosition(1L, updateRequest))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Update position with valid new code updates code successfully")
    void updatePosition_ValidNewCode_Success() {
        PositionUpdateRequest updateRequest = PositionUpdateRequest.builder()
                .code("NEW_POS")
                .title("Senior Developer")
                .basicSalary(BigDecimal.valueOf(25000000))
                .build();

        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));
        when(positionRepository.existsByCode("NEW_POS")).thenReturn(false);
        when(positionRepository.save(any(Position.class))).thenReturn(position);
        when(positionMapper.toResponse(position)).thenReturn(positionResponse);
        when(employeeRepository.countByPositionId(1L)).thenReturn(0L);

        PositionResponse result = positionService.updatePosition(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(position.getCode()).isEqualTo("NEW_POS");
    }

    @Test
    @DisplayName("Get position by ID successfully")
    void getPositionById_Success() {
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));
        when(positionMapper.toResponse(position)).thenReturn(positionResponse);
        when(employeeRepository.countByPositionId(1L)).thenReturn(0L);

        PositionResponse result = positionService.getPositionById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Delete position deletes position when no employees assigned")
    void deletePosition_Success() {
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));
        when(employeeRepository.countByPositionId(1L)).thenReturn(0L);

        positionService.deletePosition(1L);

        verify(positionRepository).delete(position);
    }
}

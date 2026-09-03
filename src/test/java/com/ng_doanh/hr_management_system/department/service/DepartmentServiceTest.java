package com.ng_doanh.hr_management_system.department.service;

import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.department.dto.request.DepartmentCreateRequest;
import com.ng_doanh.hr_management_system.department.dto.request.DepartmentUpdateRequest;
import com.ng_doanh.hr_management_system.department.dto.response.DepartmentResponse;
import com.ng_doanh.hr_management_system.department.entity.Department;
import com.ng_doanh.hr_management_system.department.mapper.DepartmentMapper;
import com.ng_doanh.hr_management_system.department.repository.DepartmentRepository;
import com.ng_doanh.hr_management_system.department.service.impl.DepartmentServiceImpl;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentService Unit Tests")
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department department;
    private DepartmentResponse departmentResponse;
    private DepartmentCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .code("IT")
                .name("Information Technology")
                .description("IT Department")
                .active(true)
                .build();
        department.setId(1L);

        departmentResponse = DepartmentResponse.builder()
                .id(1L)
                .code("IT")
                .name("Information Technology")
                .description("IT Department")
                .active(true)
                .build();

        createRequest = DepartmentCreateRequest.builder()
                .code("IT")
                .name("Information Technology")
                .description("IT Department")
                .build();
    }

    @Test
    @DisplayName("Create department successfully when code does not exist")
    void createDepartment_Success() {
        when(departmentRepository.existsByCode("IT")).thenReturn(false);
        when(departmentMapper.toEntity(createRequest)).thenReturn(department);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(departmentMapper.toResponse(department)).thenReturn(departmentResponse);
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(0L);

        DepartmentResponse result = departmentService.createDepartment(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("IT");
        assertThat(result.getName()).isEqualTo("Information Technology");
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    @DisplayName("Create department auto-generates code when code is null or blank")
    void createDepartment_AutoGenerateCode_Success() {
        DepartmentCreateRequest reqNoCode = DepartmentCreateRequest.builder()
                .name("Information Technology")
                .description("IT Department")
                .build();

        when(departmentRepository.count()).thenReturn(0L);
        when(departmentRepository.existsByCode("DEPT-00001")).thenReturn(false);
        when(departmentRepository.existsByName("Information Technology")).thenReturn(false);
        when(departmentMapper.toEntity(reqNoCode)).thenReturn(department);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(departmentMapper.toResponse(department)).thenReturn(departmentResponse);
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(0L);

        DepartmentResponse result = departmentService.createDepartment(reqNoCode);

        assertThat(result).isNotNull();
        verify(departmentRepository).save(department);
        assertThat(department.getCode()).isEqualTo("DEPT-00001");
    }

    @Test
    @DisplayName("Create department throws DuplicateResourceException when code exists")
    void createDepartment_DuplicateCode_ThrowsException() {
        when(departmentRepository.existsByCode("IT")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(createRequest))
                .isInstanceOf(BusinessException.class);

        verify(departmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get department by ID successfully")
    void getDepartmentById_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentMapper.toResponse(department)).thenReturn(departmentResponse);
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(0L);

        DepartmentResponse result = departmentService.getDepartmentById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Information Technology");
    }

    @Test
    @DisplayName("Get department by ID throws ResourceNotFoundException when not found")
    void getDepartmentById_NotFound_ThrowsException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getDepartmentById(99L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Update department successfully")
    void updateDepartment_Success() {
        DepartmentUpdateRequest updateRequest = DepartmentUpdateRequest.builder()
                .name("IT Software")
                .description("Updated IT Dept")
                .build();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(departmentMapper.toResponse(department)).thenReturn(departmentResponse);
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(0L);

        DepartmentResponse result = departmentService.updateDepartment(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(departmentMapper).updateEntityFromRequest(updateRequest, department);
        verify(departmentRepository).save(department);
    }

    @Test
    @DisplayName("Update department with duplicate new code throws DUPLICATE_RESOURCE")
    void updateDepartment_DuplicateNewCode_ThrowsException() {
        DepartmentUpdateRequest updateRequest = DepartmentUpdateRequest.builder()
                .code("NEW_IT")
                .name("Information Technology")
                .build();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByCode("NEW_IT")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.updateDepartment(1L, updateRequest))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Update department with valid new code updates code successfully")
    void updateDepartment_ValidNewCode_Success() {
        DepartmentUpdateRequest updateRequest = DepartmentUpdateRequest.builder()
                .code("NEW_IT")
                .name("Information Technology")
                .build();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByCode("NEW_IT")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(departmentMapper.toResponse(department)).thenReturn(departmentResponse);
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(0L);

        DepartmentResponse result = departmentService.updateDepartment(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(department.getCode()).isEqualTo("NEW_IT");
    }

    @Test
    @DisplayName("Delete department successfully")
    void deleteDepartment_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.findByParentDepartmentId(1L)).thenReturn(List.of());

        departmentService.deleteDepartment(1L);

        verify(departmentRepository).delete(department);
    }

    // ===================================================
    // GET BY CODE TESTS
    // ===================================================

    @Test
    @DisplayName("Get department by code successfully returns correct department")
    void getDepartmentByCode_Success() {
        when(departmentRepository.findByCode("IT")).thenReturn(Optional.of(department));
        when(departmentMapper.toResponse(department)).thenReturn(departmentResponse);
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(5L);

        DepartmentResponse result = departmentService.getDepartmentByCode("IT");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("IT");
        assertThat(result.getName()).isEqualTo("Information Technology");
    }

    @Test
    @DisplayName("Get department by non-existent code throws RESOURCE_NOT_FOUND")
    void getDepartmentByCode_NotFound_ThrowsException() {
        when(departmentRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getDepartmentByCode("UNKNOWN"))
                .isInstanceOf(BusinessException.class);
    }

    // ===================================================
    // GET ALL ACTIVE DEPARTMENTS TESTS
    // ===================================================

    @Test
    @DisplayName("Get all active departments returns only departments with active=true")
    void getAllActiveDepartments_ReturnsOnlyActive() {
        Department activeDept2 = Department.builder()
                .code("HR")
                .name("Human Resources")
                .active(true)
                .build();
        activeDept2.setId(2L);

        DepartmentResponse hr = DepartmentResponse.builder()
                .id(2L).code("HR").name("Human Resources").active(true).build();

        when(departmentRepository.findByActiveTrue()).thenReturn(List.of(department, activeDept2));
        when(departmentMapper.toResponse(department)).thenReturn(departmentResponse);
        when(departmentMapper.toResponse(activeDept2)).thenReturn(hr);
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(0L);
        when(employeeRepository.countByDepartmentId(2L)).thenReturn(3L);

        List<DepartmentResponse> result = departmentService.getAllActiveDepartments();

        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.stream().allMatch(DepartmentResponse::isActive)).isTrue();
    }

    // ===================================================
    // SEARCH DEPARTMENTS TESTS
    // ===================================================

    @Test
    @DisplayName("Search departments with keyword returns paginated results")
    void searchDepartments_WithKeyword_ReturnsPaged() {
        Page<Department> page = new PageImpl<>(List.of(department));

        when(departmentRepository.searchDepartments(eq("IT"), isNull(), any(Pageable.class)))
                .thenReturn(page);
        when(departmentMapper.toResponse(department)).thenReturn(departmentResponse);
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(0L);

        Page<DepartmentResponse> result = departmentService.searchDepartments("IT", null, PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo("IT");
    }

    // ===================================================
    // CREATE DUPLICATE NAME TEST
    // ===================================================

    @Test
    @DisplayName("Create department throws DuplicateResourceException when name already exists")
    void createDepartment_DuplicateName_ThrowsException() {
        DepartmentCreateRequest requestWithDuplicateName = DepartmentCreateRequest.builder()
                .code("NEW_CODE")
                .name("Information Technology") // same name
                .description("Duplicate name test")
                .build();

        when(departmentRepository.existsByCode("NEW_CODE")).thenReturn(false);
        when(departmentRepository.existsByName("Information Technology")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(requestWithDuplicateName))
                .isInstanceOf(BusinessException.class);

        verify(departmentRepository, never()).save(any());
    }
}

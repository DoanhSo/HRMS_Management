package com.ng_doanh.hr_management_system.employee.service;

import com.ng_doanh.hr_management_system.auth.repository.UserRepository;
import com.ng_doanh.hr_management_system.common.enums.Gender;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.department.repository.DepartmentRepository;
import com.ng_doanh.hr_management_system.employee.dto.request.EmployeeCreateRequest;
import com.ng_doanh.hr_management_system.employee.dto.request.EmployeeUpdateRequest;
import com.ng_doanh.hr_management_system.employee.dto.response.EmployeeResponse;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.enums.EmploymentStatus;
import com.ng_doanh.hr_management_system.employee.mapper.EmployeeMapper;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import com.ng_doanh.hr_management_system.employee.service.impl.EmployeeServiceImpl;
import com.ng_doanh.hr_management_system.position.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Unit Tests")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;
    private EmployeeResponse employeeResponse;
    private EmployeeCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .employeeCode("EMP-00001")
                .firstName("Nguyen")
                .lastName("Van A")
                .gender(Gender.MALE)
                .hireDate(LocalDate.of(2025, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
        employee.setId(1L);

        employeeResponse = EmployeeResponse.builder()
                .id(1L)
                .employeeCode("EMP-00001")
                .firstName("Nguyen")
                .lastName("Van A")
                .fullName("Nguyen Van A")
                .gender(Gender.MALE)
                .hireDate(LocalDate.of(2025, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();

        createRequest = EmployeeCreateRequest.builder()
                .firstName("Nguyen")
                .lastName("Van A")
                .gender(Gender.MALE)
                .hireDate(LocalDate.of(2025, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Create employee successfully")
    void createEmployee_Success() {
        when(employeeMapper.toEntity(createRequest)).thenReturn(employee);
        when(employeeRepository.count()).thenReturn(0L);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.createEmployee(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getEmployeeCode()).isEqualTo("EMP-00001");
        assertThat(result.getFirstName()).isEqualTo("Nguyen");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create employee with custom code successfully when code does not exist")
    void createEmployee_CustomCode_Success() {
        EmployeeCreateRequest customRequest = EmployeeCreateRequest.builder()
                .employeeCode("EMP-99999")
                .firstName("Nguyen")
                .lastName("Van A")
                .gender(Gender.MALE)
                .hireDate(LocalDate.of(2025, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();

        when(employeeRepository.existsByEmployeeCode("EMP-99999")).thenReturn(false);
        when(employeeMapper.toEntity(customRequest)).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.createEmployee(customRequest);

        assertThat(result).isNotNull();
        verify(employeeRepository).save(employee);
        assertThat(employee.getEmployeeCode()).isEqualTo("EMP-99999");
    }

    @Test
    @DisplayName("Create employee with custom code throws DUPLICATE_RESOURCE when code exists")
    void createEmployee_DuplicateCustomCode_ThrowsException() {
        EmployeeCreateRequest customRequest = EmployeeCreateRequest.builder()
                .employeeCode("EMP-99999")
                .firstName("Nguyen")
                .lastName("Van A")
                .gender(Gender.MALE)
                .hireDate(LocalDate.of(2025, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();

        when(employeeRepository.existsByEmployeeCode("EMP-99999")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(customRequest))
                .isInstanceOf(BusinessException.class);

        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get employee by ID successfully")
    void getEmployeeById_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.getEmployeeById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmployeeCode()).isEqualTo("EMP-00001");
    }

    @Test
    @DisplayName("Get employee by ID throws exception when not found")
    void getEmployeeById_NotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Update employee details successfully")
    void updateEmployee_Success() {
        EmployeeUpdateRequest updateRequest = EmployeeUpdateRequest.builder()
                .firstName("Nguyen")
                .lastName("Van B")
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.updateEmployee(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(employeeMapper).updateEntityFromRequest(updateRequest, employee);
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("Update employee with duplicate new code throws DUPLICATE_RESOURCE")
    void updateEmployee_DuplicateNewCode_ThrowsException() {
        EmployeeUpdateRequest updateRequest = EmployeeUpdateRequest.builder()
                .employeeCode("EMP-88888")
                .firstName("Nguyen")
                .lastName("Van B")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmployeeCode("EMP-88888")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, updateRequest))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Update employee with valid new code updates code successfully")
    void updateEmployee_ValidNewCode_Success() {
        EmployeeUpdateRequest updateRequest = EmployeeUpdateRequest.builder()
                .employeeCode("EMP-88888")
                .firstName("Nguyen")
                .lastName("Van B")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmployeeCode("EMP-88888")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.updateEmployee(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(employee.getEmployeeCode()).isEqualTo("EMP-88888");
    }

    @Test
    @DisplayName("Delete employee soft-deletes and marks as TERMINATED")
    void deleteEmployee_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(1L);

        assertThat(employee.getEmploymentStatus()).isEqualTo(EmploymentStatus.TERMINATED);
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("Search employees with multi-attribute filtering successfully")
    void searchEmployees_Success() {
        org.springframework.data.domain.Page<Employee> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(employee));
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        when(employeeRepository.searchEmployees("Nguyen", 1L, 2L, EmploymentStatus.ACTIVE, Gender.MALE, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), pageable))
                .thenReturn(page);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        org.springframework.data.domain.Page<EmployeeResponse> result = employeeService.searchEmployees(
                "Nguyen", 1L, 2L, EmploymentStatus.ACTIVE, Gender.MALE, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), pageable
        );

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    // ===================================================
    // GET EMPLOYEE BY CODE TESTS
    // ===================================================

    @Test
    @DisplayName("Get employee by code successfully returns correct employee")
    void getEmployeeByCode_Success() {
        when(employeeRepository.findByEmployeeCode("EMP-00001")).thenReturn(java.util.Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.getEmployeeByCode("EMP-00001");

        assertThat(result).isNotNull();
        assertThat(result.getEmployeeCode()).isEqualTo("EMP-00001");
    }

    @Test
    @DisplayName("Get employee by non-existent code throws RESOURCE_NOT_FOUND")
    void getEmployeeByCode_NotFound_ThrowsException() {
        when(employeeRepository.findByEmployeeCode("GHOST-999")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeByCode("GHOST-999"))
                .isInstanceOf(com.ng_doanh.hr_management_system.common.exception.BusinessException.class);
    }

    // ===================================================
    // GET EMPLOYEE BY USER ID TESTS
    // ===================================================

    @Test
    @DisplayName("Get employee by userId successfully returns linked employee profile")
    void getEmployeeByUserId_Success() {
        when(employeeRepository.findByUserId(10L)).thenReturn(java.util.Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.getEmployeeByUserId(10L);

        assertThat(result).isNotNull();
        assertThat(result.getEmployeeCode()).isEqualTo("EMP-00001");
    }

    @Test
    @DisplayName("Get employee by userId with no linked employee throws RESOURCE_NOT_FOUND")
    void getEmployeeByUserId_NotFound_ThrowsException() {
        when(employeeRepository.findByUserId(999L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeByUserId(999L))
                .isInstanceOf(com.ng_doanh.hr_management_system.common.exception.BusinessException.class);
    }

    // ===================================================
    // CREATE EMPLOYEE — DUPLICATE USER LINK TEST
    // ===================================================

    @Test
    @DisplayName("Create employee throws DUPLICATE_RESOURCE when userId is already linked to another employee")
    void createEmployee_DuplicateUserId_ThrowsException() {
        com.ng_doanh.hr_management_system.auth.entity.User linkedUser =
                com.ng_doanh.hr_management_system.auth.entity.User.builder()
                        .username("existinguser")
                        .build();
        linkedUser.setId(99L);

        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
                .firstName("Duplicate")
                .lastName("User")
                .userId(99L)
                .build();

        when(userRepository.findById(99L)).thenReturn(java.util.Optional.of(linkedUser));
        when(employeeRepository.findByUserId(99L)).thenReturn(java.util.Optional.of(employee));

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(com.ng_doanh.hr_management_system.common.exception.BusinessException.class);

        verify(employeeRepository, never()).save(any());
    }
}

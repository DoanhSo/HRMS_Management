package com.ng_doanh.hr_management_system.department.service;

import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.department.dto.request.DepartmentCreateRequest;
import com.ng_doanh.hr_management_system.department.dto.response.DepartmentResponse;
import com.ng_doanh.hr_management_system.department.entity.Department;
import com.ng_doanh.hr_management_system.department.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("Department Integration Tests (H2 In-Memory DB)")
class DepartmentIntegrationTest {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        departmentRepository.deleteAll();
    }

    @Test
    @DisplayName("Create department persists to H2 database and returns response")
    void createDepartment_PersistsToDb() {
        DepartmentCreateRequest request = DepartmentCreateRequest.builder()
                .code("INTEG_IT")
                .name("Phòng Công Nghệ Tích Hợp")
                .description("Phòng kiểm thử tích hợp")
                .build();

        DepartmentResponse response = departmentService.createDepartment(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCode()).isEqualTo("INTEG_IT");

        // Verify direct DB query
        Department foundInDb = departmentRepository.findById(response.getId()).orElse(null);
        assertThat(foundInDb).isNotNull();
        assertThat(foundInDb.getName()).isEqualTo("Phòng Công Nghệ Tích Hợp");
    }

    @Test
    @DisplayName("Create department with duplicate code throws exception and does not corrupt DB")
    void createDepartment_DuplicateCode_ThrowsException() {
        Department dept1 = Department.builder()
                .code("DUP_CODE")
                .name("Phòng 1")
                .active(true)
                .build();
        departmentRepository.save(dept1);

        DepartmentCreateRequest duplicateRequest = DepartmentCreateRequest.builder()
                .code("DUP_CODE")
                .name("Phòng 2")
                .build();

        assertThatThrownBy(() -> departmentService.createDepartment(duplicateRequest))
                .isInstanceOf(BusinessException.class);

        assertThat(departmentRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Get all active departments filters out inactive records from DB")
    void getAllActiveDepartments_FiltersInactive() {
        Department activeDept = Department.builder()
                .code("ACTIVE_01")
                .name("Active Dept")
                .active(true)
                .build();
        Department inactiveDept = Department.builder()
                .code("INACTIVE_01")
                .name("Inactive Dept")
                .active(false)
                .build();

        departmentRepository.saveAll(List.of(activeDept, inactiveDept));

        List<DepartmentResponse> activeList = departmentService.getAllActiveDepartments();

        assertThat(activeList).hasSize(1);
        assertThat(activeList.get(0).getCode()).isEqualTo("ACTIVE_01");
    }

    @Test
    @DisplayName("Delete department removes record from database")
    void deleteDepartment_RemovesFromDb() {
        Department dept = Department.builder()
                .code("DEL_DEPT")
                .name("To Delete")
                .active(true)
                .build();
        Department saved = departmentRepository.save(dept);

        departmentService.deleteDepartment(saved.getId());

        assertThat(departmentRepository.findById(saved.getId())).isEmpty();
    }
}

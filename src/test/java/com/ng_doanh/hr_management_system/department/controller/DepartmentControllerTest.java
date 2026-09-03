package com.ng_doanh.hr_management_system.department.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ng_doanh.hr_management_system.department.dto.request.DepartmentCreateRequest;
import com.ng_doanh.hr_management_system.department.dto.response.DepartmentResponse;
import com.ng_doanh.hr_management_system.department.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentController Web/Endpoint Tests")
class DepartmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private DepartmentController departmentController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(departmentController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/v1/departments creates department and returns 1001 CREATED")
    void createDepartment_Success() throws Exception {
        DepartmentCreateRequest request = DepartmentCreateRequest.builder()
                .code("IT")
                .name("Công Nghệ Thông Tin")
                .description("Phòng IT")
                .build();

        DepartmentResponse response = DepartmentResponse.builder()
                .id(1L)
                .code("IT")
                .name("Công Nghệ Thông Tin")
                .employeeCount(0L)
                .active(true)
                .build();

        when(departmentService.createDepartment(any(DepartmentCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.data.code").value("IT"))
                .andExpect(jsonPath("$.data.name").value("Công Nghệ Thông Tin"));
    }

    @Test
    @DisplayName("GET /api/v1/departments/active returns list of active departments")
    void getAllActiveDepartments_Success() throws Exception {
        DepartmentResponse response = DepartmentResponse.builder()
                .id(1L)
                .code("IT")
                .name("Công Nghệ Thông Tin")
                .active(true)
                .build();

        when(departmentService.getAllActiveDepartments()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/departments/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data[0].code").value("IT"));
    }

    @Test
    @DisplayName("DELETE /api/v1/departments/{id} deletes department and returns 1003 DELETED")
    void deleteDepartment_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));

        verify(departmentService).deleteDepartment(1L);
    }
}

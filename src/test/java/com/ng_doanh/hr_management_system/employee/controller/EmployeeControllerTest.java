package com.ng_doanh.hr_management_system.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ng_doanh.hr_management_system.employee.dto.request.EmployeeCreateRequest;
import com.ng_doanh.hr_management_system.employee.dto.response.EmployeeResponse;
import com.ng_doanh.hr_management_system.employee.enums.EmploymentStatus;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import com.ng_doanh.hr_management_system.employee.service.EmployeeService;
import com.ng_doanh.hr_management_system.report.service.ExcelExportService;
import com.ng_doanh.hr_management_system.report.service.ExcelImportService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeController Web/Endpoint Tests")
class EmployeeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ExcelExportService excelExportService;

    @Mock
    private ExcelImportService excelImportService;

    @InjectMocks
    private EmployeeController employeeController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/v1/employees creates employee and returns 1001 CREATED")
    void createEmployee_Success() throws Exception {
        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
                .firstName("Van A")
                .lastName("Nguyen")
                .hireDate(java.time.LocalDate.of(2025, 1, 1))
                .phone("0987654321")
                .build();

        EmployeeResponse response = EmployeeResponse.builder()
                .id(1L)
                .employeeCode("EMP-00001")
                .firstName("Van A")
                .lastName("Nguyen")
                .hireDate(java.time.LocalDate.of(2025, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();

        when(employeeService.createEmployee(any(EmployeeCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP-00001"))
                .andExpect(jsonPath("$.data.firstName").value("Van A"));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} returns employee details with code 1000")
    void getEmployeeById_Success() throws Exception {
        EmployeeResponse response = EmployeeResponse.builder()
                .id(1L)
                .employeeCode("EMP-00001")
                .firstName("Van A")
                .lastName("Nguyen")
                .build();

        when(employeeService.getEmployeeById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP-00001"));
    }

    @Test
    @DisplayName("DELETE /api/v1/employees/{id} soft-deletes employee and returns 1003 DELETED")
    void deleteEmployee_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));

        verify(employeeService).deleteEmployee(1L);
    }
}

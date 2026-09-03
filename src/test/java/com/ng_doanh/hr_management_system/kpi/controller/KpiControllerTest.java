package com.ng_doanh.hr_management_system.kpi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ng_doanh.hr_management_system.kpi.dto.request.KpiCriteriaCreateRequest;
import com.ng_doanh.hr_management_system.kpi.dto.response.KpiCriteriaResponse;
import com.ng_doanh.hr_management_system.kpi.dto.response.KpiEvaluationResponse;
import com.ng_doanh.hr_management_system.kpi.enums.KpiEvaluationStatus;
import com.ng_doanh.hr_management_system.kpi.enums.KpiRating;
import com.ng_doanh.hr_management_system.kpi.service.KpiService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("KpiController Web/Endpoint Tests")
class KpiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private KpiService kpiService;

    @InjectMocks
    private KpiController kpiController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(kpiController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/v1/kpi/criteria creates KPI criteria and returns 1001 CREATED")
    void createCriteria_Success() throws Exception {
        KpiCriteriaCreateRequest request = KpiCriteriaCreateRequest.builder()
                .code("KPI_QUALITY")
                .name("Chất lượng công việc")
                .weight(40)
                .build();

        KpiCriteriaResponse response = KpiCriteriaResponse.builder()
                .id(1L)
                .code("KPI_QUALITY")
                .name("Chất lượng công việc")
                .weight(40)
                .active(true)
                .build();

        when(kpiService.createCriteria(any(KpiCriteriaCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/kpi/criteria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.data.code").value("KPI_QUALITY"))
                .andExpect(jsonPath("$.data.weight").value(40));
    }

    @Test
    @DisplayName("GET /api/v1/kpi/criteria/active returns list of active criteria with code 1000")
    void getAllActiveCriteria_Success() throws Exception {
        KpiCriteriaResponse response = KpiCriteriaResponse.builder()
                .id(1L)
                .code("KPI_QUALITY")
                .name("Chất lượng công việc")
                .weight(50)
                .active(true)
                .build();

        when(kpiService.getAllActiveCriteria()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/kpi/criteria/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data[0].code").value("KPI_QUALITY"));
    }

    @Test
    @DisplayName("DELETE /api/v1/kpi/criteria/{id} deletes criteria and returns 1003 DELETED")
    void deleteCriteria_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/kpi/criteria/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));

        verify(kpiService).deleteCriteria(1L);
    }

    @Test
    @DisplayName("GET /api/v1/kpi/evaluations/{id} returns evaluation detail with code 1000")
    void getEvaluationById_Success() throws Exception {
        KpiEvaluationResponse response = KpiEvaluationResponse.builder()
                .id(10L)
                .employeeId(1L)
                .employeeCode("EMP-00001")
                .totalScore(BigDecimal.valueOf(95.0))
                .rating(KpiRating.A)
                .kpiCoefficient(BigDecimal.valueOf(1.5))
                .bonusAmount(BigDecimal.valueOf(10000000))
                .status(KpiEvaluationStatus.APPROVED)
                .build();

        when(kpiService.getEvaluationById(10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/kpi/evaluations/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP-00001"))
                .andExpect(jsonPath("$.data.rating").value("A"))
                .andExpect(jsonPath("$.data.bonusAmount").value(10000000));
    }
}

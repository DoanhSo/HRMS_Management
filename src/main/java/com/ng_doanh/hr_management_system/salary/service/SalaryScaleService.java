package com.ng_doanh.hr_management_system.salary.service;

import com.ng_doanh.hr_management_system.salary.dto.request.SalaryScaleCreateRequest;
import com.ng_doanh.hr_management_system.salary.dto.request.SalaryScaleUpdateRequest;
import com.ng_doanh.hr_management_system.salary.dto.response.SalaryScaleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SalaryScaleService {

    SalaryScaleResponse createSalaryScale(SalaryScaleCreateRequest request);

    SalaryScaleResponse updateSalaryScale(Long id, SalaryScaleUpdateRequest request);

    void deleteSalaryScale(Long id);

    SalaryScaleResponse getSalaryScaleById(Long id);

    List<SalaryScaleResponse> getAllActiveSalaryScales();

    Page<SalaryScaleResponse> searchSalaryScales(String keyword, Long positionId, Boolean active, Pageable pageable);
}

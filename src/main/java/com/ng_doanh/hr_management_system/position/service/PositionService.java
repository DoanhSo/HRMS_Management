package com.ng_doanh.hr_management_system.position.service;

import com.ng_doanh.hr_management_system.position.dto.request.PositionCreateRequest;
import com.ng_doanh.hr_management_system.position.dto.request.PositionUpdateRequest;
import com.ng_doanh.hr_management_system.position.dto.response.PositionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PositionService {

    PositionResponse createPosition(PositionCreateRequest request);

    PositionResponse getPositionById(Long id);

    PositionResponse getPositionByCode(String code);

    List<PositionResponse> getAllActivePositions();

    List<PositionResponse> getPositionsByDepartmentId(Long departmentId);

    Page<PositionResponse> searchPositions(String keyword, Long departmentId, Boolean active, Pageable pageable);

    PositionResponse updatePosition(Long id, PositionUpdateRequest request);

    void deletePosition(Long id);
}

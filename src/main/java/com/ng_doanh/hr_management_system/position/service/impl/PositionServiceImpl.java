package com.ng_doanh.hr_management_system.position.service.impl;

import com.ng_doanh.hr_management_system.audit.annotation.Audited;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.common.util.CodeGeneratorUtil;
import com.ng_doanh.hr_management_system.department.entity.Department;
import com.ng_doanh.hr_management_system.department.repository.DepartmentRepository;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import com.ng_doanh.hr_management_system.position.dto.request.PositionCreateRequest;
import com.ng_doanh.hr_management_system.position.dto.request.PositionUpdateRequest;
import com.ng_doanh.hr_management_system.position.dto.response.PositionResponse;
import com.ng_doanh.hr_management_system.position.entity.Position;
import com.ng_doanh.hr_management_system.position.mapper.PositionMapper;
import com.ng_doanh.hr_management_system.position.repository.PositionRepository;
import com.ng_doanh.hr_management_system.position.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionMapper positionMapper;

    @Override
    @Transactional
    @Audited(action = "CREATE_POSITION", entity = "Position")
    @CacheEvict(value = {"activePositions", "departmentPositions", "dashboardSummary"}, allEntries = true)
    public PositionResponse createPosition(PositionCreateRequest request) {
        log.info("Creating new position: {}", request.getTitle());

        String code;
        if (request.getCode() == null || request.getCode().isBlank()) {
            code = CodeGeneratorUtil.generateCode("POS-", 5, positionRepository.count(), positionRepository::existsByCode);
        } else {
            code = request.getCode().trim();
            if (positionRepository.existsByCode(code)) {
                throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
            }
        }

        // Salary range validation: minSalary <= basicSalary <= maxSalary
        validateSalaryRange(request.getMinSalary(), request.getBasicSalary(), request.getMaxSalary());

        Position position = positionMapper.toEntity(request);
        position.setCode(code);

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
            position.setDepartment(department);
        }

        Position savedPosition = positionRepository.save(position);
        log.info("Position created successfully with ID: {}", savedPosition.getId());

        return mapToResponseWithEmployeeCount(savedPosition);
    }

    @Override
    @Transactional(readOnly = true)
    public PositionResponse getPositionById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        return mapToResponseWithEmployeeCount(position);
    }

    @Override
    @Transactional(readOnly = true)
    public PositionResponse getPositionByCode(String code) {
        Position position = positionRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        return mapToResponseWithEmployeeCount(position);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponse> getAllActivePositions() {
        log.info("Fetching active positions from Database");
        List<Position> positions = positionRepository.findByActiveTrue();
        return positions.stream()
                .map(this::mapToResponseWithEmployeeCount)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponse> getPositionsByDepartmentId(Long departmentId) {
        log.info("Fetching positions for department ID {} from Database", departmentId);
        List<Position> positions = positionRepository.findByDepartmentId(departmentId);
        return positions.stream()
                .map(this::mapToResponseWithEmployeeCount)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PositionResponse> searchPositions(String keyword, Long departmentId, Boolean active, Pageable pageable) {
        Page<Position> positions = positionRepository.searchPositions(keyword, departmentId, active, pageable);
        return positions.map(this::mapToResponseWithEmployeeCount);
    }

    @Override
    @Transactional
    @Audited(action = "UPDATE_POSITION", entity = "Position")
    @CacheEvict(value = {"activePositions", "departmentPositions", "dashboardSummary"}, allEntries = true)
    public PositionResponse updatePosition(Long id, PositionUpdateRequest request) {
        log.info("Updating position with ID: {}", id);

        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        // Salary range validation
        validateSalaryRange(request.getMinSalary(), request.getBasicSalary(), request.getMaxSalary());

        if (request.getCode() != null && !request.getCode().isBlank()) {
            String newCode = request.getCode().trim();
            if (!newCode.equalsIgnoreCase(position.getCode())) {
                if (positionRepository.existsByCode(newCode)) {
                    throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
                }
                position.setCode(newCode);
            }
        }

        positionMapper.updateEntityFromRequest(request, position);

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
            position.setDepartment(department);
        } else {
            position.setDepartment(null);
        }

        if (request.getActive() != null) {
            position.setActive(request.getActive());
        }

        Position updatedPosition = positionRepository.save(position);
        log.info("Position updated successfully with ID: {}", updatedPosition.getId());

        return mapToResponseWithEmployeeCount(updatedPosition);
    }

    @Override
    @Transactional
    @Audited(action = "DELETE_POSITION", entity = "Position")
    @CacheEvict(value = {"activePositions", "departmentPositions", "dashboardSummary"}, allEntries = true)
    public void deletePosition(Long id) {
        log.info("Deleting position with ID: {}", id);

        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        // Guard against deleting position assigned to active employees
        long assignedEmployeesCount = employeeRepository.countByPositionId(id);
        if (assignedEmployeesCount > 0) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }

        positionRepository.delete(position);
        log.info("Position deleted successfully with ID: {}", id);
    }

    private void validateSalaryRange(java.math.BigDecimal minSalary, java.math.BigDecimal basicSalary, java.math.BigDecimal maxSalary) {
        if (minSalary != null && basicSalary != null && basicSalary.compareTo(minSalary) < 0) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }
        if (maxSalary != null && basicSalary != null && basicSalary.compareTo(maxSalary) > 0) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }
        if (minSalary != null && maxSalary != null && minSalary.compareTo(maxSalary) > 0) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }
    }

    private PositionResponse mapToResponseWithEmployeeCount(Position position) {
        PositionResponse response = positionMapper.toResponse(position);
        long count = employeeRepository.countByPositionId(position.getId());
        response.setEmployeeCount(count);
        return response;
    }
}

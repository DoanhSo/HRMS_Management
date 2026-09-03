package com.ng_doanh.hr_management_system.department.service.impl;

import com.ng_doanh.hr_management_system.audit.annotation.Audited;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.common.util.CodeGeneratorUtil;
import com.ng_doanh.hr_management_system.department.dto.request.DepartmentCreateRequest;
import com.ng_doanh.hr_management_system.department.dto.request.DepartmentUpdateRequest;
import com.ng_doanh.hr_management_system.department.dto.response.DepartmentResponse;
import com.ng_doanh.hr_management_system.department.entity.Department;
import com.ng_doanh.hr_management_system.department.mapper.DepartmentMapper;
import com.ng_doanh.hr_management_system.department.repository.DepartmentRepository;
import com.ng_doanh.hr_management_system.department.service.DepartmentService;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
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
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    @Audited(action = "CREATE_DEPARTMENT", entity = "Department")
    @CacheEvict(value = {"activeDepartments", "departmentStats", "dashboardSummary"}, allEntries = true)
    public DepartmentResponse createDepartment(DepartmentCreateRequest request) {
        log.info("Creating new department: {}", request.getName());

        String code;
        if (request.getCode() == null || request.getCode().isBlank()) {
            code = CodeGeneratorUtil.generateCode("DEPT-", 5, departmentRepository.count(), departmentRepository::existsByCode);
        } else {
            code = request.getCode().trim();
            if (departmentRepository.existsByCode(code)) {
                throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
            }
        }

        if (departmentRepository.existsByName(request.getName())) {
            throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
        }

        Department department = departmentMapper.toEntity(request);
        department.setCode(code);

        // Link Manager if managerId provided
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
            department.setManager(manager);
        }

        // Link Parent Department if parentDepartmentId provided
        if (request.getParentDepartmentId() != null) {
            Department parent = departmentRepository.findById(request.getParentDepartmentId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
            department.setParentDepartment(parent);
        }

        Department savedDepartment = departmentRepository.save(department);
        log.info("Department created successfully with ID: {}", savedDepartment.getId());

        return mapToResponseWithEmployeeCount(savedDepartment);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        return mapToResponseWithEmployeeCount(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentByCode(String code) {
        Department department = departmentRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        return mapToResponseWithEmployeeCount(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllActiveDepartments() {
        log.info("Fetching active departments from Database");
        List<Department> activeDepartments = departmentRepository.findByActiveTrue();
        return activeDepartments.stream()
                .map(this::mapToResponseWithEmployeeCount)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> searchDepartments(String keyword, Boolean active, Pageable pageable) {
        Page<Department> departmentPage = departmentRepository.searchDepartments(keyword, active, pageable);
        return departmentPage.map(this::mapToResponseWithEmployeeCount);
    }

    @Override
    @Transactional
    @Audited(action = "UPDATE_DEPARTMENT", entity = "Department")
    @CacheEvict(value = {"activeDepartments", "departmentStats", "dashboardSummary"}, allEntries = true)
    public DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest request) {
        log.info("Updating department with ID: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        // Name duplicate check if name changed
        if (!department.getName().equalsIgnoreCase(request.getName()) && departmentRepository.existsByName(request.getName())) {
            throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
        }

        // Code duplicate check if code changed
        if (request.getCode() != null && !request.getCode().isBlank()) {
            String newCode = request.getCode().trim();
            if (!newCode.equalsIgnoreCase(department.getCode())) {
                if (departmentRepository.existsByCode(newCode)) {
                    throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
                }
                department.setCode(newCode);
            }
        }

        departmentMapper.updateEntityFromRequest(request, department);

        // Update Manager
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
            department.setManager(manager);
        } else {
            department.setManager(null);
        }

        // Update Parent Department
        if (request.getParentDepartmentId() != null) {
            if (request.getParentDepartmentId().equals(id)) {
                throw new BusinessException(ResponseCode.BAD_REQUEST);
            }
            Department parent = departmentRepository.findById(request.getParentDepartmentId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
            department.setParentDepartment(parent);
        } else {
            department.setParentDepartment(null);
        }

        if (request.getActive() != null) {
            department.setActive(request.getActive());
        }

        Department updatedDepartment = departmentRepository.save(department);
        log.info("Department updated successfully with ID: {}", updatedDepartment.getId());

        return mapToResponseWithEmployeeCount(updatedDepartment);
    }

    @Override
    @Transactional
    @Audited(action = "DELETE_DEPARTMENT", entity = "Department")
    @CacheEvict(value = {"activeDepartments", "departmentStats", "dashboardSummary"}, allEntries = true)
    public void deleteDepartment(Long id) {
        log.info("Deleting department with ID: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        // Check if child departments exist
        List<Department> children = departmentRepository.findByParentDepartmentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }

        departmentRepository.delete(department);
        log.info("Department deleted successfully with ID: {}", id);
    }

    private DepartmentResponse mapToResponseWithEmployeeCount(Department department) {
        DepartmentResponse response = departmentMapper.toResponse(department);
        long count = employeeRepository.countByDepartmentId(department.getId());
        response.setEmployeeCount(count);
        return response;
    }
}

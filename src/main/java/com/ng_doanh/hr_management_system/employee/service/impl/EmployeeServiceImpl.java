package com.ng_doanh.hr_management_system.employee.service.impl;

import com.ng_doanh.hr_management_system.audit.annotation.Audited;
import com.ng_doanh.hr_management_system.auth.entity.User;
import com.ng_doanh.hr_management_system.auth.repository.UserRepository;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.common.util.CodeGeneratorUtil;
import com.ng_doanh.hr_management_system.department.repository.DepartmentRepository;
import com.ng_doanh.hr_management_system.employee.dto.request.EmployeeCreateRequest;
import com.ng_doanh.hr_management_system.employee.dto.request.EmployeeUpdateRequest;
import com.ng_doanh.hr_management_system.employee.dto.response.EmployeeResponse;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.enums.EmploymentStatus;
import com.ng_doanh.hr_management_system.employee.mapper.EmployeeMapper;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import com.ng_doanh.hr_management_system.employee.service.EmployeeService;
import com.ng_doanh.hr_management_system.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    @Audited(action = "CREATE_EMPLOYEE", entity = "Employee")
    @CacheEvict(value = {"dashboardSummary", "departmentStats", "activeDepartments"}, allEntries = true)
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        log.info("Creating new employee: {} {}", request.getFirstName(), request.getLastName());

        if (request.getDepartmentId() != null && !departmentRepository.existsById(request.getDepartmentId())) {
            throw new BusinessException(ResponseCode.RESOURCE_NOT_FOUND);
        }

        if (request.getPositionId() != null && !positionRepository.existsById(request.getPositionId())) {
            throw new BusinessException(ResponseCode.RESOURCE_NOT_FOUND);
        }

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

            if (employeeRepository.findByUserId(request.getUserId()).isPresent()) {
                throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
            }
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
        }

        String employeeCode;
        if (request.getEmployeeCode() == null || request.getEmployeeCode().isBlank()) {
            employeeCode = CodeGeneratorUtil.generateCode("EMP-", 5, employeeRepository.count(), employeeRepository::existsByEmployeeCode);
        } else {
            employeeCode = request.getEmployeeCode().trim();
            if (employeeRepository.existsByEmployeeCode(employeeCode)) {
                throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
            }
        }

        Employee employee = employeeMapper.toEntity(request);
        employee.setEmployeeCode(employeeCode);
        employee.setUser(user);
        employee.setManager(manager);

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee created successfully with code: {}", savedEmployee.getEmployeeCode());

        return employeeMapper.toResponse(savedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByCode(String code) {
        Employee employee = employeeRepository.findByEmployeeCode(code)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByUserId(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> searchEmployees(
            String keyword,
            Long departmentId,
            Long positionId,
            com.ng_doanh.hr_management_system.employee.enums.EmploymentStatus status,
            com.ng_doanh.hr_management_system.common.enums.Gender gender,
            java.time.LocalDate hireDateFrom,
            java.time.LocalDate hireDateTo,
            Pageable pageable
    ) {
        Page<Employee> employeePage = employeeRepository.searchEmployees(
                keyword,
                departmentId,
                positionId,
                status,
                gender,
                hireDateFrom,
                hireDateTo,
                pageable
        );
        return employeePage.map(employeeMapper::toResponse);
    }

    @Override
    @Transactional
    @Audited(action = "UPDATE_EMPLOYEE", entity = "Employee")
    @CacheEvict(value = {"dashboardSummary", "departmentStats", "activeDepartments"}, allEntries = true)
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        log.info("Updating employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        if (request.getDepartmentId() != null && !departmentRepository.existsById(request.getDepartmentId())) {
            throw new BusinessException(ResponseCode.RESOURCE_NOT_FOUND);
        }

        if (request.getPositionId() != null && !positionRepository.existsById(request.getPositionId())) {
            throw new BusinessException(ResponseCode.RESOURCE_NOT_FOUND);
        }

        if (request.getManagerId() != null) {
            if (request.getManagerId().equals(id)) {
                throw new BusinessException(ResponseCode.BAD_REQUEST);
            }
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
            employee.setManager(manager);
        } else {
            employee.setManager(null);
        }

        if (request.getEmployeeCode() != null && !request.getEmployeeCode().isBlank()) {
            String newCode = request.getEmployeeCode().trim();
            if (!newCode.equalsIgnoreCase(employee.getEmployeeCode())) {
                if (employeeRepository.existsByEmployeeCode(newCode)) {
                    throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
                }
                employee.setEmployeeCode(newCode);
            }
        }

        employeeMapper.updateEntityFromRequest(request, employee);

        Employee updatedEmployee = employeeRepository.save(employee);
        log.info("Employee updated successfully with ID: {}", updatedEmployee.getId());

        return employeeMapper.toResponse(updatedEmployee);
    }

    @Override
    @Transactional
    @Audited(action = "DELETE_EMPLOYEE", entity = "Employee")
    @CacheEvict(value = {"dashboardSummary", "departmentStats", "activeDepartments"}, allEntries = true)
    public void deleteEmployee(Long id) {
        log.info("Deleting (soft-deleting / terminating) employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        employee.setEmploymentStatus(EmploymentStatus.TERMINATED);
        employeeRepository.save(employee);

        log.info("Employee marked as TERMINATED with ID: {}", id);
    }
}

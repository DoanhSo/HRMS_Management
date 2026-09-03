package com.ng_doanh.hr_management_system.employee.repository;

import com.ng_doanh.hr_management_system.common.enums.Gender;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.enums.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByUserId(Long userId);

    boolean existsByEmployeeCode(String employeeCode);

    long countByDepartmentId(Long departmentId);

    long countByPositionId(Long positionId);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:keyword IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:departmentId IS NULL OR e.departmentId = :departmentId) AND " +
           "(:positionId IS NULL OR e.positionId = :positionId) AND " +
           "(:status IS NULL OR e.employmentStatus = :status) AND " +
           "(:gender IS NULL OR e.gender = :gender) AND " +
           "(:hireDateFrom IS NULL OR e.hireDate >= :hireDateFrom) AND " +
           "(:hireDateTo IS NULL OR e.hireDate <= :hireDateTo)")
    Page<Employee> searchEmployees(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("positionId") Long positionId,
            @Param("status") EmploymentStatus status,
            @Param("gender") Gender gender,
            @Param("hireDateFrom") LocalDate hireDateFrom,
            @Param("hireDateTo") LocalDate hireDateTo,
            Pageable pageable
    );

    default Page<Employee> searchEmployees(
            String keyword,
            Long departmentId,
            Long positionId,
            Pageable pageable
    ) {
        return searchEmployees(keyword, departmentId, positionId, null, null, null, null, pageable);
    }

    @Query("SELECT COUNT(e) FROM Employee e")
    long countTotalEmployees();
}

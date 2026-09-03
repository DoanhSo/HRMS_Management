package com.ng_doanh.hr_management_system.payroll.repository;

import com.ng_doanh.hr_management_system.payroll.entity.Payslip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    Optional<Payslip> findByPayrollPeriodIdAndEmployeeId(Long payrollPeriodId, Long employeeId);

    Page<Payslip> findByEmployeeId(Long employeeId, Pageable pageable);

    @Query("SELECT p FROM Payslip p WHERE " +
           "(:periodId IS NULL OR p.payrollPeriod.id = :periodId) AND " +
           "(:keyword IS NULL OR LOWER(p.employee.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.employee.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.employee.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:departmentId IS NULL OR p.employee.departmentId = :departmentId)")
    Page<Payslip> searchPayslips(
            @Param("periodId") Long periodId,
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            Pageable pageable
    );
}

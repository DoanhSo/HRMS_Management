package com.ng_doanh.hr_management_system.kpi.repository;

import com.ng_doanh.hr_management_system.kpi.entity.KpiEvaluation;
import com.ng_doanh.hr_management_system.kpi.enums.KpiEvaluationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KpiEvaluationRepository extends JpaRepository<KpiEvaluation, Long> {

    Optional<KpiEvaluation> findByEmployeeIdAndPeriodYearAndPeriodMonth(Long employeeId, Integer periodYear, Integer periodMonth);

    Optional<KpiEvaluation> findByEmployeeIdAndPeriodYearAndPeriodMonthAndStatus(
            Long employeeId, Integer periodYear, Integer periodMonth, KpiEvaluationStatus status
    );

    Page<KpiEvaluation> findByEmployeeIdOrderByPeriodYearDescPeriodMonthDesc(Long employeeId, Pageable pageable);

    @Query("SELECT k FROM KpiEvaluation k WHERE " +
           "(:year IS NULL OR k.periodYear = :year) AND " +
           "(:month IS NULL OR k.periodMonth = :month) AND " +
           "(:status IS NULL OR k.status = :status) AND " +
           "(:departmentId IS NULL OR k.employee.departmentId = :departmentId) AND " +
           "(:keyword IS NULL OR LOWER(k.employee.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(k.employee.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(k.employee.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<KpiEvaluation> searchEvaluations(
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("status") KpiEvaluationStatus status,
            @Param("departmentId") Long departmentId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}

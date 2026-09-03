package com.ng_doanh.hr_management_system.attendance.repository;

import com.ng_doanh.hr_management_system.attendance.entity.Attendance;
import com.ng_doanh.hr_management_system.attendance.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    Page<Attendance> findByEmployeeIdAndWorkDateBetween(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT a FROM Attendance a WHERE " +
           "(:keyword IS NULL OR LOWER(a.employee.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.employee.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.employee.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:departmentId IS NULL OR a.employee.departmentId = :departmentId) AND " +
           "(:startDate IS NULL OR a.workDate >= :startDate) AND " +
           "(:endDate IS NULL OR a.workDate <= :endDate) AND " +
           "(:status IS NULL OR a.status = :status)")
    Page<Attendance> searchAttendances(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") AttendanceStatus status,
            Pageable pageable
    );
}

package com.ng_doanh.hr_management_system.leave.repository;

import com.ng_doanh.hr_management_system.leave.entity.LeaveRequest;
import com.ng_doanh.hr_management_system.leave.enums.LeaveRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "(:keyword IS NULL OR LOWER(lr.employee.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(lr.employee.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(lr.employee.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:departmentId IS NULL OR lr.employee.departmentId = :departmentId) AND " +
           "(:leaveTypeId IS NULL OR lr.leaveType.id = :leaveTypeId) AND " +
           "(:status IS NULL OR lr.status = :status) AND " +
           "(:startDate IS NULL OR lr.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR lr.endDate <= :endDate)")
    Page<LeaveRequest> searchLeaveRequests(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("leaveTypeId") Long leaveTypeId,
            @Param("status") LeaveRequestStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}

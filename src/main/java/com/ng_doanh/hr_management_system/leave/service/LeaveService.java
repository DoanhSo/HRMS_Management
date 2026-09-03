package com.ng_doanh.hr_management_system.leave.service;

import com.ng_doanh.hr_management_system.leave.dto.request.LeaveApprovalRequest;
import com.ng_doanh.hr_management_system.leave.dto.request.LeaveRequestCreateRequest;
import com.ng_doanh.hr_management_system.leave.dto.request.LeaveTypeCreateRequest;
import com.ng_doanh.hr_management_system.leave.dto.response.LeaveBalanceResponse;
import com.ng_doanh.hr_management_system.leave.dto.response.LeaveRequestResponse;
import com.ng_doanh.hr_management_system.leave.dto.response.LeaveTypeResponse;
import com.ng_doanh.hr_management_system.leave.enums.LeaveRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface LeaveService {

    LeaveTypeResponse createLeaveType(LeaveTypeCreateRequest request);

    List<LeaveTypeResponse> getAllActiveLeaveTypes();

    List<LeaveBalanceResponse> getMyLeaveBalances(Long userId, Integer year);

    LeaveRequestResponse createLeaveRequest(Long userId, LeaveRequestCreateRequest request);

    Page<LeaveRequestResponse> getMyLeaveRequests(Long userId, Pageable pageable);

    Page<LeaveRequestResponse> searchLeaveRequests(
            String keyword,
            Long departmentId,
            Long leaveTypeId,
            LeaveRequestStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    LeaveRequestResponse approveLeaveRequest(Long requestId, Long approverUserId);

    LeaveRequestResponse rejectLeaveRequest(Long requestId, Long approverUserId, LeaveApprovalRequest request);

    LeaveRequestResponse cancelLeaveRequest(Long requestId, Long userId);
}

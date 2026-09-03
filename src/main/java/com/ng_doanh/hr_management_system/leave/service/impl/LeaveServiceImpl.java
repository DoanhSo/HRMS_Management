package com.ng_doanh.hr_management_system.leave.service.impl;

import com.ng_doanh.hr_management_system.attendance.entity.Attendance;
import com.ng_doanh.hr_management_system.attendance.enums.AttendanceStatus;
import com.ng_doanh.hr_management_system.attendance.repository.AttendanceRepository;
import com.ng_doanh.hr_management_system.audit.annotation.Audited;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.common.util.CodeGeneratorUtil;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import com.ng_doanh.hr_management_system.leave.dto.request.LeaveApprovalRequest;
import com.ng_doanh.hr_management_system.leave.dto.request.LeaveRequestCreateRequest;
import com.ng_doanh.hr_management_system.leave.dto.request.LeaveTypeCreateRequest;
import com.ng_doanh.hr_management_system.leave.dto.response.LeaveBalanceResponse;
import com.ng_doanh.hr_management_system.leave.dto.response.LeaveRequestResponse;
import com.ng_doanh.hr_management_system.leave.dto.response.LeaveTypeResponse;
import com.ng_doanh.hr_management_system.leave.entity.LeaveBalance;
import com.ng_doanh.hr_management_system.leave.entity.LeaveRequest;
import com.ng_doanh.hr_management_system.leave.entity.LeaveType;
import com.ng_doanh.hr_management_system.leave.enums.LeaveRequestStatus;
import com.ng_doanh.hr_management_system.leave.mapper.LeaveMapper;
import com.ng_doanh.hr_management_system.leave.repository.LeaveBalanceRepository;
import com.ng_doanh.hr_management_system.leave.repository.LeaveRequestRepository;
import com.ng_doanh.hr_management_system.leave.repository.LeaveTypeRepository;
import com.ng_doanh.hr_management_system.leave.service.LeaveService;
import com.ng_doanh.hr_management_system.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveMapper leaveMapper;
    private final EmailService emailService;
    private final com.ng_doanh.hr_management_system.notification.service.NotificationService notificationService;

    @Override
    @Transactional
    @Audited(action = "CREATE_LEAVE_TYPE", entity = "LeaveType")
    public LeaveTypeResponse createLeaveType(LeaveTypeCreateRequest request) {
        String code;
        if (request.getCode() == null || request.getCode().isBlank()) {
            code = CodeGeneratorUtil.generateCode("LT-", 5, leaveTypeRepository.count(), leaveTypeRepository::existsByCode);
        } else {
            code = request.getCode().trim();
            if (leaveTypeRepository.existsByCode(code)) {
                throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
            }
        }

        LeaveType leaveType = leaveMapper.toEntity(request);
        leaveType.setCode(code);
        LeaveType savedLeaveType = leaveTypeRepository.save(leaveType);

        return leaveMapper.toResponse(savedLeaveType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> getAllActiveLeaveTypes() {
        return leaveTypeRepository.findByActiveTrue().stream()
                .map(leaveMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<LeaveBalanceResponse> getMyLeaveBalances(Long userId, Integer year) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        int targetYear = year != null ? year : LocalDate.now().getYear();

        ensureLeaveBalancesInitialized(employee, targetYear);

        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeIdAndYear(employee.getId(), targetYear);
        return balances.stream()
                .map(leaveMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @Audited(action = "SUBMIT_LEAVE_REQUEST", entity = "LeaveRequest")
    public LeaveRequestResponse createLeaveRequest(Long userId, LeaveRequestCreateRequest request) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }

        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        BigDecimal totalDays = calculateWorkingDays(request.getStartDate(), request.getEndDate());

        int currentYear = request.getStartDate().getYear();
        ensureLeaveBalancesInitialized(employee, currentYear);

        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), leaveType.getId(), currentYear
        ).orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        if (leaveType.isPaid() && balance.getRemainingDays().compareTo(totalDays) < 0) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(totalDays)
                .reason(request.getReason())
                .status(LeaveRequestStatus.PENDING)
                .build();

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request created for employee: {} from {} to {}", employee.getEmployeeCode(), request.getStartDate(), request.getEndDate());

        // Send Email to Manager if available
        if (employee.getManager() != null && employee.getManager().getUser() != null && employee.getManager().getUser().getEmail() != null) {
            emailService.sendLeaveRequestCreatedNotification(
                    employee.getManager().getUser().getEmail(),
                    employee.getFirstName() + " " + employee.getLastName(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getReason()
            );
        }

        // Send in-app notification to Manager & HR
        String employeeName = employee.getFirstName() + " " + employee.getLastName();
        String leaveInfo = String.format("%s đã nộp đơn xin nghỉ phép từ %s đến %s", employeeName, request.getStartDate(), request.getEndDate());
        if (employee.getManager() != null && employee.getManager().getUser() != null) {
            notificationService.send(
                    employee.getManager().getUser().getId(),
                    com.ng_doanh.hr_management_system.notification.enums.NotificationType.LEAVE_SUBMITTED,
                    "Đơn nghỉ phép mới",
                    leaveInfo,
                    "/leave"
            );
        } else {
            notificationService.sendToRole(
                    "HR",
                    com.ng_doanh.hr_management_system.notification.enums.NotificationType.LEAVE_SUBMITTED,
                    "Đơn nghỉ phép mới",
                    leaveInfo,
                    "/leave"
            );
        }

        return leaveMapper.toResponse(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequestResponse> getMyLeaveRequests(Long userId, Pageable pageable) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        Page<LeaveRequest> requests = leaveRequestRepository.findByEmployeeId(employee.getId(), pageable);
        return requests.map(leaveMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequestResponse> searchLeaveRequests(
            String keyword,
            Long departmentId,
            Long leaveTypeId,
            LeaveRequestStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        Page<LeaveRequest> requests = leaveRequestRepository.searchLeaveRequests(keyword, departmentId, leaveTypeId, status, startDate, endDate, pageable);
        return requests.map(leaveMapper::toResponse);
    }

    @Override
    @Transactional
    @Audited(action = "APPROVE_LEAVE_REQUEST", entity = "LeaveRequest")
    public LeaveRequestResponse approveLeaveRequest(Long requestId, Long approverUserId) {
        Employee approver = employeeRepository.findByUserId(approverUserId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }

        leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest.setApprover(approver);

        // Deduct from leave balance if paid leave
        if (leaveRequest.getLeaveType().isPaid()) {
            int year = leaveRequest.getStartDate().getYear();
            LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                    leaveRequest.getEmployee().getId(), leaveRequest.getLeaveType().getId(), year
            ).orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

            balance.setUsedDays(balance.getUsedDays().add(leaveRequest.getTotalDays()));
            balance.setRemainingDays(balance.getTotalDays().subtract(balance.getUsedDays()));
            leaveBalanceRepository.save(balance);
        }

        // Mark attendances as ON_LEAVE for dates in range
        LocalDate curr = leaveRequest.getStartDate();
        while (!curr.isAfter(leaveRequest.getEndDate())) {
            if (curr.getDayOfWeek() != DayOfWeek.SATURDAY && curr.getDayOfWeek() != DayOfWeek.SUNDAY) {
                final LocalDate dateToProcess = curr;
                Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(leaveRequest.getEmployee().getId(), dateToProcess)
                        .orElseGet(() -> Attendance.builder()
                                .employee(leaveRequest.getEmployee())
                                .workDate(dateToProcess)
                                .build());

                attendance.setStatus(AttendanceStatus.ON_LEAVE);
                attendanceRepository.save(attendance);
            }
            curr = curr.plusDays(1);
        }

        LeaveRequest updatedRequest = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request ID {} approved by {}", requestId, approver.getEmployeeCode());

        // Send confirmation email to Employee
        if (leaveRequest.getEmployee().getUser() != null && leaveRequest.getEmployee().getUser().getEmail() != null) {
            emailService.sendLeaveDecisionNotification(
                    leaveRequest.getEmployee().getUser().getEmail(),
                    leaveRequest.getEmployee().getFirstName() + " " + leaveRequest.getEmployee().getLastName(),
                    "APPROVED",
                    approver.getFirstName() + " " + approver.getLastName(),
                    null
            );
        }

        // Send in-app notification to Employee
        if (leaveRequest.getEmployee().getUser() != null) {
            notificationService.send(
                    leaveRequest.getEmployee().getUser().getId(),
                    com.ng_doanh.hr_management_system.notification.enums.NotificationType.LEAVE_APPROVED,
                    "Đơn nghỉ phép đã được duyệt",
                    String.format("Đơn xin nghỉ phép của bạn từ %s đến %s đã được %s %s phê duyệt",
                            leaveRequest.getStartDate(), leaveRequest.getEndDate(),
                            approver.getFirstName(), approver.getLastName()),
                    "/leave"
            );
        }

        return leaveMapper.toResponse(updatedRequest);
    }

    @Override
    @Transactional
    @Audited(action = "REJECT_LEAVE_REQUEST", entity = "LeaveRequest")
    public LeaveRequestResponse rejectLeaveRequest(Long requestId, Long approverUserId, LeaveApprovalRequest request) {
        Employee approver = employeeRepository.findByUserId(approverUserId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }

        leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
        leaveRequest.setApprover(approver);
        String reason = null;
        if (request != null && request.getRejectionReason() != null) {
            reason = request.getRejectionReason();
            leaveRequest.setRejectionReason(reason);
        }

        LeaveRequest updatedRequest = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request ID {} rejected by {}", requestId, approver.getEmployeeCode());

        // Send rejection email to Employee
        if (leaveRequest.getEmployee().getUser() != null && leaveRequest.getEmployee().getUser().getEmail() != null) {
            emailService.sendLeaveDecisionNotification(
                    leaveRequest.getEmployee().getUser().getEmail(),
                    leaveRequest.getEmployee().getFirstName() + " " + leaveRequest.getEmployee().getLastName(),
                    "REJECTED",
                    approver.getFirstName() + " " + approver.getLastName(),
                    reason
            );
        }

        // Send in-app notification to Employee
        if (leaveRequest.getEmployee().getUser() != null) {
            String rejectMsg = String.format("Đơn xin nghỉ phép từ %s đến %s đã bị từ chối.",
                    leaveRequest.getStartDate(), leaveRequest.getEndDate());
            if (reason != null && !reason.isBlank()) {
                rejectMsg += " Lý do: " + reason;
            }
            notificationService.send(
                    leaveRequest.getEmployee().getUser().getId(),
                    com.ng_doanh.hr_management_system.notification.enums.NotificationType.LEAVE_REJECTED,
                    "Đơn nghỉ phép bị từ chối",
                    rejectMsg,
                    "/leave"
            );
        }

        return leaveMapper.toResponse(updatedRequest);
    }

    @Override
    @Transactional
    @Audited(action = "CANCEL_LEAVE_REQUEST", entity = "LeaveRequest")
    public LeaveRequestResponse cancelLeaveRequest(Long requestId, Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        if (!leaveRequest.getEmployee().getId().equals(employee.getId())) {
            throw new BusinessException(ResponseCode.FORBIDDEN);
        }

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new BusinessException(ResponseCode.BAD_REQUEST);
        }

        leaveRequest.setStatus(LeaveRequestStatus.CANCELLED);
        LeaveRequest updatedRequest = leaveRequestRepository.save(leaveRequest);

        // Notify manager if available
        if (employee.getManager() != null && employee.getManager().getUser() != null) {
            notificationService.send(
                    employee.getManager().getUser().getId(),
                    com.ng_doanh.hr_management_system.notification.enums.NotificationType.LEAVE_CANCELLED,
                    "Đơn nghỉ phép đã hủy",
                    String.format("%s %s đã hủy đơn xin nghỉ phép từ %s đến %s",
                            employee.getFirstName(), employee.getLastName(),
                            leaveRequest.getStartDate(), leaveRequest.getEndDate()),
                    "/leave"
            );
        }

        return leaveMapper.toResponse(updatedRequest);
    }

    private void ensureLeaveBalancesInitialized(Employee employee, int year) {
        List<LeaveType> activeLeaveTypes = leaveTypeRepository.findByActiveTrue();
        for (LeaveType leaveType : activeLeaveTypes) {
            leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employee.getId(), leaveType.getId(), year)
                    .orElseGet(() -> {
                        LeaveBalance newBalance = LeaveBalance.builder()
                                .employee(employee)
                                .leaveType(leaveType)
                                .year(year)
                                .totalDays(BigDecimal.valueOf(leaveType.getDefaultDaysPerYear()))
                                .usedDays(BigDecimal.ZERO)
                                .remainingDays(BigDecimal.valueOf(leaveType.getDefaultDaysPerYear()))
                                .build();
                        return leaveBalanceRepository.save(newBalance);
                    });
        }
    }

    private BigDecimal calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        double workingDays = 0.0;
        LocalDate curr = startDate;
        while (!curr.isAfter(endDate)) {
            if (curr.getDayOfWeek() != DayOfWeek.SATURDAY && curr.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays += 1.0;
            }
            curr = curr.plusDays(1);
        }
        return BigDecimal.valueOf(workingDays);
    }
}

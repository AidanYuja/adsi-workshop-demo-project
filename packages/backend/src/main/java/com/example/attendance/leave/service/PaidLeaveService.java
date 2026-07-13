package com.example.attendance.leave.service;

import com.example.attendance.leave.dto.LeaveBalanceResponse;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest;
import com.example.attendance.leave.dto.LeaveRequestResponse;
import com.example.attendance.leave.dto.PendingLeaveResponse;

import java.util.List;
import java.util.UUID;

public interface PaidLeaveService {

    LeaveRequestResponse create(UUID requesterId, LeaveRequestCreateRequest request);

    List<LeaveRequestResponse> findByRequester(UUID requesterId);

    LeaveRequestResponse cancel(UUID leaveRequestId, UUID requesterId, Long version);

    List<PendingLeaveResponse> findPending(UUID managerId);

    LeaveRequestResponse approve(UUID leaveRequestId, UUID approverId, Long version);

    LeaveRequestResponse reject(UUID leaveRequestId, UUID approverId, String reason, Long version);

    LeaveBalanceResponse getBalance(UUID employeeId);
}

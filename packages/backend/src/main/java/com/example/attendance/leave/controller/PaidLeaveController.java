package com.example.attendance.leave.controller;

import com.example.attendance.leave.dto.LeaveBalanceResponse;
import com.example.attendance.leave.dto.LeaveRejectRequest;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest;
import com.example.attendance.leave.dto.LeaveRequestResponse;
import com.example.attendance.leave.dto.PendingLeaveResponse;
import com.example.attendance.leave.service.PaidLeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leaves")
public class PaidLeaveController {

    private final PaidLeaveService paidLeaveService;

    public PaidLeaveController(PaidLeaveService paidLeaveService) {
        this.paidLeaveService = paidLeaveService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequestResponse create(
            @RequestParam UUID requesterId,
            @Valid @RequestBody LeaveRequestCreateRequest request) {
        return paidLeaveService.create(requesterId, request);
    }

    @GetMapping
    public List<LeaveRequestResponse> findByRequester(@RequestParam UUID requesterId) {
        return paidLeaveService.findByRequester(requesterId);
    }

    @PatchMapping("/{id}/cancel")
    public LeaveRequestResponse cancel(
            @PathVariable UUID id,
            @RequestParam UUID requesterId,
            @RequestParam Long version) {
        return paidLeaveService.cancel(id, requesterId, version);
    }

    @GetMapping("/pending")
    public List<PendingLeaveResponse> findPending(@RequestParam UUID managerId) {
        return paidLeaveService.findPending(managerId);
    }

    @PatchMapping("/{id}/approve")
    public LeaveRequestResponse approve(
            @PathVariable UUID id,
            @RequestParam UUID approverId,
            @RequestParam Long version) {
        return paidLeaveService.approve(id, approverId, version);
    }

    @PatchMapping("/{id}/reject")
    public LeaveRequestResponse reject(
            @PathVariable UUID id,
            @RequestParam UUID approverId,
            @Valid @RequestBody LeaveRejectRequest request) {
        return paidLeaveService.reject(id, approverId, request.reason(), request.version());
    }

    @GetMapping("/balance")
    public LeaveBalanceResponse getBalance(@RequestParam UUID employeeId) {
        return paidLeaveService.getBalance(employeeId);
    }
}

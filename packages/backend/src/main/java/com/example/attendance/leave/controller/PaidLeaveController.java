package com.example.attendance.leave.controller;

import com.example.attendance.common.config.security.EmployeeUserDetails;
import com.example.attendance.leave.dto.LeaveBalanceResponse;
import com.example.attendance.leave.dto.LeaveRejectRequest;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest;
import com.example.attendance.leave.dto.LeaveRequestResponse;
import com.example.attendance.leave.dto.PendingLeaveResponse;
import com.example.attendance.leave.service.PaidLeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal EmployeeUserDetails user,
            @Valid @RequestBody LeaveRequestCreateRequest request) {
        return paidLeaveService.create(user.getEmployeeId(), request);
    }

    @GetMapping
    public List<LeaveRequestResponse> findByRequester(
            @AuthenticationPrincipal EmployeeUserDetails user) {
        return paidLeaveService.findByRequester(user.getEmployeeId());
    }

    @PatchMapping("/{id}/cancel")
    public LeaveRequestResponse cancel(
            @AuthenticationPrincipal EmployeeUserDetails user,
            @PathVariable UUID id,
            @RequestParam Long version) {
        return paidLeaveService.cancel(id, user.getEmployeeId(), version);
    }

    @GetMapping("/pending")
    public List<PendingLeaveResponse> findPending(
            @AuthenticationPrincipal EmployeeUserDetails user) {
        return paidLeaveService.findPending(user.getEmployeeId());
    }

    @PatchMapping("/{id}/approve")
    public LeaveRequestResponse approve(
            @AuthenticationPrincipal EmployeeUserDetails user,
            @PathVariable UUID id,
            @RequestParam Long version) {
        return paidLeaveService.approve(id, user.getEmployeeId(), version);
    }

    @PatchMapping("/{id}/reject")
    public LeaveRequestResponse reject(
            @AuthenticationPrincipal EmployeeUserDetails user,
            @PathVariable UUID id,
            @Valid @RequestBody LeaveRejectRequest request) {
        return paidLeaveService.reject(id, user.getEmployeeId(), request.reason(), request.version());
    }

    @GetMapping("/balance")
    public LeaveBalanceResponse getBalance(
            @AuthenticationPrincipal EmployeeUserDetails user) {
        return paidLeaveService.getBalance(user.getEmployeeId());
    }
}

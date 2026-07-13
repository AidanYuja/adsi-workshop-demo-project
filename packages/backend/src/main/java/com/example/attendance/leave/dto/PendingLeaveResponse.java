package com.example.attendance.leave.dto;

import com.example.attendance.leave.entity.LeaveType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PendingLeaveResponse(
    UUID id,
    UUID requesterId,
    String requesterName,
    LocalDate startDate,
    LocalDate endDate,
    LeaveType leaveType,
    String reason,
    BigDecimal totalDays,
    Instant createdAt,
    Long version
) {}

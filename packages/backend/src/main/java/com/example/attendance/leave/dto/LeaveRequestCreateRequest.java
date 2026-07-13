package com.example.attendance.leave.dto;

import com.example.attendance.leave.entity.LeaveType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record LeaveRequestCreateRequest(
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @NotNull LeaveType leaveType,
    @NotNull @Size(min = 1, max = 500) String reason
) {
    @AssertTrue(message = "開始日は終了日以前である必要があります")
    boolean isDateRangeValid() {
        if (startDate == null || endDate == null) return true;
        return !startDate.isAfter(endDate);
    }
}

package com.example.attendance.attendance.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TeamMemberSummaryResponse(
    UUID employeeId,
    String employeeName,
    int workDays,
    int totalWorkMinutes,
    int totalOvertimeMinutes,
    int absentDays,
    List<MemoEntry> memos
) {
    public record MemoEntry(
        LocalDate date,
        String clockInMemo,
        String clockOutMemo
    ) {}
}

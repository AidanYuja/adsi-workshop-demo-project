package com.example.attendance.attendance.dto;

import jakarta.validation.constraints.Size;

public record ClockOutRequest(
    @Size(max = 1000)
    String memo
) {}

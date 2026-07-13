package com.example.attendance.attendance.dto;

import jakarta.validation.constraints.Size;

public record UpdateMemoRequest(
    @Size(max = 1000)
    String clockInMemo,

    @Size(max = 1000)
    String clockOutMemo
) {}

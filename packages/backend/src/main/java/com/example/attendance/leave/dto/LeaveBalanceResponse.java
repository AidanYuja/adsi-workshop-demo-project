package com.example.attendance.leave.dto;

import java.math.BigDecimal;

public record LeaveBalanceResponse(
    int fiscalYear,
    BigDecimal grantedDays,
    BigDecimal carriedOverDays,
    BigDecimal usedDays,
    BigDecimal remainingDays
) {}

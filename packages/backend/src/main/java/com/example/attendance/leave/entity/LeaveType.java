package com.example.attendance.leave.entity;

import java.math.BigDecimal;

public enum LeaveType {
    FULL_DAY(new BigDecimal("1.0")),
    AM_HALF(new BigDecimal("0.5")),
    PM_HALF(new BigDecimal("0.5"));

    private final BigDecimal daysPerUnit;

    LeaveType(BigDecimal daysPerUnit) {
        this.daysPerUnit = daysPerUnit;
    }

    public BigDecimal getDaysPerUnit() {
        return daysPerUnit;
    }
}

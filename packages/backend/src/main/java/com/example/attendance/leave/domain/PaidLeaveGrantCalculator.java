package com.example.attendance.leave.domain;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class PaidLeaveGrantCalculator {

    private static final BigDecimal[] GRANT_TABLE = {
        new BigDecimal("10"),  // 0.5年
        new BigDecimal("11"),  // 1.5年
        new BigDecimal("12"),  // 2.5年
        new BigDecimal("14"),  // 3.5年
        new BigDecimal("16"),  // 4.5年
        new BigDecimal("18"),  // 5.5年
        new BigDecimal("20"),  // 6.5年以上
    };

    public BigDecimal calculate(LocalDate hireDate, LocalDate referenceDate) {
        long months = ChronoUnit.MONTHS.between(hireDate, referenceDate);
        if (months < 6) {
            return BigDecimal.ZERO;
        }

        int yearsIndex = (int) ((months - 6) / 12);
        if (yearsIndex >= GRANT_TABLE.length) {
            return GRANT_TABLE[GRANT_TABLE.length - 1];
        }
        return GRANT_TABLE[yearsIndex];
    }

    public long countWeekdays(LocalDate start, LocalDate end) {
        long count = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }
}

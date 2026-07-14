package com.example.attendance.leave.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PaidLeaveGrantCalculatorTest {

    private final PaidLeaveGrantCalculator calculator = new PaidLeaveGrantCalculator();

    @Test
    @DisplayName("入社6ヶ月未満: 付与日数0日")
    void calculate_lessThan6Months_returnsZero() {
        var hireDate = LocalDate.of(2026, 1, 15);
        var referenceDate = LocalDate.of(2026, 7, 1);

        assertThat(calculator.calculate(hireDate, referenceDate))
            .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("入社6ヶ月（0.5年）: 10日付与")
    void calculate_6Months_returns10() {
        var hireDate = LocalDate.of(2026, 1, 1);
        var referenceDate = LocalDate.of(2026, 7, 1);

        assertThat(calculator.calculate(hireDate, referenceDate))
            .isEqualByComparingTo(new BigDecimal("10"));
    }

    @Test
    @DisplayName("入社1.5年: 11日付与")
    void calculate_1Point5Years_returns11() {
        var hireDate = LocalDate.of(2025, 1, 1);
        var referenceDate = LocalDate.of(2026, 7, 1);

        assertThat(calculator.calculate(hireDate, referenceDate))
            .isEqualByComparingTo(new BigDecimal("11"));
    }

    @Test
    @DisplayName("入社2.5年: 12日付与")
    void calculate_2Point5Years_returns12() {
        var hireDate = LocalDate.of(2024, 1, 1);
        var referenceDate = LocalDate.of(2026, 7, 1);

        assertThat(calculator.calculate(hireDate, referenceDate))
            .isEqualByComparingTo(new BigDecimal("12"));
    }

    @Test
    @DisplayName("入社3.5年: 14日付与")
    void calculate_3Point5Years_returns14() {
        var hireDate = LocalDate.of(2023, 1, 1);
        var referenceDate = LocalDate.of(2026, 7, 1);

        assertThat(calculator.calculate(hireDate, referenceDate))
            .isEqualByComparingTo(new BigDecimal("14"));
    }

    @Test
    @DisplayName("入社4.5年: 16日付与")
    void calculate_4Point5Years_returns16() {
        var hireDate = LocalDate.of(2022, 1, 1);
        var referenceDate = LocalDate.of(2026, 7, 1);

        assertThat(calculator.calculate(hireDate, referenceDate))
            .isEqualByComparingTo(new BigDecimal("16"));
    }

    @Test
    @DisplayName("入社5.5年: 18日付与")
    void calculate_5Point5Years_returns18() {
        var hireDate = LocalDate.of(2021, 1, 1);
        var referenceDate = LocalDate.of(2026, 7, 1);

        assertThat(calculator.calculate(hireDate, referenceDate))
            .isEqualByComparingTo(new BigDecimal("18"));
    }

    @Test
    @DisplayName("入社6.5年以上: 20日付与（上限）")
    void calculate_6Point5YearsOrMore_returns20() {
        var hireDate = LocalDate.of(2019, 1, 1);
        var referenceDate = LocalDate.of(2026, 7, 1);

        assertThat(calculator.calculate(hireDate, referenceDate))
            .isEqualByComparingTo(new BigDecimal("20"));
    }

    @Test
    @DisplayName("平日カウント: 月〜金のみ（土日除外）")
    void countWeekdays_excludesSaturdayAndSunday() {
        // 2026-07-06 (Mon) 〜 2026-07-12 (Sun) = 5平日
        var start = LocalDate.of(2026, 7, 6);
        var end = LocalDate.of(2026, 7, 12);

        assertThat(calculator.countWeekdays(start, end)).isEqualTo(5);
    }

    @Test
    @DisplayName("平日カウント: 土日のみの期間は0")
    void countWeekdays_weekendOnly_returnsZero() {
        // 2026-07-11 (Sat) 〜 2026-07-12 (Sun)
        var start = LocalDate.of(2026, 7, 11);
        var end = LocalDate.of(2026, 7, 12);

        assertThat(calculator.countWeekdays(start, end)).isEqualTo(0);
    }
}

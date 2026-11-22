package pl.edu.skarbonka.service;

import java.math.BigDecimal;

public record MonthlyStatistics(
        String monthLabel,
        BigDecimal deposits,
        BigDecimal withdrawals,
        int depositsPercent,
        int withdrawalsPercent) {
}

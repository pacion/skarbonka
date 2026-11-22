package pl.edu.skarbonka.service;

import java.math.BigDecimal;

public record AccountStatistics(
        BigDecimal totalSaved,
        BigDecimal totalDeposits,
        BigDecimal totalWithdrawals,
        int goalCount,
        int achievedGoalCount) {
}

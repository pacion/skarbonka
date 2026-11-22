package pl.edu.skarbonka.service;

import java.math.BigDecimal;
import java.util.List;

public record DashboardView(
        BigDecimal accountBalance,
        List<GoalSummary> goals,
        List<MonthlyStatistics> monthlyStatistics,
        AccountStatistics accountStatistics) {
}

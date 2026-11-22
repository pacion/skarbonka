package pl.edu.skarbonka.service;

import java.math.BigDecimal;

public record GoalSummary(
        Long id,
        String name,
        BigDecimal targetAmount,
        BigDecimal balance,
        BigDecimal remainingAmount,
        boolean achieved,
        int progressPercent) {
}

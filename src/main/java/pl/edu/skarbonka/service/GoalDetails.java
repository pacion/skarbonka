package pl.edu.skarbonka.service;

import java.math.BigDecimal;
import java.util.List;

public record GoalDetails(
        Long id,
        String name,
        BigDecimal targetAmount,
        BigDecimal balance,
        BigDecimal remainingAmount,
        boolean achieved,
        List<OperationView> operations) {
}

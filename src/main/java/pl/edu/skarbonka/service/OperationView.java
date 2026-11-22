package pl.edu.skarbonka.service;

import java.math.BigDecimal;
import pl.edu.skarbonka.model.OperationType;

public record OperationView(
        OperationType type,
        BigDecimal amount,
        String occurredAt) {
}

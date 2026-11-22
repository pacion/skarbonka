package pl.edu.skarbonka.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "savings_operations")
public class SavingsOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_id", nullable = false)
    private SavingsGoal goal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperationType type;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    protected SavingsOperation() {
    }

    public SavingsOperation(SavingsGoal goal, OperationType type, BigDecimal amount, LocalDateTime occurredAt) {
        this.goal = goal;
        this.type = type;
        this.amount = amount;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public SavingsGoal getGoal() {
        return goal;
    }

    public OperationType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}

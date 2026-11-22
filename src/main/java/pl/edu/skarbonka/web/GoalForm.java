package pl.edu.skarbonka.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class GoalForm {

    @NotBlank(message = "Podaj nazwę skarbonki.")
    @Size(max = 100, message = "Nazwa może mieć maksymalnie 100 znaków.")
    private String name;

    @NotNull(message = "Podaj kwotę docelową.")
    @DecimalMin(value = "0.01", message = "Kwota docelowa musi być większa od zera.")
    @Digits(integer = 12, fraction = 2, message = "Kwota może mieć maksymalnie dwa miejsca po przecinku.")
    private BigDecimal targetAmount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }
}

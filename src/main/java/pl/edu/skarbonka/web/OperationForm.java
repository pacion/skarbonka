package pl.edu.skarbonka.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class OperationForm {

    @NotNull(message = "Podaj kwotę operacji.")
    @DecimalMin(value = "0.01", message = "Kwota musi być większa od zera.")
    @Digits(integer = 12, fraction = 2, message = "Kwota może mieć maksymalnie dwa miejsca po przecinku.")
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

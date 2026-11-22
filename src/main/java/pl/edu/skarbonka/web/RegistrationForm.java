package pl.edu.skarbonka.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class RegistrationForm {

    @NotBlank(message = "Podaj login.")
    @Size(min = 3, max = 50, message = "Login musi mieć od 3 do 50 znaków.")
    @Pattern(regexp = "[A-Za-z0-9._-]+", message = "Login może zawierać litery bez polskich znaków, cyfry, kropkę, myślnik i podkreślenie.")
    private String username;

    @NotBlank(message = "Podaj imię i nazwisko.")
    @Size(max = 160, message = "Imię i nazwisko może mieć maksymalnie 160 znaków.")
    private String fullName;

    @NotBlank(message = "Podaj hasło.")
    @Size(min = 8, max = 72, message = "Hasło musi mieć od 8 do 72 znaków.")
    private String password;

    @NotNull(message = "Podaj saldo początkowe.")
    @PositiveOrZero(message = "Saldo początkowe nie może być ujemne.")
    @Digits(integer = 36, fraction = 2, message = "Kwota jest poza obsługiwanym zakresem lub ma więcej niż dwa miejsca po przecinku.")
    private BigDecimal initialBalance;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}

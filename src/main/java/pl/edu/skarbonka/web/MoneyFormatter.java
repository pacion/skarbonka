package pl.edu.skarbonka.web;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component("moneyFormatter")
public class MoneyFormatter {

    public String format(BigDecimal amount) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pl-PL"));
        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        return format.format(amount);
    }
}

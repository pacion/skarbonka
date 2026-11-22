package pl.edu.skarbonka.service;

import java.math.BigDecimal;
import java.util.Locale;
import pl.edu.skarbonka.model.Account;
import pl.edu.skarbonka.model.Customer;
import pl.edu.skarbonka.repository.AccountRepository;
import pl.edu.skarbonka.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public boolean register(String username, String fullName, String password, BigDecimal initialBalance) {
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        if (customerRepository.existsByUsername(normalizedUsername)) {
            return false;
        }

        Customer customer = customerRepository.save(new Customer(
                normalizedUsername,
                passwordEncoder.encode(password),
                fullName.trim()));
        accountRepository.save(new Account(customer, initialBalance));
        return true;
    }
}

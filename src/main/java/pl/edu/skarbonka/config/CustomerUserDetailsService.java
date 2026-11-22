package pl.edu.skarbonka.config;

import pl.edu.skarbonka.repository.CustomerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public CustomerUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono klienta."));
        return User.withUsername(customer.getUsername())
                .password(customer.getPasswordHash())
                .roles("CUSTOMER")
                .build();
    }
}

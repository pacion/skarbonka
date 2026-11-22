package pl.edu.skarbonka.repository;

import java.util.Optional;
import pl.edu.skarbonka.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUsername(String username);

    boolean existsByUsername(String username);
}

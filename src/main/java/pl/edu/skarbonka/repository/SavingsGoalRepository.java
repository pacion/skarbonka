package pl.edu.skarbonka.repository;

import java.util.List;
import java.util.Optional;
import pl.edu.skarbonka.model.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findAllByAccountCustomerUsernameOrderByIdDesc(String username);

    Optional<SavingsGoal> findByIdAndAccountCustomerUsername(Long id, String username);
}

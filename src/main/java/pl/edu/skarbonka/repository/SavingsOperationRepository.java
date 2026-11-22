package pl.edu.skarbonka.repository;

import java.util.List;
import pl.edu.skarbonka.model.SavingsOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SavingsOperationRepository extends JpaRepository<SavingsOperation, Long> {

    List<SavingsOperation> findAllByGoalIdOrderByOccurredAtDescIdDesc(Long goalId);

    @Query("""
            select operation
            from SavingsOperation operation
            join fetch operation.goal goal
            join goal.account account
            join account.customer customer
            where customer.username = :username
            order by operation.occurredAt asc, operation.id asc
            """)
    List<SavingsOperation> findAllForCustomerOrderByOccurredAt(@Param("username") String username);
}

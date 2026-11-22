package pl.edu.skarbonka.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import pl.edu.skarbonka.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("select a from Account a join fetch a.customer c where c.username = :username")
    Optional<Account> findByCustomerUsername(@Param("username") String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a join fetch a.customer c where c.username = :username")
    Optional<Account> findByCustomerUsernameForUpdate(@Param("username") String username);
}

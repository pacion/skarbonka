package pl.edu.skarbonka;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import pl.edu.skarbonka.repository.AccountRepository;
import pl.edu.skarbonka.repository.SavingsGoalRepository;
import pl.edu.skarbonka.repository.SavingsOperationRepository;
import pl.edu.skarbonka.service.SavingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:demo-migration;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;LOCK_TIMEOUT=10000"
})
class DemoDataMigrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SavingsGoalRepository goalRepository;

    @Autowired
    private SavingsOperationRepository operationRepository;

    @Autowired
    private SavingsService savingsService;

    @Test
    void migrationSeedsConsistentGoalsOperationsAndMonthlyStatisticsForBothCustomers() {
        assertAmount("anna", "1200.00");
        assertAmount("patryk", "1000.00");
        assertEquals(2, goalRepository.findAllByAccountCustomerUsernameOrderByIdDesc("anna").size());
        assertEquals(2, goalRepository.findAllByAccountCustomerUsernameOrderByIdDesc("patryk").size());
        assertEquals(5, operationRepository.findAllForCustomerOrderByOccurredAt("anna").size());
        assertEquals(5, operationRepository.findAllForCustomerOrderByOccurredAt("patryk").size());

        var anna = savingsService.getDashboard("anna");
        assertAmount(anna.accountStatistics().totalSaved(), "1800.00");
        assertAmount(anna.accountStatistics().totalDeposits(), "2000.00");
        assertAmount(anna.accountStatistics().totalWithdrawals(), "200.00");
        assertAmount(anna.accountBalance().add(anna.accountStatistics().totalSaved()), "3000.00");
        assertAmount(anna.accountStatistics().totalDeposits()
                .subtract(anna.accountStatistics().totalWithdrawals()), "1800.00");
        assertEquals(0, anna.accountStatistics().achievedGoalCount());
        assertEquals("grudzień 2025", anna.monthlyStatistics().getFirst().monthLabel());

        var patryk = savingsService.getDashboard("patryk");
        assertAmount(patryk.accountStatistics().totalSaved(), "2000.00");
        assertAmount(patryk.accountStatistics().totalDeposits(), "2100.00");
        assertAmount(patryk.accountStatistics().totalWithdrawals(), "100.00");
        assertAmount(patryk.accountBalance().add(patryk.accountStatistics().totalSaved()), "3000.00");
        assertAmount(patryk.accountStatistics().totalDeposits()
                .subtract(patryk.accountStatistics().totalWithdrawals()), "2000.00");
        assertEquals(1, patryk.accountStatistics().achievedGoalCount());
    }

    private void assertAmount(String username, String expected) {
        assertAmount(accountRepository.findByCustomerUsername(username).orElseThrow().getBalance(), expected);
    }

    private void assertAmount(BigDecimal actual, String expected) {
        assertEquals(0, actual.compareTo(new BigDecimal(expected)));
    }
}

package pl.edu.skarbonka;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import pl.edu.skarbonka.model.OperationType;
import pl.edu.skarbonka.repository.AccountRepository;
import pl.edu.skarbonka.repository.CustomerRepository;
import pl.edu.skarbonka.repository.SavingsGoalRepository;
import pl.edu.skarbonka.repository.SavingsOperationRepository;
import pl.edu.skarbonka.model.SavingsOperation;
import pl.edu.skarbonka.service.SavingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/reset-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class SavingsMvcIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SavingsGoalRepository goalRepository;

    @Autowired
    private SavingsOperationRepository operationRepository;

    @Autowired
    private SavingsService savingsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void demoPasswordsAreBCryptHashes() {
        assertTrue(passwordEncoder.matches("anna",
                accountRepository.findByCustomerUsername("anna").orElseThrow()
                        .getCustomer().getPasswordHash()));
        assertTrue(passwordEncoder.matches("patryk",
                accountRepository.findByCustomerUsername("patryk").orElseThrow()
                        .getCustomer().getPasswordHash()));
    }

    @Test
    void loginPageIncludesCsrfAndDemoUserCanSignIn() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "anna")
                        .param("password", "anna"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/"));
    }

    @Test
    void protectedPagesAndActionsRequireAuthentication() throws Exception {
        long goalId = createGoal("anna", "Prywatna", "500.00");

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
        mockMvc.perform(get("/goals/{id}", goalId))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
        mockMvc.perform(post("/goals").with(csrf())
                        .param("name", "Anonimowa")
                        .param("targetAmount", "100.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
        mockMvc.perform(post("/goals/{id}/deposit", goalId).with(csrf())
                        .param("amount", "100.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));

        assertAmount("anna", "3000.00");
        assertEquals(1, goalRepository.findAllByAccountCustomerUsernameOrderByIdDesc("anna").size());
        assertTrue(operationRepository.findAllByGoalIdOrderByOccurredAtDescIdDesc(goalId).isEmpty());
    }

    @Test
    void csrfProtectionRejectsGoalAndBalanceChanges() throws Exception {
        long goalId = createGoal("anna", "Prywatna", "500.00");

        mockMvc.perform(post("/goals").with(user("anna"))
                        .param("name", "Bez CSRF")
                        .param("targetAmount", "100.00"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/goals/{id}/deposit", goalId).with(user("anna"))
                        .param("amount", "100.00"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/goals/{id}/withdraw", goalId).with(user("anna"))
                        .param("amount", "100.00"))
                .andExpect(status().isForbidden());

        assertAmount("anna", "3000.00");
        assertEquals(1, goalRepository.findAllByAccountCustomerUsernameOrderByIdDesc("anna").size());
        assertEquals(0, goalRepository.findByIdAndAccountCustomerUsername(goalId, "anna").orElseThrow()
                .getBalance().compareTo(BigDecimal.ZERO.setScale(2)));
        assertTrue(operationRepository.findAllByGoalIdOrderByOccurredAtDescIdDesc(goalId).isEmpty());
    }

    @Test
    void registrationCreatesAccountWithChosenBalanceAndEncodedPassword() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "NowyKlient")
                        .param("fullName", "Nowy Klient")
                        .param("password", "bezpieczne-haslo")
                        .param("initialBalance", "1234.56"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login?registered"));

        assertAmount("nowyklient", "1234.56");
        assertTrue(passwordEncoder.matches("bezpieczne-haslo",
                accountRepository.findByCustomerUsername("nowyklient").orElseThrow()
                        .getCustomer().getPasswordHash()));
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "nowyklient")
                        .param("password", "bezpieczne-haslo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/"));

        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "zerokonto")
                        .param("fullName", "Konto Zerowe")
                        .param("password", "dlugie-haslo")
                        .param("initialBalance", "0"))
                .andExpect(status().is3xxRedirection());
        assertAmount("zerokonto", "0.00");
    }

    @Test
    void registrationRejectsDuplicateLoginAndNegativeStartingBalance() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "ANNA")
                        .param("fullName", "Inna Anna")
                        .param("password", "dlugie-haslo")
                        .param("initialBalance", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ten login jest już zajęty.")));
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "ujemne")
                        .param("fullName", "Ujemne Saldo")
                        .param("password", "dlugie-haslo")
                        .param("initialBalance", "-0.01"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Saldo początkowe nie może być ujemne.")));

        assertTrue(accountRepository.findByCustomerUsername("ujemne").isEmpty());
    }

    @Test
    void registrationRejectsInvalidLoginPasswordAndAmountWithoutCreatingCustomer() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "zly login")
                        .param("fullName", "Niepoprawny Login")
                        .param("password", "dlugie-haslo")
                        .param("initialBalance", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Login może zawierać")));
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "krotkiehaslo")
                        .param("fullName", "Krótkie Hasło")
                        .param("password", "krótkie")
                        .param("initialBalance", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hasło musi mieć od 8 do 72 znaków.")));
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "zlaprecyzja")
                        .param("fullName", "Zła Precyzja")
                        .param("password", "dlugie-haslo")
                        .param("initialBalance", "10.001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("więcej niż dwa miejsca po przecinku")));

        assertTrue(customerRepository.findByUsername("zly login").isEmpty());
        assertTrue(customerRepository.findByUsername("krotkiehaslo").isEmpty());
        assertTrue(customerRepository.findByUsername("zlaprecyzja").isEmpty());
    }

    @Test
    void dashboardShowsPolishMoneyAndPerGoalProgressChart() throws Exception {
        long goalId = createGoal("anna", "Wakacje", "1000.00");
        mockMvc.perform(post("/goals/{id}/deposit", goalId)
                        .with(user("anna")).with(csrf()).param("amount", "400.00"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/").with(user("anna")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Postęp skarbonek")))
                .andExpect(content().string(containsString("monthly-column-chart")))
                .andExpect(content().string(containsString("40%")))
                .andExpect(content().string(containsString("2\u00a0600,00")));
    }

    @Test
    void monthlyStatisticsGroupByMonthAndYearAndOnlyIncludeOwnGoals() throws Exception {
        long annaGoalId = createGoal("anna", "Wakacje", "1000.00");
        long patrykGoalId = createGoal("patryk", "Rower", "2000.00");
        var annaGoal = goalRepository.findByIdAndAccountCustomerUsername(annaGoalId, "anna").orElseThrow();
        var patrykGoal = goalRepository.findByIdAndAccountCustomerUsername(patrykGoalId, "patryk").orElseThrow();

        operationRepository.save(new SavingsOperation(
                annaGoal, OperationType.DEPOSIT, new BigDecimal("120.00"), LocalDateTime.of(2025, 12, 10, 12, 0)));
        operationRepository.save(new SavingsOperation(
                annaGoal, OperationType.WITHDRAWAL, new BigDecimal("20.00"), LocalDateTime.of(2025, 12, 11, 12, 0)));
        operationRepository.save(new SavingsOperation(
                annaGoal, OperationType.DEPOSIT, new BigDecimal("30.00"), LocalDateTime.of(2026, 1, 10, 12, 0)));
        operationRepository.save(new SavingsOperation(
                annaGoal, OperationType.WITHDRAWAL, new BigDecimal("40.00"), LocalDateTime.of(2026, 1, 11, 12, 0)));
        operationRepository.save(new SavingsOperation(
                patrykGoal, OperationType.DEPOSIT, new BigDecimal("900.00"), LocalDateTime.of(2026, 1, 12, 12, 0)));

        var statistics = savingsService.getDashboard("anna").monthlyStatistics();
        assertEquals(2, statistics.size());
        assertEquals("grudzień 2025", statistics.getFirst().monthLabel());
        assertEquals(0, statistics.get(0).deposits().compareTo(new BigDecimal("120.00")));
        assertEquals(0, statistics.get(0).withdrawals().compareTo(new BigDecimal("20.00")));
        assertEquals("styczeń 2026", statistics.get(1).monthLabel());
        assertEquals(0, statistics.get(1).deposits().compareTo(new BigDecimal("30.00")));
        assertEquals(0, statistics.get(1).withdrawals().compareTo(new BigDecimal("40.00")));

        mockMvc.perform(get("/").with(user("anna")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("grudzień 2025")))
                .andExpect(content().string(containsString("styczeń 2026")))
                .andExpect(content().string(containsString("120,00 PLN")))
                .andExpect(content().string(containsString("40,00 PLN")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("900,00 PLN"))));
    }

    @Test
    void depositAndWithdrawalUpdateBothBalancesAndKeepHistory() throws Exception {
        long goalId = createGoal("anna", "Wakacje", "1000.00");
        assertAmount("anna", "3000.00");

        mockMvc.perform(post("/goals/{id}/deposit", goalId)
                        .with(user("anna")).with(csrf()).param("amount", "400.00"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/goals/{id}/withdraw", goalId)
                        .with(user("anna")).with(csrf()).param("amount", "100.00"))
                .andExpect(status().is3xxRedirection());

        assertAmount("anna", "2700.00");
        var goal = goalRepository.findByIdAndAccountCustomerUsername(goalId, "anna").orElseThrow();
        assertEquals(0, goal.getBalance().compareTo(new BigDecimal("300.00")));
        assertEquals(2, operationRepository.findAllByGoalIdOrderByOccurredAtDescIdDesc(goalId).size());
        assertEquals(OperationType.WITHDRAWAL,
                operationRepository.findAllByGoalIdOrderByOccurredAtDescIdDesc(goalId).getFirst().getType());
        assertEquals(0, savingsService.getGoalDetails(goalId, "anna")
                .remainingAmount().compareTo(new BigDecimal("700.00")));
    }

    @Test
    void goalCreationStartsAtZeroAndDoesNotChangeAccountBalance() throws Exception {
        mockMvc.perform(post("/goals")
                        .with(user("anna")).with(csrf())
                        .param("name", "  Wakacje  ")
                        .param("targetAmount", "1000.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/"));

        var goal = goalRepository.findAllByAccountCustomerUsernameOrderByIdDesc("anna").getFirst();
        assertEquals("Wakacje", goal.getName());
        assertEquals(0, goal.getTargetAmount().compareTo(new BigDecimal("1000.00")));
        assertEquals(0, goal.getBalance().compareTo(BigDecimal.ZERO.setScale(2)));
        assertAmount("anna", "3000.00");
    }

    @Test
    void invalidGoalNamesAndTargetPrecisionDoNotCreateGoal() throws Exception {
        mockMvc.perform(post("/goals")
                        .with(user("anna")).with(csrf())
                        .param("name", "   ")
                        .param("targetAmount", "1000.00"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Podaj nazwę skarbonki.")));
        mockMvc.perform(post("/goals")
                        .with(user("anna")).with(csrf())
                        .param("name", "Wakacje")
                        .param("targetAmount", "1000.001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("maksymalnie dwa miejsca po przecinku")));

        assertTrue(goalRepository.findAllByAccountCustomerUsernameOrderByIdDesc("anna").isEmpty());
        assertAmount("anna", "3000.00");
    }

    @Test
    void rejectsInsufficientAndInvalidAmountsWithoutChangingData() throws Exception {
        long goalId = createGoal("anna", "Wakacje", "1000.00");

        mockMvc.perform(post("/goals/{id}/deposit", goalId)
                        .with(user("anna")).with(csrf()).param("amount", "3000.01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", containsString("Brak wystarczających środków")));
        mockMvc.perform(post("/goals/{id}/withdraw", goalId)
                        .with(user("anna")).with(csrf()).param("amount", "0.01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", containsString("Brak wystarczających środków")));
        mockMvc.perform(post("/goals/{id}/deposit", goalId)
                        .with(user("anna")).with(csrf()).param("amount", "10.001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("maksymalnie dwa miejsca po przecinku")));
        mockMvc.perform(post("/goals/{id}/deposit", goalId)
                        .with(user("anna")).with(csrf()).param("amount", "niepoprawna"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Podaj kwotę w poprawnym formacie.")));
        mockMvc.perform(post("/goals")
                        .with(user("anna")).with(csrf())
                        .param("name", "Niepoprawny cel")
                        .param("targetAmount", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Kwota docelowa musi być większa od zera.")));

        assertAmount("anna", "3000.00");
        assertEquals(1, goalRepository.findAllByAccountCustomerUsernameOrderByIdDesc("anna").size());
        assertEquals(0, goalRepository.findByIdAndAccountCustomerUsername(goalId, "anna").orElseThrow()
                .getBalance().compareTo(BigDecimal.ZERO.setScale(2)));
        assertTrue(operationRepository.findAllByGoalIdOrderByOccurredAtDescIdDesc(goalId).isEmpty());
    }

    @Test
    void zeroNegativeAndOverPrecisionAmountsAreRejectedForBothOperationTypes() throws Exception {
        long goalId = createGoal("anna", "Wakacje", "1000.00");

        for (String amount : new String[] {"0", "-1.00"}) {
            mockMvc.perform(post("/goals/{id}/deposit", goalId)
                            .with(user("anna")).with(csrf()).param("amount", amount))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Kwota musi być większa od zera.")));
            mockMvc.perform(post("/goals/{id}/withdraw", goalId)
                            .with(user("anna")).with(csrf()).param("amount", amount))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Kwota musi być większa od zera.")));
        }
        for (String path : new String[] {"deposit", "withdraw"}) {
            mockMvc.perform(post("/goals/{id}/" + path, goalId)
                            .with(user("anna")).with(csrf()).param("amount", "1.001"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("maksymalnie dwa miejsca po przecinku")));
        }

        assertAmount("anna", "3000.00");
        assertEquals(0, goalRepository.findByIdAndAccountCustomerUsername(goalId, "anna").orElseThrow()
                .getBalance().compareTo(BigDecimal.ZERO.setScale(2)));
        assertTrue(operationRepository.findAllByGoalIdOrderByOccurredAtDescIdDesc(goalId).isEmpty());
    }

    @Test
    void goalDetailsRenderCalculatedAmountsAndNewestOperationFirst() throws Exception {
        long goalId = createGoal("anna", "Wakacje", "1000.00");
        mockMvc.perform(post("/goals/{id}/deposit", goalId)
                        .with(user("anna")).with(csrf()).param("amount", "400.00"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/goals/{id}/withdraw", goalId)
                        .with(user("anna")).with(csrf()).param("amount", "100.00"))
                .andExpect(status().is3xxRedirection());

        MvcResult result = mockMvc.perform(get("/goals/{id}", goalId).with(user("anna")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("300,00")))
                .andExpect(content().string(containsString("700,00")))
                .andExpect(content().string(containsString("Historia operacji")))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        String historyTable = html.substring(html.indexOf("<tbody>"), html.indexOf("</tbody>"));
        assertTrue(historyTable.indexOf("Wycofanie") < historyTable.indexOf("Zasilenie"));
    }

    @Test
    void goalCanExceedTargetAndWithdrawalCanMoveItBelowTarget() throws Exception {
        long goalId = createGoal("anna", "Wakacje", "1000.00");
        mockMvc.perform(post("/goals/{id}/deposit", goalId)
                        .with(user("anna")).with(csrf()).param("amount", "1200.00"))
                .andExpect(status().is3xxRedirection());

        var achieved = savingsService.getGoalDetails(goalId, "anna");
        assertTrue(achieved.achieved());
        assertEquals(0, achieved.remainingAmount().compareTo(BigDecimal.ZERO));

        mockMvc.perform(post("/goals/{id}/withdraw", goalId)
                        .with(user("anna")).with(csrf()).param("amount", "300.00"))
                .andExpect(status().is3xxRedirection());
        var belowTarget = savingsService.getGoalDetails(goalId, "anna");
        assertFalse(belowTarget.achieved());
        assertEquals(0, belowTarget.balance().compareTo(new BigDecimal("900.00")));
        assertEquals(0, belowTarget.remainingAmount().compareTo(new BigDecimal("100.00")));
    }

    @Test
    void anotherCustomerCannotReadOrChangeGoal() throws Exception {
        long goalId = createGoal("anna", "Prywatna", "500.00");

        mockMvc.perform(get("/goals/{id}", goalId).with(user("patryk")))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/goals/{id}/deposit", goalId)
                        .with(user("patryk")).with(csrf()).param("amount", "100.00"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/goals/999999").with(user("anna")))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Strona lub skarbonka jest niedostępna.")));

        assertAmount("anna", "3000.00");
        assertAmount("patryk", "3000.00");
        assertEquals(0, goalRepository.findByIdAndAccountCustomerUsername(goalId, "anna").orElseThrow()
                .getBalance().compareTo(BigDecimal.ZERO.setScale(2)));
        assertTrue(operationRepository.findAllByGoalIdOrderByOccurredAtDescIdDesc(goalId).isEmpty());
    }

    @Test
    void concurrentDepositsCannotSpendTheSameAccountFundsTwice() throws Exception {
        long goalId = createGoal("anna", "Wakacje", "5000.00");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<MvcResult> first = executor.submit(() -> concurrentDeposit(goalId, ready, start));
            Future<MvcResult> second = executor.submit(() -> concurrentDeposit(goalId, ready, start));
            ready.await();
            start.countDown();
            MvcResult firstResult = first.get();
            MvcResult secondResult = second.get();

            int failedOperations = (firstResult.getFlashMap().containsKey("error") ? 1 : 0)
                    + (secondResult.getFlashMap().containsKey("error") ? 1 : 0);
            assertEquals(1, failedOperations);
        }

        assertAmount("anna", "1000.00");
        assertEquals(0, goalRepository.findByIdAndAccountCustomerUsername(goalId, "anna").orElseThrow()
                .getBalance().compareTo(new BigDecimal("2000.00")));
        assertEquals(1, operationRepository.findAllByGoalIdOrderByOccurredAtDescIdDesc(goalId).size());
    }

    private MvcResult concurrentDeposit(long goalId, CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        start.await();
        return mockMvc.perform(post("/goals/{id}/deposit", goalId)
                        .with(user("anna")).with(csrf()).param("amount", "2000.00"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }

    private long createGoal(String username, String name, String target) throws Exception {
        MvcResult result = mockMvc.perform(post("/goals")
                        .with(user(username)).with(csrf())
                        .param("name", name)
                        .param("targetAmount", target))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        String location = result.getResponse().getRedirectedUrl();
        assertEquals("/", location);
        return goalRepository.findAllByAccountCustomerUsernameOrderByIdDesc(username).getFirst().getId();
    }

    private void assertAmount(String username, String expected) {
        assertEquals(0, accountRepository.findByCustomerUsername(username).orElseThrow()
                .getBalance().compareTo(new BigDecimal(expected)));
    }
}

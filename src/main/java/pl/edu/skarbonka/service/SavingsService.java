package pl.edu.skarbonka.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import pl.edu.skarbonka.model.Account;
import pl.edu.skarbonka.model.OperationType;
import pl.edu.skarbonka.model.SavingsGoal;
import pl.edu.skarbonka.model.SavingsOperation;
import pl.edu.skarbonka.repository.AccountRepository;
import pl.edu.skarbonka.repository.CustomerRepository;
import pl.edu.skarbonka.repository.SavingsGoalRepository;
import pl.edu.skarbonka.repository.SavingsOperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SavingsService {

    private static final DateTimeFormatter HISTORY_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MONTH_LABEL_FORMAT =
            DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("pl-PL"));

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final SavingsGoalRepository goalRepository;
    private final SavingsOperationRepository operationRepository;

    public SavingsService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            SavingsGoalRepository goalRepository,
            SavingsOperationRepository operationRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.goalRepository = goalRepository;
        this.operationRepository = operationRepository;
    }

    @Transactional(readOnly = true)
    public DashboardView getDashboard(String username) {
        Account account = accountRepository.findByCustomerUsername(username)
                .orElseThrow(GoalNotFoundException::new);
        List<GoalSummary> goals = goalRepository
                .findAllByAccountCustomerUsernameOrderByIdDesc(username)
                .stream()
                .map(this::toSummary)
                .toList();
        List<MonthlyStatistics> monthlyStatistics = monthlyStatistics(username);
        AccountStatistics accountStatistics = accountStatistics(goals, monthlyStatistics);
        return new DashboardView(account.getBalance(), goals, monthlyStatistics, accountStatistics);
    }

    @Transactional
    public void createGoal(String username, String name, BigDecimal targetAmount) {
        Account account = accountRepository.findByCustomerUsername(username)
                .orElseThrow(GoalNotFoundException::new);
        goalRepository.save(new SavingsGoal(account, name.trim(), targetAmount));
    }

    @Transactional(readOnly = true)
    public GoalDetails getGoalDetails(Long goalId, String username) {
        SavingsGoal goal = goalRepository.findByIdAndAccountCustomerUsername(goalId, username)
                .orElseThrow(GoalNotFoundException::new);
        List<OperationView> operations = operationRepository
                .findAllByGoalIdOrderByOccurredAtDescIdDesc(goalId)
                .stream()
                .map(operation -> new OperationView(
                        operation.getType(),
                        operation.getAmount(),
                        operation.getOccurredAt().format(HISTORY_TIME_FORMAT)))
                .toList();
        return toDetails(goal, operations);
    }

    @Transactional
    public void deposit(Long goalId, String username, BigDecimal amount) {
        Account account = accountRepository.findByCustomerUsernameForUpdate(username)
                .orElseThrow(GoalNotFoundException::new);
        SavingsGoal goal = goalRepository.findByIdAndAccountCustomerUsername(goalId, username)
                .orElseThrow(GoalNotFoundException::new);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Brak wystarczających środków na rachunku.");
        }

        account.setBalance(account.getBalance().subtract(amount));
        goal.setBalance(goal.getBalance().add(amount));
        operationRepository.save(new SavingsOperation(goal, OperationType.DEPOSIT, amount, LocalDateTime.now()));
    }

    @Transactional
    public void withdraw(Long goalId, String username, BigDecimal amount) {
        Account account = accountRepository.findByCustomerUsernameForUpdate(username)
                .orElseThrow(GoalNotFoundException::new);
        SavingsGoal goal = goalRepository.findByIdAndAccountCustomerUsername(goalId, username)
                .orElseThrow(GoalNotFoundException::new);
        if (goal.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Brak wystarczających środków w skarbonce.");
        }

        goal.setBalance(goal.getBalance().subtract(amount));
        account.setBalance(account.getBalance().add(amount));
        operationRepository.save(new SavingsOperation(goal, OperationType.WITHDRAWAL, amount, LocalDateTime.now()));
    }

    private GoalSummary toSummary(SavingsGoal goal) {
        return new GoalSummary(
                goal.getId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getBalance(),
                remainingAmount(goal),
                goal.getBalance().compareTo(goal.getTargetAmount()) >= 0,
                progressPercent(goal));
    }

    private int progressPercent(SavingsGoal goal) {
        if (goal.getBalance().compareTo(goal.getTargetAmount()) >= 0) {
            return 100;
        }
        return goal.getBalance()
                .multiply(BigDecimal.valueOf(100))
                .divide(goal.getTargetAmount(), 0, RoundingMode.DOWN)
                .intValue();
    }

    private List<MonthlyStatistics> monthlyStatistics(String username) {
        Map<YearMonth, MonthlyTotals> totalsByMonth = new TreeMap<>();
        for (SavingsOperation operation : operationRepository.findAllForCustomerOrderByOccurredAt(username)) {
            YearMonth month = YearMonth.from(operation.getOccurredAt());
            totalsByMonth.computeIfAbsent(month, ignored -> new MonthlyTotals())
                    .add(operation.getType(), operation.getAmount());
        }
        if (totalsByMonth.isEmpty()) {
            return List.of();
        }

        BigDecimal maximumMonthlyValue = totalsByMonth.values().stream()
                .map(MonthlyTotals::maximum)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        List<MonthlyStatistics> statistics = new ArrayList<>();
        totalsByMonth.forEach((month, totals) -> statistics.add(new MonthlyStatistics(
                month.atDay(1).format(MONTH_LABEL_FORMAT),
                totals.deposits,
                totals.withdrawals,
                chartPercent(totals.deposits, maximumMonthlyValue),
                chartPercent(totals.withdrawals, maximumMonthlyValue))));
        return List.copyOf(statistics);
    }

    private int chartPercent(BigDecimal amount, BigDecimal maximum) {
        if (amount.signum() == 0) {
            return 0;
        }
        return amount.multiply(BigDecimal.valueOf(100))
                .divide(maximum, 0, RoundingMode.HALF_UP)
                .max(BigDecimal.ONE)
                .intValue();
    }

    private AccountStatistics accountStatistics(
            List<GoalSummary> goals,
            List<MonthlyStatistics> monthlyStatistics) {
        BigDecimal totalSaved = goals.stream()
                .map(GoalSummary::balance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDeposits = monthlyStatistics.stream()
                .map(MonthlyStatistics::deposits)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalWithdrawals = monthlyStatistics.stream()
                .map(MonthlyStatistics::withdrawals)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int achievedGoalCount = (int) goals.stream().filter(GoalSummary::achieved).count();
        return new AccountStatistics(
                totalSaved,
                totalDeposits,
                totalWithdrawals,
                goals.size(),
                achievedGoalCount);
    }

    private GoalDetails toDetails(SavingsGoal goal, List<OperationView> operations) {
        return new GoalDetails(
                goal.getId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getBalance(),
                remainingAmount(goal),
                goal.getBalance().compareTo(goal.getTargetAmount()) >= 0,
                operations);
    }

    private BigDecimal remainingAmount(SavingsGoal goal) {
        return goal.getTargetAmount().subtract(goal.getBalance()).max(BigDecimal.ZERO);
    }

    private static class MonthlyTotals {
        private BigDecimal deposits = BigDecimal.ZERO;
        private BigDecimal withdrawals = BigDecimal.ZERO;

        private void add(OperationType type, BigDecimal amount) {
            if (type == OperationType.DEPOSIT) {
                deposits = deposits.add(amount);
            } else {
                withdrawals = withdrawals.add(amount);
            }
        }

        private BigDecimal maximum() {
            return deposits.max(withdrawals);
        }
    }
}

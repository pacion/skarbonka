package pl.edu.skarbonka.web;

import jakarta.validation.Valid;
import pl.edu.skarbonka.service.GoalDetails;
import pl.edu.skarbonka.service.InsufficientFundsException;
import pl.edu.skarbonka.service.SavingsService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PageController {

    private final SavingsService savingsService;

    public PageController(SavingsService savingsService) {
        this.savingsService = savingsService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String dashboard(Authentication authentication, Model model) {
        model.addAttribute("dashboard", savingsService.getDashboard(authentication.getName()));
        model.addAttribute("goalForm", new GoalForm());
        return "dashboard";
    }

    @PostMapping("/goals")
    public String createGoal(
            @Valid @ModelAttribute("goalForm") GoalForm goalForm,
            BindingResult bindingResult,
            Authentication authentication,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("dashboard", savingsService.getDashboard(authentication.getName()));
            return "dashboard";
        }
        savingsService.createGoal(authentication.getName(), goalForm.getName(), goalForm.getTargetAmount());
        return "redirect:/";
    }

    @GetMapping("/goals/{id}")
    public String goalDetails(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {
        addGoalPageModel(id, authentication.getName(), model);
        return "goal";
    }

    @PostMapping("/goals/{id}/deposit")
    public String deposit(
            @PathVariable Long id,
            @Valid @ModelAttribute("depositForm") OperationForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addGoalPageModel(id, authentication.getName(), model);
            return "goal";
        }
        try {
            savingsService.deposit(id, authentication.getName(), form.getAmount());
        } catch (InsufficientFundsException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/goals/" + id;
    }

    @PostMapping("/goals/{id}/withdraw")
    public String withdraw(
            @PathVariable Long id,
            @Valid @ModelAttribute("withdrawForm") OperationForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addGoalPageModel(id, authentication.getName(), model);
            return "goal";
        }
        try {
            savingsService.withdraw(id, authentication.getName(), form.getAmount());
        } catch (InsufficientFundsException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/goals/" + id;
    }

    private void addGoalPageModel(Long id, String username, Model model) {
        GoalDetails goal = savingsService.getGoalDetails(id, username);
        model.addAttribute("goal", goal);
        if (!model.containsAttribute("depositForm")) {
            model.addAttribute("depositForm", new OperationForm());
        }
        if (!model.containsAttribute("withdrawForm")) {
            model.addAttribute("withdrawForm", new OperationForm());
        }
    }
}

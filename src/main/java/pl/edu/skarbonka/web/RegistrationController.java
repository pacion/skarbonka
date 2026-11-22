package pl.edu.skarbonka.web;

import jakarta.validation.Valid;
import pl.edu.skarbonka.service.RegistrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String registrationForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registrationForm") RegistrationForm form,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        if (!registrationService.register(
                form.getUsername(),
                form.getFullName(),
                form.getPassword(),
                form.getInitialBalance())) {
            bindingResult.rejectValue("username", "username.duplicate", "Ten login jest już zajęty.");
            return "register";
        }
        return "redirect:/login?registered";
    }
}

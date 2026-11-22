package pl.edu.skarbonka.web;

import pl.edu.skarbonka.service.GoalNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GoalNotFoundHandler {

    @ExceptionHandler(GoalNotFoundException.class)
    public ModelAndView handleGoalNotFound() {
        ModelAndView page = new ModelAndView("error/404");
        page.setStatus(HttpStatus.NOT_FOUND);
        return page;
    }
}

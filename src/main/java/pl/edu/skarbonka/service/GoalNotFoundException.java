package pl.edu.skarbonka.service;

public class GoalNotFoundException extends RuntimeException {

    public GoalNotFoundException() {
        super("Nie znaleziono skarbonki.");
    }
}

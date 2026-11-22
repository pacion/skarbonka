# Wirtualne skarbonki

Prosta aplikacja uczelniana do zarządzania celami oszczędnościowymi. Symuluje rachunek klienta, skarbonki i historię operacji; nie łączy się z bankiem ani z prawdziwymi środkami.

## Wymagania i uruchomienie

- Java 21
- Dostęp do internetu przy pierwszym uruchomieniu, aby Maven Wrapper pobrał Maven i zależności
- Przeglądarka internetowa

Uruchom aplikację:

```sh
./mvnw spring-boot:run
```

Otwórz <http://localhost:8080>. Testy uruchomisz poleceniem:

```sh
./mvnw test
```

Na ekranie logowania wybierz **Zarejestruj się**, aby utworzyć konto. Formularz przyjmuje imię i nazwisko, login, hasło oraz saldo początkowe (również 0,00 PLN). Saldo jest wyłącznie symulacją demonstracyjną, nie prawdziwymi środkami. Kwoty w interfejsie są formatowane zgodnie z polskimi separatorami. Pulpit pokazuje postęp każdej skarbonki oraz miesięczne sumy wpłat i wypłat z całej historii, rozdzielone według miesiąca i roku.

## Konta demonstracyjne

Migracje Flyway tworzą po dwa cele i pięć operacji dla każdego konta, dzięki czemu wykresy i statystyki są wypełnione od pierwszego uruchomienia. Salda początkowe przed przykładowymi operacjami wynoszą 3000,00 PLN.

| Login | Hasło | Saldo rachunku po przykładowych operacjach |
|---|---|---:|
| `anna` | `anna` | 1200,00 PLN |
| `patryk` | `patryk` | 1000,00 PLN |

Hasła zapisane w migracji są hashami BCrypt. Baza H2 działa w pamięci, więc po zatrzymaniu i ponownym uruchomieniu aplikacji Flyway odtwarza schemat i dane demonstracyjne.

## Krótki scenariusz demo

Zaloguj się jako `anna` i zobacz przykładową historię oraz statystyki. Jej skarbonka „Wakacje” ma 600,00 PLN przy celu 1000,00 PLN, a rachunek 1200,00 PLN. Zasil ją kwotą 400,00 PLN, a następnie wycofaj 100,00 PLN: rachunek będzie miał 900,00 PLN, skarbonka 900,00 PLN, do celu będzie brakować 100,00 PLN. Historia „Wakacje” będzie zawierała pięć wpisów. Szczegółowy scenariusz i dane migracji opisano w [dokumentacji projektu](docs/projekt.md) i [scenariuszu demo](docs/demo.md).

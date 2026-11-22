# Scenariusz prezentacji — Wirtualne skarbonki

## Przebieg pokazu

Zacznij od świeżego uruchomienia aplikacji, aby baza H2 zawierała dane startowe.

Możesz również pokazać rejestrację: na ekranie logowania wybierz „Zarejestruj się”, wprowadź imię i nazwisko, nowy login, hasło o długości co najmniej 8 znaków oraz saldo początkowe (np. 0,00 PLN). Po utworzeniu konta zaloguj się nim i sprawdź, że wskazane saldo jest widoczne na pulpicie. To saldo jest symulowane.

| Krok | Czynność i dane wejściowe | Oczekiwany wynik |
|---|---|---|
| 1 | Otwórz `http://localhost:8080`, zaloguj się jako `anna` hasłem `anna`. | Pulpit pokazuje saldo 1200,00 PLN, skarbonki „Wakacje” i „Nowy laptop”, karty statystyk oraz kolumnowy wykres operacji. |
| 2 | Odczytaj przykładowe statystyki Anny. | Łącznie wpłacono 2000,00 PLN, wypłacono 200,00 PLN, a w skarbonkach jest 1800,00 PLN. Wykres pokazuje miesiące od grudnia 2025 do października 2026. |
| 3 | Otwórz „Wakacje” i zasil ją kwotą 400,00 PLN. | Rachunek ma 800,00 PLN, skarbonka 1000,00 PLN i cel jest osiągnięty. |
| 4 | Wycofaj 100,00 PLN. | Rachunek ma 900,00 PLN, skarbonka 900,00 PLN, do celu brakuje 100,00 PLN. Historia „Wakacje” ma pięć wpisów. |
| 5 | Wróć na pulpit. | Statystyki i wykres uwzględniają nowe operacje; wpłaty i wypłaty są pokazane oddzielnie. |
| 6 | Spróbuj zasilić skarbonkę kwotą 1000,00 PLN. | Operacja jest odrzucona komunikatem o braku środków; salda i historia pozostają bez zmian. |
| 7 | Wyloguj się, zaloguj jako `patryk` hasłem `patryk` i otwórz adres skarbonki Anny z kroku 3. | Pulpit Patryka pokazuje jego własne dane, w tym 1000,00 PLN dostępnego salda; adres skarbonki Anny zwraca polski 404. |

## Lista zrzutów ekranu do samodzielnego wykonania

1. **Logowanie** — formularz logowania; nie pokazuj hasła podczas wpisywania.
2. **Rejestracja** — formularz danych klienta i pola salda początkowego.
3. **Pulpit Anny przed operacjami** — saldo 1200,00 PLN, karty statystyk, wykres postępu i kolumnowy wykres miesięcznych wpłat/wypłat.
4. **Szczegóły „Wakacje” przed operacjami** — saldo 600,00 PLN, cel 1000,00 PLN i przykładowa historia.
5. **Szczegóły „Wakacje” po operacjach** — saldo 900,00 PLN, brakujące 100,00 PLN oraz pięć wpisów historii z najnowszym wycofaniem na górze.
6. **Odrzucona wpłata** — polski komunikat o braku środków po próbie z kroku 6.
7. **Brak dostępu drugiego klienta** — polski ekran 404 po otwarciu adresu skarbonki Anny jako Patryk.

Nie przygotowano zrzutów ani atrap ekranów. Test integracyjny sprawdza kod i treść odpowiedzi 404; zrzut należy wykonać samodzielnie po uruchomieniu aplikacji.

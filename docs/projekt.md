# Dokumentacja projektu — Wirtualne skarbonki

## 1. Organizacja i jej działalność

Projekt przedstawia fikcyjny Bank Przykład, bank detaliczny obsługujący klientów indywidualnych. W rzeczywistej bankowości internetowej klient korzystałby między innymi z rachunku płatniczego oraz funkcji pomagających planować oszczędności. Moduł skarbonek byłby dodatkiem do bankowości elektronicznej, pokazującym cele i przesunięcia środków.

Aplikacja uczelniana symuluje wyłącznie saldo rachunku, skarbonki klienta i historię tych operacji. Nie realizuje działalności bankowej, nie przechowuje prawdziwych pieniędzy i nie komunikuje się z systemami banku ani zewnętrznymi usługami.

## 2. Problem i kontekst

System pozwala klientowi wydzielić część dostępnego salda na nazwany cel. Klient tworzy skarbonkę, podając nazwę i dodatnią kwotę docelową. Następnie może zasilać ją środkami z rachunku lub wycofywać środki ze skarbonki. Cel jest osiągnięty, gdy saldo skarbonki jest co najmniej równe kwocie docelowej; wpłaty nie są przez nią ograniczone.

Przykładowym dokumentem powstającym w procesie jest historia operacji skarbonki. Dla przykładu demonstracyjnego po zasileniu 400,00 PLN i wycofaniu 100,00 PLN historia zawiera:

| Typ | Kwota |
|---|---:|
| Wycofanie | 100,00 PLN |
| Zasilenie | 400,00 PLN |

Widok prezentuje wpisy od najnowszego. Para 400,00/100,00 PLN ilustruje sekwencję opisaną w testach. Dodatkowa migracja demo tworzy przykładowe skarbonki i historię dla Anny i Patryka, aby pulpit i wykresy były wypełnione po uruchomieniu.

## 3. Użytkownicy, role i uprawnienia

Jedyną rolą aplikacyjną jest klient (`ROLE_CUSTOMER`). Nowy klient może zarejestrować się, podając imię i nazwisko, unikalny login, hasło oraz symulowane saldo początkowe. Saldo może wynosić zero; nie ma limitu biznesowego, a jego techniczny zakres wynika z kolumny `DECIMAL(38,2)`. Klient może odczytywać własny pulpit i skarbonki, zakładać skarbonki oraz wykonywać na nich wpłaty i wypłaty. Identyfikator właściciela pochodzi z zalogowanej sesji, a pobranie skarbonki jest ograniczone do tego klienta. Próba odczytu lub zmiany cudzej albo nieistniejącej skarbonki kończy się odpowiedzią 404 i polskim komunikatem.

Dane demonstracyjne po migracji przykładowych operacji:

| Login | Hasło | Saldo rachunku | Skarbonki (saldo / cel) |
|---|---|---:|---|
| `anna` | `anna` | 1200,00 PLN | Wakacje 600,00 / 1000,00 PLN; Nowy laptop 1200,00 / 3000,00 PLN |
| `patryk` | `patryk` | 1000,00 PLN | Rower 1500,00 / 2500,00 PLN; Poduszka finansowa 500,00 / 500,00 PLN |

Oba rachunki zaczynają z saldem 3000,00 PLN w migracji bazowej. Migracja demo zapisuje historię, skarbonki i odpowiadające im salda; dlatego powyższe dostępne salda są niższe. Hasła przechowuje się jako hashe BCrypt. Administracja nie jest dostępna.

Przykładowe sumy widoczne na wykresie miesięcznym po migracji:

| Klient | Miesiąc | Wpłaty | Wypłaty |
|---|---|---:|---:|
| Anna | grudzień 2025 | 300,00 PLN | 0,00 PLN |
| Anna | styczeń 2026 | 1100,00 PLN | 0,00 PLN |
| Anna | luty 2026 | 0,00 PLN | 200,00 PLN |
| Anna | październik 2026 | 600,00 PLN | 0,00 PLN |
| Patryk | grudzień 2025 | 500,00 PLN | 0,00 PLN |
| Patryk | marzec 2026 | 800,00 PLN | 0,00 PLN |
| Patryk | kwiecień 2026 | 0,00 PLN | 100,00 PLN |
| Patryk | lipiec 2026 | 500,00 PLN | 0,00 PLN |
| Patryk | październik 2026 | 300,00 PLN | 0,00 PLN |

## 4. Założenia i zależności

Aplikacja działa lokalnie w przeglądarce jako pojedyncza aplikacja Spring Boot. Do uruchomienia potrzebna jest Java 21; Maven i zależności pobierane są przez Maven Wrapper. Baza H2 działa in-memory, a Flyway podczas uruchomienia tworzy schemat, dane kont oraz przykładowe cele i operacje. Ponowne uruchomienie procesu odtwarza bazę wraz z historią demo. Hibernate sprawdza zgodność mapowań ze schematem, nie tworzy go ani nie aktualizuje.

Nie ma integracji z prawdziwym bankiem, zewnętrznym API, innymi walutami, powiadomieniami, eksportem ani trwałym magazynem danych. Brak administracji, edycji i usuwania skarbonek. Salda ustawione przy rejestracji są wyłącznie danymi demonstracyjnymi. Interfejs korzysta z lokalnego arkusza CSS i elementu HTML `progress`; nie wymaga zewnętrznych usług ani bibliotek wykresów.

## 5. Wymagania funkcjonalne

| ID | Funkcja i kryterium akceptacji |
|---|---|
| F1 | Klient loguje się loginem i hasłem; poprawne dane otwierają pulpit, błędne wyświetlają komunikat. |
| F2 | Klient widzi własne saldo oraz listę i statystyki własnych skarbonek; rachunki demo mają saldo 3000,00 PLN przed uwzględnieniem przykładowych operacji migracji. |
| F3 | Klient tworzy skarbonkę z niepustą nazwą i dodatnim celem o skali do dwóch miejsc; saldo nowej skarbonki wynosi 0,00 PLN, a saldo rachunku nie zmienia się. |
| F4 | Klient zasila własną skarbonkę; przy środkach 3000,00 PLN wpłata 400,00 PLN pozostawia rachunek 2600,00 PLN i skarbonkę 400,00 PLN. |
| F5 | Klient wycofuje środki do wysokości salda skarbonki; po wycofaniu 100,00 PLN z 400,00 PLN rachunek ma 2700,00 PLN, a skarbonka 300,00 PLN. |
| F6 | Klient widzi saldo, cel, brakującą kwotę i stan osiągnięcia celu; osiągnięty cel ma brakującą kwotę 0,00 PLN, a wypłata poniżej progu przywraca status nieosiągnięty. |
| F7 | Klient widzi historię operacji od najnowszej; każda poprawna wpłata lub wypłata ma wpis typu, kwoty i czasu. |
| F8 | Klient nie może odczytać ani zmienić cudzej skarbonki; oba przypadki zwracają 404. |
| F9 | Niepoprawna kwota oraz operacja przekraczająca dostępne saldo są odrzucane bez zmiany sald ani historii i komunikowane po polsku. |
| F10 | Gość może utworzyć konto, ustawiając saldo początkowe równe zero lub większe; konto otrzymuje rolę klienta, a hasło jest zapisywane jako BCrypt. Ponowny login użytkownika jest odrzucany. |
| F11 | Klient widzi na pulpicie wykres paskowy postępu każdej własnej skarbonki względem celu; postęp jest ograniczony do 100% także po przekroczeniu celu. |
| F12 | Kwoty są prezentowane z polskimi separatorami dziesiętnymi i tysięcy, a brak strony lub dostępnej skarbonki pokazuje polską stronę 404. |
| F13 | Klient widzi miesięczne sumy wpłat i wypłat ze wszystkich własnych skarbonek; miesiące z różnych lat są osobnymi grupami, a wykres obejmuje całą dostępną historię. |
| F14 | Klient widzi podsumowanie rachunku: bieżącą sumę w skarbonkach, liczbę celów, cele osiągnięte oraz łączne wpłaty i wypłaty. |

## 6. Wymagania niefunkcjonalne

| ID | Wymaganie i sposób weryfikacji |
|---|---|
| NF1 | Izolacja danych: odczyt i zapis celu odbywa się w kontekście właściciela z sesji; weryfikują to testy MVC próbujące odczytu i zapisu jako drugi klient. |
| NF2 | Spójność sald i historii: zmiana obu sald oraz zapis operacji są jedną transakcją; test integracyjny sprawdza komplet wartości po wpłacie i wypłacie oraz brak zmian po odrzuceniu. |
| NF3 | Równoczesne operacje nie wydają tych samych środków: wpłaty i wypłaty blokują rachunek właściciela w tej samej kolejności; test współbieżny uruchamia dwie wpłaty po 2000,00 PLN przy saldzie 3000,00 PLN i sprawdza, że tylko jedna się powiedzie. |
| NF4 | Kwoty są reprezentowane jako `BigDecimal` i kolumny `DECIMAL` ze skalą 2 (saldo rachunku `DECIMAL(38,2)`, pozostałe kwoty `DECIMAL(14,2)`); walidacja formularza i test nieprawidłowej skali sprawdzają ograniczenie do dwóch miejsc. |
| NF5 | Lokalnie aplikacja wymaga Java 21 i dostępnej przeglądarki; weryfikacja polega na uruchomieniu `./mvnw spring-boot:run` i otwarciu strony logowania na `localhost:8080`. W tej dokumentacji nie deklaruje się pomiarów wydajności ani dostępności. |

## 7. Projekt koncepcyjny

Warstwa MVC (`PageController`, `RegistrationController` i szablony Thymeleaf) obsługuje zwykłe formularze HTML. Spring Security zapewnia logowanie formularzem, sesję i domyślną ochronę CSRF. Formularze są walidowane przez Bean Validation. `RegistrationService` tworzy klienta i rachunek w jednej transakcji oraz zapisuje hasło BCrypt. `SavingsService` zawiera reguły biznesowe i transakcje, repozytoria Spring Data JPA odczytują i zapisują encje, a Hibernate mapuje je na schemat zarządzany przez Flyway.

Model danych:

- `Customer` przechowuje login, hash hasła i nazwę klienta.
- `Account` ma relację jeden-do-jednego z klientem i dostępne saldo.
- `SavingsGoal` należy do rachunku i zawiera nazwę, cel oraz saldo.
- `SavingsOperation` należy do skarbonki i przechowuje typ, kwotę oraz czas.

Zasilenie przebiega następująco:

1. Klient wysyła formularz kwoty; walidacja sprawdza, czy jest dodatnia i ma najwyżej dwa miejsca po przecinku.
2. Kontroler przekazuje identyfikator celu i nazwę użytkownika z uwierzytelnionej sesji. Tożsamość właściciela nie pochodzi z formularza.
3. Serwis rozpoczyna transakcję i blokuje wiersz rachunku klienta (`PESSIMISTIC_WRITE`), zanim odczyta salda. Wycofanie stosuje tę samą kolejność blokowania.
4. Serwis pobiera skarbonkę tylko wtedy, gdy należy do tego klienta. Cudzy lub nieistniejący cel kończy się 404.
5. Jeśli saldo rachunku jest za małe, serwis zgłasza błąd przed zmianą danych; transakcja nie zapisuje sald ani historii.
6. W przeciwnym razie serwis zmniejsza saldo rachunku, zwiększa saldo skarbonki i zapisuje wpis `DEPOSIT` w tej samej transakcji. Po sukcesie kontroler przekierowuje na widok GET.

Kwota brakująca i status osiągnięcia celu są obliczane z bieżących sald podczas odczytu, nie są przechowywane w bazie.

Podczas rejestracji serwis normalizuje login, sprawdza jego unikalność, hashuje hasło i zapisuje klienta wraz z rachunkiem o saldzie z formularza. Wykres postępu skarbonek jest renderowany po stronie serwera jako paski HTML `progress`; procent wynika z salda skarbonki i celu, a wartość prezentacyjna jest ograniczona do 100%. Drugi wykres kolumnowy agreguje kwoty z historii operacji według miesiąca i rozdziela je na wpłaty oraz wypłaty. Karty statystyk pokazują bieżącą sumę oszczędności, liczbę skarbonek, liczbę osiągniętych celów oraz łączne wpłaty i wypłaty. Zapytanie historii ogranicza operacje do zalogowanego klienta, a miesiące są uporządkowane chronologicznie z uwzględnieniem roku. Kwoty formatuje lokalny komponent Java, a wyjątek braku skarbonki zwraca szablon 404 bez ujawniania, czy cel istnieje dla innego klienta.

## 8. Realizacja i weryfikacja

Zaimplementowano logowanie, rejestrację, ustawianie początkowego salda, pulpit z kartami statystyk i wykresami, przykładowe dane migracyjne Anny i Patryka, formatowanie kwot, tworzenie skarbonki, wpłaty, wypłaty, historię, izolację właściciela, polską stronę 404 i komunikaty oraz blokowanie rachunku dla operacji finansowych. Testy MVC pokrywają spójność przykładowych migracji, rejestrację, wybrane saldo i BCrypt, odrzucenie powtórzonego loginu i salda ujemnego, wykres postępu, grupowanie wpłat i wypłat według miesiąca i roku oraz izolację statystyk klienta, przepływ 3000/400/100 PLN, odrzucenie niewystarczających i niepoprawnych kwot, przekroczenie celu i powrót poniżej celu, dostęp drugiego klienta oraz dwie współbieżne wpłaty po 2000 PLN.

Testy integracyjne MVC i migracji obejmują logowanie, dostęp anonimowy i ochronę CSRF, rejestrację oraz walidację jej danych, początkowe saldo skarbonki i brak wpływu jej utworzenia na rachunek, poprawne i odrzucone wpłaty oraz wypłaty, wyświetlanie historii od najnowszej operacji, własność skarbonek, statystyki i wykresy, migrację danych demo oraz współbieżne wpłaty. Wykonano `./mvnw test`; 18 testów integracyjnych zakończyło się powodzeniem, bez błędów i pominięć. Nie zweryfikowano ręcznie zachowania w różnych przeglądarkach, uruchomienia na zewnętrznym systemie bazodanowym ani scenariuszy wdrożenia produkcyjnego; projekt nie jest przeznaczony do produkcji.

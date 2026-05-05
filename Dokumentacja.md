# Dokumentacja Projektu: Serwis Informacyjny Białegostoku

## 1. Opis Projektu
Projekt stanowi implementację rozproszonego systemu informacyjnego dla miasta Białystok w architekturze klient-serwer z użyciem technologii SOAP. System został przygotowany tak, by spełniać wymagania na ocenę 5.0 z przedmiotu RSI (Rozproszone Systemy Informatyczne). 

Serwis udostępnia następujące funkcje:
- Pobieranie eventów dla danego dnia
- Pobieranie eventów dla danego tygodnia
- Pobranie informacji o danym evencie
- Dodawanie eventów
- Modyfikacja informacji o danym evencie
- Odbiór zestawienia eventów w formacie PDF

**Zastosowane Technologie (w tym punkty dodatkowe na 5.0):**
- **Język Serwera**: Java (JAX-WS, Biblioteka PDFBox do PDF)
- **Język Klienta**: Python (Flask, Zeep) - *Dodatkowy punkt: klient i serwer w różnych językach*
- **Szyfrowanie**: Komunikacja oparta jest na SSL/TLS (HTTPS z własnoręcznie wygenerowanym certyfikatem) - *Dodatkowy punkt*
- **Handlers**: Wdrożono `LoggingHandler` w technologii JAX-WS po stronie serwera przechwytujący i logujący do konsoli komunikaty SOAP.
- **MTOM**: Zaimplementowano w operacji przesyłania zestawienia w formacie PDF (załączniki binarne).

## 2. Instrukcja Uruchomienia i Kompilacji

### Krok 1: Serwer Java (Opcja A - Lokalnie przez Maven)
1. Należy wygenerować certyfikat SSL do obsługi HTTPS. W głównym katalogu uruchom skrypt:
   `generate_keystore.bat` (stworzy to plik `keystore.jks`).
2. Upewnij się, że posiadasz zainstalowanego Mavena (narzędzie `mvn`). Skompiluj projekt:
   `mvn clean install`
3. Uruchom serwer (np. z poziomu IDE klasy `com.bialystok.events.Server` lub z użyciem narzędzia Maven):
   `mvn exec:java -Dexec.mainClass="com.bialystok.events.Server"`

### Krok 1: Serwer Java i Klient (Opcja B - Docker Compose - ZALECANE)
Projekt został skonteneryzowany, więc możesz uruchomić wszystkie serwisy (w tym klienta webowego) za pomocą jednej komendy.
1. Upewnij się, że masz zainstalowanego **Dockera** oraz **docker-compose**.
2. W głównym katalogu uruchom skrypt `generate_keystore.bat` (jeśli plik `keystore.jks` jeszcze nie istnieje).
3. Wykonaj komendę w głównym katalogu projektu:
   `docker-compose up -d --build`
4. Po wdrożeniu, serwer SOAP będzie dostępny na porcie `8443`, a klient webowy na porcie `5000`. W razie edycji plików źródłowych, każdorazowo używaj flagi `--build`, aby przebudować kod źródłowy wewnątrz kontenera.

### Krok 2: Klient Python (Aplikacja Webowa)
1. Wejdź do katalogu `client`.
2. Zainstaluj biblioteki:
   `pip install -r requirements.txt`
3. Uruchom serwer developerski Flask:
   `python app.py`
4. Aplikacja dostępna jest pod adresem: `http://127.0.0.1:5000/`

## 3. Prezentacja na 2 komputerach (lub przez sieć VPN np. ZeroTier)
Aby zaprezentować projekt na 2 maszynach lub klientowi zewnętrznemu:
1. Upewnij się, że oba komputery są w tej samej sieci (np. to samo Wi-Fi lub podłączone do tej samej wirtualnej sieci LAN poprzez klienta ZeroTier).
2. Na Komputerze 1 (Serwer), odczytaj jego przypisany adres IP (komenda `ipconfig` w CMD na Windows lub adres przyznany przez interfejs ZeroTier). Powiedzmy, że jest to `192.168.1.50` lub `10.147.x.x`.
3. Komponenty serwerowe (zarówno w Dockerze jak i klasa `Server.java`) zostały skonfigurowane tak, aby domyślnie nasłuchiwać na adresie `0.0.0.0`, dzięki czemu usługa przyjmuje zapytania z każdego interfejsu sieciowego (w tym wirtualnych kart sieciowych VPN).
4. Upewnij się, że firewall w Komputerze 1 przepuszcza ruch przychodzący na porty `8443` oraz `8444`.
5. Na Komputerze 2 (Klient), upewnij się, że klient webowy korzysta z poprawnego zewnętrznego adresu WSDL (możesz go podmienić w `.env` lub wprost w kodzie, jeśli uruchamiasz to lokalnie):
   `EVENTS_WSDL_URL='https://192.168.1.50:8443/ws/events?wsdl'`
6. Uruchom klienta na Komputerze 2 i korzystaj z aplikacji.

## 4. Prezentacja z użyciem Monitora (tcpmonitor / SOAP UI)
Ponieważ nasza usługa używa szyfrowania HTTPS, bezpośrednie użycie `tcpmonitora` jako proxy typu pass-through jest utrudnione. Masz 2 opcje:

### Opcja 1: Tymczasowe wyłączenie HTTPS w klasie Server.java
W klasie `Server.java` opublikuj endpoint bez HTTPS na innym porcie, np. 8080:
`Endpoint.publish("http://localhost:8080/ws/events", new BialystokEventServiceImpl());`
Wtedy w `tcpmonitor`:
- Listen Port: `8081`
- Target Host: `localhost`
- Target Port: `8080`
A klienta nakieruj na adres: `http://localhost:8081/ws/events?wsdl`

### Opcja 2: Użycie SOAP UI z uwierzytelnieniem HTTPS
1. Otwórz **SOAP UI** i stwórz nowy projekt SOAP wpisując adres WSDL: `https://localhost:8443/ws/events?wsdl`.
2. W preferencjach SOAP UI (zakładka SSL Settings) wskaż swój plik `keystore.jks` z hasłem `password`, aby SOAP UI ufał temu certyfikatowi.
3. Wygeneruj żądanie dla danej metody np. `getEventsByDay` i kliknij zielony przycisk "Play". Będziesz mógł zobaczyć surowy Request (z lewej) i Response (z prawej).

## 5. Opis WSDL
Adres WSDL: `https://localhost:8443/ws/events?wsdl` (po uruchomieniu serwera).

Plik WSDL definiuje jeden *PortType* `BialystokEventService` składający się z 6 podstawowych *Operations*:
- `getEventsByDay` (wejście: string date, wyjście: lista Event)
- `getEventsByWeek` (wejście: int week, int year, wyjście: lista Event)
- `getEventDetails` (wejście: int id, wyjście: obiekt Event)
- `addEvent` (wejście: string name, string type, string date, int week, int month, int year, string description, wyjście: boolean)
- `updateEvent` (wejście: int id, string name, string type, string date, int week, int month, int year, string description, wyjście: boolean)
- `getEventSummaryPdf` (wyjście: base64Binary z ustawionym *MTOM / xmime:expectedContentTypes="application/pdf"*)

Schemat definiuje strukturę obiektu XML `Event` zawierającego m.in pola `id`, `name`, `type`, `date` i `description`.

## 6. Przykładowe komunikaty SOAP

### Przykładowy Request SOAP (Pobieranie szczegółów eventu - ID: 1)
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://events.bialystok.com/">
   <soapenv:Header/>
   <soapenv:Body>
      <ser:getEventDetails>
         <id>1</id>
      </ser:getEventDetails>
   </soapenv:Body>
</soapenv:Envelope>
```

### Przykładowy Response SOAP (Szczegóły eventu)
```xml
<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
   <S:Body>
      <ns2:getEventDetailsResponse xmlns:ns2="http://events.bialystok.com/">
         <return>
            <id>1</id>
            <name>Koncert Muzyki Dawnej</name>
            <type>Kultura</type>
            <date>2024-05-10</date>
            <week>19</week>
            <month>5</month>
            <year>2024</year>
            <description>Koncert w Pałacu Branickich</description>
         </return>
      </ns2:getEventDetailsResponse>
   </S:Body>
</S:Envelope>
```

## 7. Instrukcja dla potencjalnego zewnętrznego klienta
Jeżeli chcesz konsumować nasze usługi API w swojej aplikacji:
1. Pobierz plik WSDL, aby wygenerować klasy klienckie w swoim języku programowania (np. wsimport dla Javy, wsdl2py dla Pythona, WCF dla C#). Adres udostępniania usługi otrzymasz od dostawcy.
2. Z uwagi na korzystanie z formatu HTTPS, musisz albo zaakceptować certyfikat serwera (`keystore.jks`) na swojej maszynie/w magazynie zaufanych certyfikatów Twojej platformy (TrustStore), albo wyłączyć walidację certyfikatów w środowisku deweloperskim.
3. Nasz serwis w pełni wspiera specyfikację MTOM. W wypadku wywołania operacji `getEventSummaryPdf`, upewnij się, że Twój framework (klient SOAP) ma aktywowane przetwarzanie MTOM, aby poprawnie odkodować załącznik binarny do pliku `.pdf`.
4. Typy dat wprowadzaj jako zwykły `String` w formacie konwencjonalnym (np. "YYYY-MM-DD" dla kompatybilności), ponieważ nie wymuszamy w tym projekcie schematu daty `xs:date`.

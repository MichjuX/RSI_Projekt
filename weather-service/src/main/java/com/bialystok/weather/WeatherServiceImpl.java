package com.bialystok.weather;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.jws.WebService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

@WebService(endpointInterface = "com.bialystok.weather.WeatherService")
public class WeatherServiceImpl implements WeatherService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Override
    public String getWeatherForDate(String date) {
        try {
            boolean isPast = LocalDate.parse(date).isBefore(LocalDate.now());
            String base = isPast ? "https://archive-api.open-meteo.com/v1/archive" : "https://api.open-meteo.com/v1/forecast";
            String url = base + "?latitude=53.13&longitude=23.16&daily=temperature_2m_max,weathercode&timezone=Europe/Warsaw&start_date=" + date + "&end_date=" + date;

            HttpResponse<String> response = HTTP_CLIENT.send(
                HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
            );

            JsonObject daily = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonObject("daily");
            int temp = daily.getAsJsonArray("temperature_2m_max").get(0).getAsInt();
            int code = daily.getAsJsonArray("weathercode").get(0).getAsInt();

            return describeWeather(code) + ", " + temp + "C";

        } catch (Exception e) {
            return "Brak danych pogodowych dla daty: " + date;
        }
    }

    private String describeWeather(int code) {
        if (code == 0) return "Bezchmurnie";
        if (code <= 3) return "Częściowe zachmurzenie";
        if (code <= 48) return "Mgła";
        if (code <= 67) return "Deszczowo";
        if (code <= 77) return "Śnieżnie";
        if (code <= 82) return "Przelotne opady";
        return "Burza";
    }
}

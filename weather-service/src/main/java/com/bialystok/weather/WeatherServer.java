package com.bialystok.weather;

import javax.xml.ws.Endpoint;

public class WeatherServer {

    public static void main(String[] args) {
        Endpoint.publish("http://localhost:8444/ws/weather", new WeatherServiceImpl());
    }
}

package com.bialystok.weather;

import javax.xml.ws.Endpoint;

public class WeatherServer {

    public static void main(String[] args) {
        Endpoint.publish("http://0.0.0.0:8444/ws/weather", new WeatherServiceImpl());
        System.out.println("Weather Service is running!");
        System.out.println("WSDL available at: http://localhost:8444/ws/weather?wsdl");
    }
}

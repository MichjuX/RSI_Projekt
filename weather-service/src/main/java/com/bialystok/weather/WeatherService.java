package com.bialystok.weather;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface WeatherService {

    @WebMethod
    String getWeatherForDate(@WebParam(name = "date") String date);
}

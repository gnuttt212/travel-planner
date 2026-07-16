package com.travelplanner.itinerary.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Service tich hop OpenWeatherMap API (free tier, khong can the).
 *
 * Goi API lay du bao thoi tiet 5 ngay cho toa do cua diem den chinh.
 * Phan tich ket qua de xac dinh co mua hay khong, tu do dieu chinh
 * prompt cho Gemini AI (goi y hoat dong trong nha thay vi ngoai troi).
 */
@Service
@Slf4j
public class WeatherService {

    @Value("${openweathermap.api.key}")
    private String apiKey;

    @Value("${openweathermap.api.url}")
    private String apiUrl;

    /**
     * Lay du bao thoi tiet va tra ve chuoi mo ta ngan gon.
     * Neu co mua => tra ve "Rain expected", nguoc lai => "Clear/Sunny"
     */
    @SuppressWarnings("unchecked")
    public String getWeatherForecast(double lat, double lon) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = String.format("%s?lat=%s&lon=%s&appid=%s&units=metric&cnt=8",
                    apiUrl, lat, lon, apiKey);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("list")) {
                List<Map<String, Object>> forecasts = (List<Map<String, Object>>) response.get("list");

                for (Map<String, Object> forecast : forecasts) {
                    List<Map<String, Object>> weatherList = (List<Map<String, Object>>) forecast.get("weather");
                    if (weatherList != null) {
                        for (Map<String, Object> weather : weatherList) {
                            String main = (String) weather.get("main");
                            if ("Rain".equalsIgnoreCase(main) || "Drizzle".equalsIgnoreCase(main)
                                    || "Thunderstorm".equalsIgnoreCase(main)) {
                                Map<String, Object> mainData = (Map<String, Object>) forecast.get("main");
                                double temp = ((Number) mainData.get("temp")).doubleValue();
                                String desc = (String) weather.get("description");
                                return String.format("Rain expected (%.1f°C, %s). Indoor activities recommended.", temp, desc);
                            }
                        }
                    }
                }

                // No rain found in the forecast window
                Map<String, Object> first = forecasts.get(0);
                Map<String, Object> mainData = (Map<String, Object>) first.get("main");
                double temp = ((Number) mainData.get("temp")).doubleValue();
                return String.format("Clear weather expected (%.1f°C). Outdoor activities are great!", temp);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch weather forecast: {}", e.getMessage());
        }
        return "Weather data unavailable.";
    }

    /**
     * Kiem tra xem du bao co mua hay khong.
     */
    public boolean isRainExpected(double lat, double lon) {
        String forecast = getWeatherForecast(lat, lon);
        return forecast.toLowerCase().contains("rain");
    }
}

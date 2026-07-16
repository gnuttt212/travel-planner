package com.travelplanner.itinerary.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Service tich hop Nominatim (OpenStreetMap Geocoding).
 * Su dung de tim kiem toa do tu ten dia diem.
 */
@Service
@Slf4j
public class NominatimService {

    private final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    @SuppressWarnings("unchecked")
    public double[] geocode(String query) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            // Yeu cau cua Nominatim: nen set User-Agent de khong bi block
            // Trong demo dung RestTemplate default nhung thuc te can Headers
            String url = String.format("%s?q=%s&format=json&limit=1", NOMINATIM_URL, query);
            
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);
            
            if (response != null && !response.isEmpty()) {
                Map<String, Object> firstResult = response.get(0);
                double lat = Double.parseDouble((String) firstResult.get("lat"));
                double lon = Double.parseDouble((String) firstResult.get("lon"));
                return new double[]{lat, lon};
            }
        } catch (Exception e) {
            log.error("Nominatim geocoding failed for query: {}", query, e);
        }
        return null;
    }
}

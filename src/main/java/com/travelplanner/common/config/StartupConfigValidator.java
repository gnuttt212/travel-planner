package com.travelplanner.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class StartupConfigValidator {

    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        List<String> missing = new ArrayList<>();

        require("jwt.secret", missing);
        require("spring.datasource.url", missing);
        require("spring.datasource.username", missing);
        require("spring.datasource.password", missing);

        validateOrsApiKey();
        validateOpenWeatherMapApiKey();
        validateGeminiApiKey();

        // Phase 3: uncomment when integrated
        // require("gemini.api.key", missing);

        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing required production configuration: " + String.join(", ", missing));
        }
    }

    private void require(String property, List<String> missing) {
        String value = environment.getProperty(property);
        if (value == null || value.isBlank()) {
            missing.add(property);
        }
    }

    private void validateOrsApiKey() {
        String orsKey = environment.getProperty("ors.api.key");
        if (orsKey == null || orsKey.trim().isEmpty() || orsKey.startsWith("${")) {
            System.err.println("WARNING: ORS_API_KEY is not configured. System will fallback to Haversine distance calculation.");
        }
    }

    private void validateOpenWeatherMapApiKey() {
        String owmKey = environment.getProperty("openweathermap.api.key");
        if (owmKey == null || owmKey.trim().isEmpty() || owmKey.startsWith("${")) {
            System.err.println("WARNING: OPENWEATHERMAP_API_KEY is not configured. System will assume no rain (isRaining = false).");
        }
    }

    private void validateGeminiApiKey() {
        String geminiKey = environment.getProperty("gemini.api.key");
        if (geminiKey == null || geminiKey.trim().isEmpty() || geminiKey.startsWith("${")) {
            System.err.println("WARNING: GEMINI_API_KEY is not configured. AI narratives will fall back to empty strings.");
        }
    }
}

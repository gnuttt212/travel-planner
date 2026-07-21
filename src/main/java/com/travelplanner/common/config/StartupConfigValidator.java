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
        require("gemini.api.key", missing);
        require("openweathermap.api.key", missing);
        require("ors.api.key", missing);

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
}

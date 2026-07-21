package com.travelplanner.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StartupConfigValidatorTest {

    @Test
    void shouldFailFastWhenRequiredPropertiesAreMissingInProd() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        environment.setProperty("jwt.secret", "");
        environment.setProperty("spring.datasource.url", "");
        environment.setProperty("spring.datasource.username", "");
        environment.setProperty("spring.datasource.password", "");
        environment.setProperty("gemini.api.key", "");
        environment.setProperty("openweathermap.api.key", "");
        environment.setProperty("ors.api.key", "");

        StartupConfigValidator validator = new StartupConfigValidator(environment);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::run);
        assertTrue(exception.getMessage().contains("jwt.secret"));
    }
}

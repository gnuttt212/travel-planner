package com.travelplanner.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class StartupConfigValidatorTest {

    @Test
    void shouldFailFastWhenRequiredPropertiesAreMissingInProd() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        environment.setProperty("jwt.secret", "");
        environment.setProperty("spring.datasource.url", "");
        environment.setProperty("spring.datasource.username", "");
        environment.setProperty("spring.datasource.password", "");

        StartupConfigValidator validator = new StartupConfigValidator(environment);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::run);
        assertTrue(exception.getMessage().contains("jwt.secret"));
    }

    @Test
    void shouldPassWhenAllRequiredPropertiesAreProvided() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        environment.setProperty("jwt.secret", "a-strong-secret");
        environment.setProperty("spring.datasource.url", "jdbc:postgresql://db:5432/tp");
        environment.setProperty("spring.datasource.username", "user");
        environment.setProperty("spring.datasource.password", "pass");

        StartupConfigValidator validator = new StartupConfigValidator(environment);

        assertDoesNotThrow(validator::run);
    }
}

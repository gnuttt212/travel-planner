package com.travelplanner;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigurationPropertiesTest {

    @Test
    void applicationConfigurationShouldUseEnvironmentBindingsForSecrets() throws IOException {
        String applicationConfig = Files.readString(Path.of("src/main/resources/application.yml"));

        assertThat(applicationConfig)
                .contains("password: ${DB_PASSWORD:postgres}")
                .contains("secret: ${JWT_SECRET:change-me-in-production}")
                .contains("key: ${GEMINI_API_KEY:}")
                .contains("key: ${ORS_API_KEY:}")
                .contains("key: ${OPENWEATHERMAP_API_KEY:}")
                .doesNotContain("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
    }

    @Test
    void devProfileShouldUseEnvironmentBindingsForLocalDatabaseCredentials() throws IOException {
        String devConfig = Files.readString(Path.of("src/main/resources/application-dev.yml"));

        assertThat(devConfig)
                .contains("username: ${DB_USERNAME:postgres}")
                .contains("password: ${DB_PASSWORD:postgres}");
    }
}

package com.travelplanner;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigurationPropertiesTest {

    @Test
    void applicationConfigurationShouldRequireJwtSecretFromEnvironment() throws IOException {
        String applicationConfig = Files.readString(Path.of("src/main/resources/application.yml"));

        assertThat(applicationConfig)
                .contains("password: ${DB_PASSWORD:postgres}")
                .contains("secret: ${JWT_SECRET}")
                .doesNotContain("change-me-in-production")
                .doesNotContain("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
    }

    @Test
    void devProfileShouldProvideJwtSecretFallback() throws IOException {
        String devConfig = Files.readString(Path.of("src/main/resources/application-dev.yml"));

        assertThat(devConfig)
                .contains("secret: ${JWT_SECRET:dev-only-secret-do-not-use-in-production}")
                .contains("username: ${DB_USERNAME:postgres}")
                .contains("password: ${DB_PASSWORD:postgres}");
    }
}

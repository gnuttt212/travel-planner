package com.travelplanner.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI travelPlannerOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Travel Planner API")
                .description("API cho ung dung len ke hoach du lich")
                .version("v0.1.0"));
    }
}

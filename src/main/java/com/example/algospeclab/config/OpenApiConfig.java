package com.example.algospeclab.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI algoSpecLabOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("algo-spec-lab API")
                .description("アルゴリズム Spec 検証用の API")
                .version("0.0.1-SNAPSHOT"));
    }
}

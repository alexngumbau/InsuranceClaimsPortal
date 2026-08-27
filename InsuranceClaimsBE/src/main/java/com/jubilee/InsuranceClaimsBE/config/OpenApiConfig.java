package com.jubilee.InsuranceClaimsBE.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI insuranceClaimsOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Insurance Claims API")
                .version("v1")
                .description("REST API for creating, reviewing, and processing insurance claims.")
                .contact(new Contact().name("Insurance Claims Portal")));
    }
}
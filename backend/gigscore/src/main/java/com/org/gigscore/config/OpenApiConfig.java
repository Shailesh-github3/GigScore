package com.org.gigscore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_TOKEN = "Bearer Token";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GigScore API")
                        .description("""
                                REST API for gig workers and freelancers to track earnings,
                                jobs, ratings, and compute a unified performance score.
                                Authenticate via JWT Bearer token obtained from the login endpoint.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("GigScore")
                                .url("https://github.com/your-org/gigscore")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_TOKEN))
                .components(new Components()
                        .addSecuritySchemes(BEARER_TOKEN,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste the JWT token from /api/users/login")));
    }
}

package com.gnemirko.bank_rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankApi() {
        // SecurityScheme: Bearer JWT
        var bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new OpenAPI()
                .info(new Info()
                        .title("Bank REST API")
                        .description("""
                                API для управления картами и пользователями.
                                Роли: ADMIN/USER. Маскирование номеров карт, переводы, фильтрация и пагинация.
                                """)
                        .version("v1.0.0")
                        .contact(new Contact().name("Your Name").email("you@example.com"))
                        .license(new License().name("Apache-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local"),
                        new Server().url("https://api.example.com").description("Prod")
                ))
                .schemaRequirement("BearerAuth", bearer)         // объявляем схему
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth")); // применяем по-умолчанию
    }
}
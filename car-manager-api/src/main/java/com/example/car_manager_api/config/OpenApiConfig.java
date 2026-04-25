package com.example.car_manager_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .info(apiInfo())
                .components(securityComponents());
    }

    private Info apiInfo(){
        return new Info()
                .title("Car Manager API")
                .description("API do projeto de gerenciamento de veículos")
                .version("1.0")
                .contact(contact());
    }

    private Contact contact(){
        return new Contact()
                .name("Henrique Greve")
                .url("http://github.com/henriquegreve")
                .email("henriquegreve@live.com");
    }

    private Components securityComponents(){
        return new Components().addSecuritySchemes("bearer-jwt",
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Obtenha o token em POST /auth/login"));
    }

}

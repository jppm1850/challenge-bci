package com.bci.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI customOpenAPI(
            @Value("${springdoc.version:1.0.0}") String appVersion,
            @Value("${server.port:8080}") String serverPort) {

        return new OpenAPI()
                .info(new Info()
                        .title("BCI Challenge API")
                        .version(appVersion)
                        .description("""
                                API RESTful para gestión de usuarios con autenticación JWT.
                                
                                Características principales:
                                - Registro de usuarios con validación avanzada
                                - Autenticación mediante tokens JWT
                                - Validación de email único
                                - Gestión de números telefónicos
                                - Arquitectura reactiva con Spring WebFlux
                                """)
                        .contact(new Contact()
                                .name("Junior Pedro Pecho Mendoza")
                                .email("jppm1850@gmail.com")
                                .url("https://www.jppm1850.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de desarrollo local"),
                        new Server()
                                .url("https://api.bci.com")
                                .description("Servidor de producción")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        Autenticación mediante token JWT.
                                        
                                        Formato: Bearer {token}
                                        Ejemplo: Bearer eyJhbGciOiJIUzI1NiJ9...
                                        """)))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"));
    }
}
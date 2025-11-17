package com.kaddy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI medicoOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setEmail("support@medico.com");
        contact.setName("Medico Support Team");
        contact.setUrl("https://www.medico.com");

        License license = new License()
            .name("Proprietary License")
            .url("https://www.medico.com/license");

        Info info = new Info()
            .title("Medico Hospital and Pharmacy Management System API")
            .version("1.0.0")
            .contact(contact)
            .description("Comprehensive REST API for managing hospital operations and pharmacy services")
            .termsOfService("https://www.medico.com/terms")
            .license(license);

        return new OpenAPI()
            .info(info)
            .servers(List.of(devServer));
    }
}

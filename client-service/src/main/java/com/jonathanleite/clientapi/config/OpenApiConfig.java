package com.jonathanleite.clientapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Client API")
                        .description("""
                                Microsserviço responsável pelo cadastro, consulta, atualização e inativação lógica de clientes.
                                Use o header X-User-Id nas rotas privadas para identificar o usuário da operação.
                                """)
                        .version("v1")
                        .contact(new Contact()
                                .name("Jonathan Leite"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Ambiente local")
                ))
                .tags(List.of(
                        new Tag()
                                .name("Clientes")
                                .description("Operações de cadastro, consulta, atualização e inativação lógica de clientes")
                ));
    }
}

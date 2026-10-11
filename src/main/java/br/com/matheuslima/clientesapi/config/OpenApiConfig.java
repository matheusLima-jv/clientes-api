package br.com.matheuslima.clientesapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clientesOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Clientes API")
                .version("v1")
                .description("API REST para cadastro, consulta e remoção de clientes. "
                        + "Erros são retornados no formato Problem Details (RFC 9457), "
                        + "com Content-Type application/problem+json."));
    }

}

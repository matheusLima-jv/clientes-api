package br.com.matheuslima.clientesapi.config;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Configuration
public class OpenApiConfig {

    static {
        SpringDocUtils.getConfig().replaceWithClass(ProblemDetail.class, ProblemDetailSchema.class);
    }

    @Bean
    public OpenAPI clientesOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Clientes API")
                        .version("v1")
                        .description("API REST para cadastro, consulta e remoção de clientes. "
                                + "Erros são retornados no formato Problem Details (RFC 9457), "
                                + "com Content-Type application/problem+json."))
                .servers(List.of(new Server().url("/").description("Servidor atual")));
    }

    @Schema(name = "ProblemDetail", description = "Erro no formato Problem Details (RFC 9457)")
    record ProblemDetailSchema(
            @Schema(description = "URI que identifica o tipo do erro", example = "about:blank",
                    requiredMode = REQUIRED)
            URI type,

            @Schema(description = "Resumo do erro", example = "Cliente não encontrado",
                    requiredMode = REQUIRED)
            String title,

            @Schema(description = "Código de status HTTP", example = "404",
                    requiredMode = REQUIRED)
            int status,

            @Schema(description = "Explicação do erro", example = "Cliente com id 99 não encontrado",
                    requiredMode = REQUIRED)
            String detail,

            @Schema(description = "Caminho da requisição que gerou o erro", example = "/api/v1/clientes/99",
                    requiredMode = REQUIRED)
            URI instance,

            @Schema(description = "Mensagens de erro por campo, presente apenas em erros de validação")
            Map<String, List<String>> erros
    ) {
    }
}

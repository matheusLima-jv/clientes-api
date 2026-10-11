package br.com.matheuslima.clientesapi.controller;

import br.com.matheuslima.clientesapi.dto.ClienteRequest;
import br.com.matheuslima.clientesapi.dto.ClienteResponse;
import br.com.matheuslima.clientesapi.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

@Tag(name = "Clientes", description = "Cadastro, consulta e remoção de clientes")
public interface ClienteApi {

    String PROBLEM_JSON = "application/problem+json";

    String EXEMPLO_400_DADOS = """
            {
              "type": "about:blank",
              "title": "Dados inválidos",
              "status": 400,
              "detail": "Um ou mais campos são inválidos",
              "instance": "/api/v1/clientes",
              "erros": {
                "nome": "o tamanho deve ser entre 3 e 100",
                "email": "deve ser um endereço de e-mail bem formado",
                "cpf": "deve conter exatamente 11 dígitos numéricos"
              }
            }
            """;

    String EXEMPLO_400_ID = """
            {
              "type": "about:blank",
              "title": "Bad Request",
              "status": 400,
              "detail": "Failed to convert 'id' with value: 'abc'",
              "instance": "/api/v1/clientes/abc"
            }
            """;

    String EXEMPLO_404 = """
            {
              "type": "about:blank",
              "title": "Cliente não encontrado",
              "status": 404,
              "detail": "Cliente com id 99 não encontrado",
              "instance": "/api/v1/clientes/99"
            }
            """;

    String EXEMPLO_409 = """
            {
              "type": "about:blank",
              "title": "CPF duplicado",
              "status": 409,
              "detail": "CPF já cadastrado",
              "instance": "/api/v1/clientes"
            }
            """;

    @Operation(summary = "Cadastra um cliente",
            description = "Cria um cliente e retorna seus dados com o cabeçalho Location apontando para o recurso criado. "
                    + "O id e a data de cadastro são gerados pelo servidor.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente criado",
                    headers = @Header(name = "Location", description = "URI do cliente criado",
                            schema = @Schema(type = "string", format = "uri"))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou corpo malformado",
                    content = @Content(mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = EXEMPLO_400_DADOS))),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado",
                    content = @Content(mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = EXEMPLO_409)))
    })
    ResponseEntity<ClienteResponse> criar(ClienteRequest request);

    @Operation(summary = "Busca um cliente por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "400", description = "Id em formato inválido",
                    content = @Content(mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = EXEMPLO_400_ID))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = EXEMPLO_404)))
    })
    ClienteResponse buscarPorId(@Parameter(description = "Id do cliente", example = "1") Long id);

    @Operation(summary = "Lista clientes de forma paginada",
            description = "Retorna os clientes ordenados por id.")
    @Parameters({
            @Parameter(name = "pagina", in = ParameterIn.QUERY, description = "Número da página, começando em 0",
                    schema = @Schema(type = "integer", minimum = "0", defaultValue = "0")),
            @Parameter(name = "tamanho", in = ParameterIn.QUERY, description = "Quantidade de clientes por página",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "50", defaultValue = "10"))
    })
    @ApiResponse(responseCode = "200", description = "Página de clientes")
    PaginaResponse<ClienteResponse> listar(@Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "Remove um cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente removido"),
            @ApiResponse(responseCode = "400", description = "Id em formato inválido",
                    content = @Content(mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = EXEMPLO_400_ID))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = EXEMPLO_404)))
    })
    void remover(@Parameter(description = "Id do cliente", example = "1") Long id);
}

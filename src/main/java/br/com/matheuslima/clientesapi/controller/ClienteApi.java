package br.com.matheuslima.clientesapi.controller;

import br.com.matheuslima.clientesapi.dto.ClienteRequest;
import br.com.matheuslima.clientesapi.dto.ClienteResponse;
import br.com.matheuslima.clientesapi.dto.PaginaResponse;
import br.com.matheuslima.clientesapi.dto.PaginacaoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
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
                "cpf": [
                  "deve conter exatamente 11 dígitos numéricos",
                  "número do registro de contribuinte individual brasileiro (CPF) inválido"
                ],
                "email": ["deve ser um endereço de e-mail bem formado"],
                "nome": ["tamanho deve ser entre 3 e 100"]
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

    String EXEMPLO_400_PAGINACAO = """
            {
              "type": "about:blank",
              "title": "Dados inválidos",
              "status": 400,
              "detail": "Um ou mais campos são inválidos",
              "instance": "/api/v1/clientes",
              "erros": {
                "tamanho": ["deve ser menor que ou igual à 50"]
              }
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de clientes"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos",
                    content = @Content(mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = EXEMPLO_400_PAGINACAO)))
    })
    PaginaResponse<ClienteResponse> listar(@ParameterObject PaginacaoRequest paginacao);

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

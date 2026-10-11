package br.com.matheuslima.clientesapi.controller;

import br.com.matheuslima.clientesapi.dto.ClienteRequest;
import br.com.matheuslima.clientesapi.dto.ClienteResponse;
import br.com.matheuslima.clientesapi.dto.PaginaResponse;
import br.com.matheuslima.clientesapi.exception.ClienteNaoEncontradoException;
import br.com.matheuslima.clientesapi.exception.CpfDuplicadoException;
import br.com.matheuslima.clientesapi.service.ClienteService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    private static final ClienteResponse MARIA = new ClienteResponse(
            1L, "Maria Silva", "maria@email.com", "52998224725", Instant.parse("2026-10-10T12:00:00Z"));

    private static final PaginaResponse<ClienteResponse> PAGINA_VAZIA = new PaginaResponse<>(List.of(), 0, 10, 0, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    @Test
    @DisplayName("POST deve criar cliente e retornar 201 com Location")
    void deveCriarCliente() throws Exception {
        when(service.criar(any(ClienteRequest.class))).thenReturn(MARIA);

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Maria Silva", "email": "maria@email.com", "cpf": "52998224725"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/clientes/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }

    @Test
    @DisplayName("GET por id deve retornar 200 com o cliente")
    void deveBuscarPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(MARIA);

        mockMvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andExpect(jsonPath("$.dataCadastro").value("2026-10-10T12:00:00Z"));
    }

    @Test
    @DisplayName("GET deve aceitar os parâmetros pagina e tamanho")
    void deveListarPaginado() throws Exception {
        when(service.listar(any(Pageable.class))).thenReturn(new PaginaResponse<>(List.of(MARIA), 2, 5, 11, 3));

        mockMvc.perform(get("/api/v1/clientes").param("pagina", "2").param("tamanho", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].id").value(1))
                .andExpect(jsonPath("$.pagina").value(2));

        verify(service).listar(argThat((Pageable p) -> p.getPageNumber() == 2 && p.getPageSize() == 5));
    }

    @Test
    @DisplayName("GET sem parâmetros deve usar página 0 e tamanho 10")
    void deveUsarPaginacaoPadrao() throws Exception {
        when(service.listar(any(Pageable.class))).thenReturn(PAGINA_VAZIA);

        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk());

        verify(service).listar(argThat((Pageable p) -> p.getPageNumber() == 0 && p.getPageSize() == 10));
    }

    @Test
    @DisplayName("GET deve ordenar por id e ignorar ordenação informada pelo cliente")
    void deveIgnorarOrdenacaoDoCliente() throws Exception {
        when(service.listar(any(Pageable.class))).thenReturn(PAGINA_VAZIA);

        mockMvc.perform(get("/api/v1/clientes").param("sort", "inexistente"))
                .andExpect(status().isOk());

        verify(service).listar(argThat((Pageable p) -> p.getSort().equals(Sort.by("id"))));
    }

    @ParameterizedTest(name = "pagina={0}, tamanho={1}")
    @CsvSource({
            "-1, 10, pagina",
            "abc, 10, pagina",
            "2147483647, 10, pagina",
            "0, 0, tamanho",
            "0, 51, tamanho"
    })
    @DisplayName("GET com paginação fora dos limites deve retornar 400 com erro no parâmetro")
    void deveRejeitarPaginacaoInvalida(String pagina, String tamanho, String parametro) throws Exception {
        mockMvc.perform(get("/api/v1/clientes").param("pagina", pagina).param("tamanho", tamanho))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.erros." + parametro).exists());

        verify(service, never()).listar(any());
    }

    @Test
    @DisplayName("DELETE deve retornar 204")
    void deveRemoverCliente() throws Exception {
        mockMvc.perform(delete("/api/v1/clientes/1"))
                .andExpect(status().isNoContent());

        verify(service).remover(1L);
    }

    @Test
    @DisplayName("POST com dados inválidos deve retornar 400 com erros por campo")
    void deveRejeitarDadosInvalidos() throws Exception {
        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"nome": "Ma", "email": "invalido", "cpf": "123"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.erros.nome").exists())
                .andExpect(jsonPath("$.erros.email").exists())
                .andExpect(jsonPath("$.erros.cpf").exists());

        verify(service, never()).criar(any());
    }

    @Test
    @DisplayName("POST deve retornar todas as mensagens de erro de um mesmo campo")
    void deveRetornarTodasAsMensagensDoCampo() throws Exception {
        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"nome": "Maria Silva", "email": "maria@email.com", "cpf": "123"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.cpf", hasSize(2)))
                .andExpect(jsonPath("$.erros.cpf", hasItem("deve conter exatamente 11 dígitos numéricos")));
    }

    @Test
    @DisplayName("POST deve remover espaços extras do nome antes de validar")
    void deveNormalizarNome() throws Exception {
        when(service.criar(any(ClienteRequest.class))).thenReturn(MARIA);

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"nome": "  Maria   Silva  ", "email": "maria@email.com", "cpf": "52998224725"}
                            """))
                .andExpect(status().isCreated());

        verify(service).criar(argThat(request -> request.nome().equals("Maria Silva")));
    }

    @Test
    @DisplayName("POST com nome de menos de 3 caracteres desconsiderando espaços deve retornar 400")
    void deveRejeitarNomeCurtoComEspacos() throws Exception {
        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"nome": "  a ", "email": "maria@email.com", "cpf": "52998224725"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists());

        verify(service, never()).criar(any());
    }

    @Test
    @DisplayName("POST com e-mail sem domínio completo deve retornar 400")
    void deveRejeitarEmailSemDominioCompleto() throws Exception {
        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"nome": "Maria Silva", "email": "maria@localhost", "cpf": "52998224725"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.email").exists());

        verify(service, never()).criar(any());
    }

    @Test
    @DisplayName("GET de cliente inexistente deve retornar 404 em ProblemDetail")
    void deveRetornar404() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new ClienteNaoEncontradoException(99L));

        mockMvc.perform(get("/api/v1/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Cliente não encontrado"))
                .andExpect(jsonPath("$.detail").value("Cliente com id 99 não encontrado"));
    }

    @Test
    @DisplayName("erro inesperado deve retornar 500 em ProblemDetail sem expor detalhes internos")
    void deveRetornar500SemExporDetalhes() throws Exception {
        when(service.buscarPorId(1L)).thenThrow(new IllegalStateException("conexão com o banco recusada"));

        mockMvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Erro interno"))
                .andExpect(jsonPath("$.detail").value("Ocorreu um erro inesperado. Tente novamente mais tarde."));
    }

    @Test
    @DisplayName("POST com CPF duplicado deve retornar 409")
    void deveRetornar409() throws Exception {
        when(service.criar(any(ClienteRequest.class))).thenThrow(new CpfDuplicadoException());

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"nome": "Maria Silva", "email": "maria@email.com", "cpf": "52998224725"}
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("CPF já cadastrado"));
    }

    @Test
    @DisplayName("POST deve retornar 409 quando o banco rejeitar CPF duplicado")
    void deveRetornar409QuandoBancoRejeitarCpf() throws Exception {
        when(service.criar(any(ClienteRequest.class)))
                .thenThrow(violacaoDeIntegridade("PUBLIC.UK_CLIENTES_CPF_INDEX_6"));

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"nome": "Maria Silva", "email": "maria@email.com", "cpf": "52998224725"}
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("CPF já cadastrado"));
    }

    @Test
    @DisplayName("POST não deve tratar outra violação de integridade como CPF duplicado")
    void naoDeveTratarOutraViolacaoComoCpfDuplicado() throws Exception {
        when(service.criar(any(ClienteRequest.class)))
                .thenThrow(violacaoDeIntegridade("PUBLIC.OUTRA_CONSTRAINT"));

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"nome": "Maria Silva", "email": "maria@email.com", "cpf": "52998224725"}
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito de dados"))
                .andExpect(jsonPath("$.detail").value(not("CPF já cadastrado")));
    }

    @Test
    @DisplayName("POST com e-mail acima de 255 caracteres deve retornar 400")
    void deveRejeitarEmailLongo() throws Exception {
        var email = "a".repeat(64) + "@" + ("b".repeat(60) + ".").repeat(4) + "com";

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"nome": "Maria Silva", "email": "%s", "cpf": "52998224725"}
                            """.formatted(email)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.email").exists());

        verify(service, never()).criar(any());
    }

    private static DataIntegrityViolationException violacaoDeIntegridade(String constraint) {
        return new DataIntegrityViolationException("violação de integridade",
                new ConstraintViolationException("violação de integridade", new SQLException(), constraint));
    }

}

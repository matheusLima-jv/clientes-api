package br.com.matheuslima.clientesapi.controller;

import br.com.matheuslima.clientesapi.dto.ClienteRequest;
import br.com.matheuslima.clientesapi.dto.ClienteResponse;
import br.com.matheuslima.clientesapi.dto.PaginaResponse;
import br.com.matheuslima.clientesapi.exception.ClienteNaoEncontradoException;
import br.com.matheuslima.clientesapi.exception.CpfDuplicadoException;
import br.com.matheuslima.clientesapi.service.ClienteService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
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
            1L, "Maria Silva", "maria@email.com", "52998224725", LocalDateTime.of(2026, 10, 10, 9, 0));

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
                .andExpect(jsonPath("$.email").value("maria@email.com"));
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
                .thenThrow(new DataIntegrityViolationException("unique constraint"));

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"nome": "Maria Silva", "email": "maria@email.com", "cpf": "52998224725"}
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("CPF já cadastrado"));
    }

}

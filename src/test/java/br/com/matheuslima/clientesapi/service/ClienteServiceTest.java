package br.com.matheuslima.clientesapi.service;

import br.com.matheuslima.clientesapi.domain.Cliente;
import br.com.matheuslima.clientesapi.dto.ClienteRequest;
import br.com.matheuslima.clientesapi.exception.ClienteNaoEncontradoException;
import br.com.matheuslima.clientesapi.exception.CpfDuplicadoException;
import br.com.matheuslima.clientesapi.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    private static final Instant AGORA = Instant.parse("2026-10-10T12:00:00Z");
    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final String CPF = "52998224725";

    @Mock
    private ClienteRepository repository;

    private ClienteService service;

    @BeforeEach
    void setUp() {
        service = new ClienteService(repository, Clock.fixed(AGORA, SAO_PAULO));
    }

    @Test
    @DisplayName("deve criar cliente com data de cadastro gerada pelo servidor")
    void deveCriarCliente() {
        var request = new ClienteRequest("Maria Silva", "maria@email.com", CPF);
        when(repository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            ReflectionTestUtils.setField(cliente, "id", 1L);
            return cliente;
        });

        var response = service.criar(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Maria Silva");
        assertThat(response.cpf()).isEqualTo(CPF);
        assertThat(response.dataCadastro()).isEqualTo(LocalDateTime.of(2026, 10, 10, 9, 0));
    }

    @Test
    @DisplayName("deve lançar exceção quando CPF já estiver cadastrado")
    void deveRejeitarCpfDuplicado() {
        var request = new ClienteRequest("Maria Silva", "maria@email.com", CPF);
        when(repository.existsByCpf(CPF)).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(CpfDuplicadoException.class)
                .hasMessage("CPF já cadastrado");
        verify(repository, never()).save(any());
    }

    private Cliente clienteSalvo(Long id) {
        var cliente = new Cliente("Maria Silva", "maria@email.com", CPF, LocalDateTime.of(2026, 10, 10, 9, 0));
        ReflectionTestUtils.setField(cliente, "id", id);
        return cliente;
    }

    @Test
    @DisplayName("deve buscar cliente existente por id")
    void deveBuscarPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(clienteSalvo(1L)));

        var response = service.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("maria@email.com");
    }

    @Test
    @DisplayName("deve lançar exceção ao buscar cliente inexistente")
    void deveFalharAoBuscarInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(ClienteNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deve remover cliente existente")
    void deveRemoverCliente() {
        when(repository.existsById(1L)).thenReturn(true);

        service.remover(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("deve lançar exceção ao remover cliente inexistente")
    void deveFalharAoRemoverInexistente() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.remover(99L))
                .isInstanceOf(ClienteNaoEncontradoException.class);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deve listar clientes paginados")
    void deveListarPaginado() {
        var pageable = PageRequest.of(0, 10);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(clienteSalvo(1L)), pageable, 1));

        var pagina = service.listar(pageable);

        assertThat(pagina.conteudo()).hasSize(1);
        assertThat(pagina.pagina()).isZero();
        assertThat(pagina.tamanho()).isEqualTo(10);
        assertThat(pagina.totalElementos()).isEqualTo(1);
        assertThat(pagina.totalPaginas()).isEqualTo(1);
    }

}

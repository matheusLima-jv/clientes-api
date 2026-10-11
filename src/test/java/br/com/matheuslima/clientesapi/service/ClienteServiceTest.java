package br.com.matheuslima.clientesapi.service;

import br.com.matheuslima.clientesapi.domain.Cliente;
import br.com.matheuslima.clientesapi.dto.ClienteRequest;
import br.com.matheuslima.clientesapi.exception.CpfDuplicadoException;
import br.com.matheuslima.clientesapi.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
}

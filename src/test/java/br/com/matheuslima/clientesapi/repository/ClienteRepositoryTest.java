package br.com.matheuslima.clientesapi.repository;

import br.com.matheuslima.clientesapi.domain.Cliente;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DataJpaTest
public class ClienteRepositoryTest {

    private static final String CPF = "52998224725";

    @Autowired
    private ClienteRepository repository;

    @Test
    @DisplayName("deve informar se já existe cliente com o CPF")
    void deveVerificarExistenciaPorCpf() {
        repository.saveAndFlush(novoCliente("maria@email.com"));

        assertThat(repository.existsByCpf(CPF)).isTrue();
        assertThat(repository.existsByCpf("11144477735")).isFalse();
    }

    @Test
    @DisplayName("banco deve rejeitar CPF duplicado mesmo sem passar pelo service")
    void deveRejeitarCpfDuplicadoNoBanco() {
        repository.saveAndFlush(novoCliente("maria@email.com"));

        assertThatThrownBy(() -> repository.saveAndFlush(novoCliente("outra@email.com")))
                .isInstanceOf(DataIntegrityViolationException.class)
                .cause()
                .isInstanceOfSatisfying(ConstraintViolationException.class,
                        ex -> assertThat(ex.getConstraintName()).containsIgnoringCase("uk_clientes_cpf"));
    }

    private Cliente novoCliente(String email) {
        return new Cliente("Maria Silva", email, CPF, LocalDateTime.of(2026, 10, 10, 9, 0));
    }

}

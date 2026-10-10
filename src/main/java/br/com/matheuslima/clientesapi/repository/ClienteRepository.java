package br.com.matheuslima.clientesapi.repository;

import br.com.matheuslima.clientesapi.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByCpf(String cpf);

}

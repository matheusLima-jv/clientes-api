package br.com.matheuslima.clientesapi.repository;

import br.com.matheuslima.clientesapi.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByCpf(String cpf);

    @Modifying
    @Query("delete from Cliente c where c.id = :id")
    int removerPorId(Long id);

}

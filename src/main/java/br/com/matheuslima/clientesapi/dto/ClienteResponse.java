package br.com.matheuslima.clientesapi.dto;

import br.com.matheuslima.clientesapi.domain.Cliente;

import java.time.LocalDateTime;

public record ClienteResponse(Long id, String nome, String email, String cpf, LocalDateTime dataCadastro) {

    public static ClienteResponse from(Cliente cliente) {

        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getCpf(),
                cliente.getDataCadastro());
    }
}

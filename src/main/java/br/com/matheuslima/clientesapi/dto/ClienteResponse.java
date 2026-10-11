package br.com.matheuslima.clientesapi.dto;

import br.com.matheuslima.clientesapi.domain.Cliente;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Dados de um cliente cadastrado")
public record ClienteResponse(
        @Schema(description = "Identificador gerado pelo servidor", example = "1")
        Long id,

        @Schema(description = "Nome completo", example = "Maria Silva")
        String nome,

        @Schema(description = "E-mail de contato", example = "maria@email.com")
        String email,

        @Schema(description = "CPF com 11 dígitos, sem pontuação", example = "52998224725")
        String cpf,

        @Schema(description = "Data e hora do cadastro em UTC, gerada pelo servidor", example = "2026-10-10T12:00:00Z")
        Instant dataCadastro
) {

    public static ClienteResponse from(Cliente cliente) {

        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getCpf(),
                cliente.getDataCadastro());
    }
}

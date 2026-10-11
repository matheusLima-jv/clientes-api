package br.com.matheuslima.clientesapi.dto;

import br.com.matheuslima.clientesapi.domain.Cliente;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Dados de um cliente cadastrado")
public record ClienteResponse(
        @Schema(description = "Identificador gerado pelo servidor", example = "1", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "Nome completo", example = "Maria Silva", requiredMode = REQUIRED)
        String nome,

        @Schema(description = "E-mail de contato", example = "maria@email.com", requiredMode = REQUIRED)
        String email,

        @Schema(description = "CPF com 11 dígitos, sem pontuação", example = "52998224725", requiredMode = REQUIRED)
        String cpf,

        @Schema(description = "Data e hora do cadastro em UTC, gerada pelo servidor", example = "2026-10-10T12:00:00Z",
                requiredMode = REQUIRED)
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

package br.com.matheuslima.clientesapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

@Schema(description = "Dados para cadastro de um cliente")
public record ClienteRequest(
        @Schema(description = "Nome completo", example = "Maria Silva")
        @NotBlank @Size(min = 3, max = 100)
        String nome,

        @Schema(description = "E-mail de contato", example = "maria@email.com")
        @NotBlank @Email
        String email,

        @Schema(description = "CPF válido com 11 dígitos, sem pontuação", example = "52998224725")
        @NotBlank
        @Pattern(regexp = "\\d{11}", message = "deve conter exatamente 11 dígitos numéricos")
        @CPF
        String cpf
) {
}

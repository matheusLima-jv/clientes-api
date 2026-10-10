package br.com.matheuslima.clientesapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record ClienteRequest(
        @NotBlank @Size(min = 3, max = 100)
        String nome,

        @NotBlank @Email
        String email,

        @NotBlank
        @Pattern(regexp = "\\d{11}", message = "deve conter exatamente 11 dígitos numéricos")
        @CPF
        String cpf
) {
}

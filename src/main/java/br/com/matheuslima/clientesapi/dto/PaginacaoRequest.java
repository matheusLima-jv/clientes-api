package br.com.matheuslima.clientesapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Objects;

public record PaginacaoRequest(
        @Schema(description = "Número da página, começando em 0", defaultValue = "0")
        @Min(0) @Max(PAGINA_MAXIMA)
        Integer pagina,

        @Schema(description = "Quantidade de clientes por página", defaultValue = "10")
        @Min(1) @Max(TAMANHO_MAXIMO)
        Integer tamanho
) {

    public static final int TAMANHO_MAXIMO = 50;
    public static final int PAGINA_MAXIMA = Integer.MAX_VALUE / TAMANHO_MAXIMO;

    public PaginacaoRequest {
        pagina = Objects.requireNonNullElse(pagina, 0);
        tamanho = Objects.requireNonNullElse(tamanho, 10);
    }

    public Pageable toPageable() {
        return PageRequest.of(pagina, tamanho, Sort.by("id"));
    }
}

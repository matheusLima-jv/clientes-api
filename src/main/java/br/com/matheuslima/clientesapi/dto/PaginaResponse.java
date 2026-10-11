package br.com.matheuslima.clientesapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Página de resultados")
public record PaginaResponse<T>(
        @Schema(description = "Itens da página atual", requiredMode = REQUIRED)
        List<T> conteudo,

        @Schema(description = "Número da página atual, começando em 0", example = "0", requiredMode = REQUIRED)
        int pagina,

        @Schema(description = "Quantidade máxima de itens por página", example = "10", requiredMode = REQUIRED)
        int tamanho,

        @Schema(description = "Total de itens em todas as páginas", example = "1", requiredMode = REQUIRED)
        long totalElementos,

        @Schema(description = "Total de páginas", example = "1", requiredMode = REQUIRED)
        int totalPaginas
) {

    public static <T> PaginaResponse<T> from(Page<T> page) {
        return new PaginaResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

}

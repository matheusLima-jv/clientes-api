package br.com.matheuslima.clientesapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClienteIntegracaoTest {

    private static final String MARIA = """
            {"nome": "Maria Silva", "email": "maria@email.com", "cpf": "52998224725"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("deve cadastrar, consultar, listar e remover um cliente")
    void deveExecutarFluxoCompleto() throws Exception {
        var location = mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MARIA))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        var cliente = URI.create(location);

        mockMvc.perform(get(cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.cpf").value("52998224725"))
                .andExpect(jsonPath("$.dataCadastro").exists());

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MARIA))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        mockMvc.perform(get("/api/v1/clientes").param("pagina", "0").param("tamanho", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].nome").value("Maria Silva"));

        mockMvc.perform(delete(cliente))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(cliente))
                .andExpect(status().isNotFound());
    }

}

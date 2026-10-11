package br.com.matheuslima.clientesapi.controller;

import br.com.matheuslima.clientesapi.dto.ClienteRequest;
import br.com.matheuslima.clientesapi.dto.ClienteResponse;
import br.com.matheuslima.clientesapi.dto.PaginaResponse;
import br.com.matheuslima.clientesapi.dto.PaginacaoRequest;
import br.com.matheuslima.clientesapi.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController implements ClienteApi {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @Override
    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        var criado = service.criar(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @Override
    @GetMapping("/{id}")
    public ClienteResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @Override
    @GetMapping
    public PaginaResponse<ClienteResponse> listar(@Valid PaginacaoRequest paginacao) {
        return service.listar(paginacao.toPageable());
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}

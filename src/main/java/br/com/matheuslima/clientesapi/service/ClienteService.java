package br.com.matheuslima.clientesapi.service;

import br.com.matheuslima.clientesapi.domain.Cliente;
import br.com.matheuslima.clientesapi.dto.ClienteRequest;
import br.com.matheuslima.clientesapi.dto.ClienteResponse;
import br.com.matheuslima.clientesapi.dto.PaginaResponse;
import br.com.matheuslima.clientesapi.exception.ClienteNaoEncontradoException;
import br.com.matheuslima.clientesapi.exception.CpfDuplicadoException;
import br.com.matheuslima.clientesapi.repository.ClienteRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final Clock clock;

    public ClienteService(ClienteRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        if (repository.existsByCpf(request.cpf())) {
            throw new CpfDuplicadoException();
        }
        var cliente = new Cliente(request.nome(), request.email(), request.cpf(), LocalDateTime.now(clock));
        return ClienteResponse.from(repository.save(cliente));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(ClienteResponse::from)
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new ClienteNaoEncontradoException(id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ClienteResponse> listar(Pageable pageable) {
        return PaginaResponse.from(repository.findAll(pageable).map(ClienteResponse::from));
    }
}

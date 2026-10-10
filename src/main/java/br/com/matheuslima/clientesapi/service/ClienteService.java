package br.com.matheuslima.clientesapi.service;

import br.com.matheuslima.clientesapi.dto.ClienteRequest;
import br.com.matheuslima.clientesapi.dto.ClienteResponse;
import br.com.matheuslima.clientesapi.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final Clock clock;

    public ClienteService(ClienteRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public ClienteResponse criar(ClienteRequest request) {
        throw new UnsupportedOperationException("não implementado");
    }
}

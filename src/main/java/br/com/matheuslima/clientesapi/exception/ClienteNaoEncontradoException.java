package br.com.matheuslima.clientesapi.exception;

public class ClienteNaoEncontradoException extends RuntimeException {

    public ClienteNaoEncontradoException(Long id) {
        super("Cliente com id " + id + " não encontrado");
    }
}

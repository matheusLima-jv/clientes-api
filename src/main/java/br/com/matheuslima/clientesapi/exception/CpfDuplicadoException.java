package br.com.matheuslima.clientesapi.exception;

public class CpfDuplicadoException extends RuntimeException {

    public CpfDuplicadoException() {
        super("CPF já cadastrado");
    }
}


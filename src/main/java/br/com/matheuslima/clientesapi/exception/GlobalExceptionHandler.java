package br.com.matheuslima.clientesapi.exception;

import br.com.matheuslima.clientesapi.domain.Cliente;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ProblemDetail handleClienteNaoEncontrado(ClienteNaoEncontradoException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Cliente não encontrado");
        return problem;
    }

    @ExceptionHandler(CpfDuplicadoException.class)
    public ProblemDetail handleCpfDuplicado(CpfDuplicadoException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("CPF duplicado");
        return problem;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> erros = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        erro -> erro.isBindingFailure()
                                ? "valor em formato inválido"
                                : Objects.requireNonNullElse(erro.getDefaultMessage(), "valor inválido"),
                        (primeiro, segundo) -> primeiro));

        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Um ou mais campos são inválidos");
        problem.setTitle("Dados inválidos");
        problem.setProperty("erros", erros);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    // segurança contra race condition duas requisições simultâneas podem passar
    // pelo existsByCpf ao mesmo tempo; a constraint unique do banco barra a segunda
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleViolacaoDeIntegridade(DataIntegrityViolationException ex) {
        if (violouUnicidadeDoCpf(ex)) {
            return handleCpfDuplicado(new CpfDuplicadoException());
        }
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "A operação viola uma restrição de integridade dos dados");
        problem.setTitle("Conflito de dados");
        return problem;
    }

    private boolean violouUnicidadeDoCpf(DataIntegrityViolationException ex) {
        return ex.getCause() instanceof ConstraintViolationException violacao
                && violacao.getConstraintName() != null
                && violacao.getConstraintName().toLowerCase().contains(Cliente.UK_CPF);
    }

}

package br.com.matheuslima.clientesapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "clientes", uniqueConstraints = @UniqueConstraint(name = Cliente.UK_CPF, columnNames = "cpf"))
public class Cliente {

    public static final String UK_CPF = "uk_clientes_cpf";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false)
    private String email;

    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private Instant dataCadastro;

    protected Cliente() {
    }

    public Cliente(String nome, String email, String cpf, Instant dataCadastro) {
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.dataCadastro = dataCadastro;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    public Instant getDataCadastro() {
        return dataCadastro;
    }
}


# Clientes API

API REST para cadastro, consulta, listagem paginada e remoção de clientes, construída com Java 17, Spring Boot 3 e Maven.

O projeto segue uma arquitetura em camadas (Controller → Service → Repository), valida as entradas com Bean Validation, padroniza todas as respostas de erro no formato Problem Details (RFC 9457) e garante a unicidade do CPF tanto na regra de negócio quanto no banco de dados.

## Sumário

- [Tecnologias](#tecnologias)
- [Como executar](#como-executar)
- [Endpoints](#endpoints)
- [Regras de validação](#regras-de-validação)
- [Paginação](#paginação)
- [Tratamento de erros](#tratamento-de-erros)
- [Arquitetura](#arquitetura)
- [Decisões técnicas](#decisões-técnicas)
- [Testes](#testes)
- [Atendimento aos requisitos](#atendimento-aos-requisitos)
- [Próximos passos](#próximos-passos)

## Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Linguagem |
| Spring Boot | 3.5.16 | Web, Validation e Data JPA |
| H2 Database | gerenciada pelo Spring Boot | Banco de dados em memória |
| SpringDoc OpenAPI | 2.9.1 | Documentação OpenAPI e Swagger UI |
| JUnit 5, Mockito, AssertJ e MockMvc | gerenciadas pelo Spring Boot | Testes |
| Maven (via Maven Wrapper) | 3.10.0 | Build, sem precisar do Maven instalado |

## Como executar

### Pré-requisitos

- JDK 17 ou superior

O Maven não precisa estar instalado: o projeto inclui o Maven Wrapper (`mvnw`).

### Executando a aplicação

```bash
git clone https://github.com/matheusLima-jv/clientes-api.git
cd clientes-api

# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

Também é possível gerar o JAR e executá-lo diretamente:

```bash
./mvnw clean package
java -jar target/clientes-api-0.0.1-SNAPSHOT.jar
```

A aplicação sobe na porta `8080`. Para usar outra porta, adicione `--server.port=8081` ao comando `java -jar`.

### Endereços úteis

| Recurso | URL |
|---|---|
| API | http://localhost:8080/api/v1/clientes |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Especificação OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Console do H2 | http://localhost:8080/h2-console |

Para acessar o console do H2, use a JDBC URL `jdbc:h2:mem:clientesdb`, usuário `sa` e senha em branco.

> O banco é em memória: os dados existem apenas enquanto a aplicação estiver rodando.

## Endpoints

| Método | Rota | Descrição | Sucesso | Erros possíveis |
|---|---|---|---|---|
| `POST` | `/api/v1/clientes` | Cadastra um cliente | `201 Created` + cabeçalho `Location` | `400`, `409` |
| `GET` | `/api/v1/clientes/{id}` | Busca um cliente por id | `200 OK` | `400`, `404` |
| `GET` | `/api/v1/clientes?pagina=0&tamanho=10` | Lista clientes de forma paginada | `200 OK` | `400` |
| `DELETE` | `/api/v1/clientes/{id}` | Remove um cliente | `204 No Content` | `400`, `404` |

Todos os endpoints podem retornar `500` em caso de erro inesperado.

### Cadastrar cliente

```bash
curl -i -X POST http://localhost:8080/api/v1/clientes \
  -H "Content-Type: application/json" \
  -d '{"nome": "Maria Silva", "email": "maria@email.com", "cpf": "52998224725"}'
```

```http
HTTP/1.1 201
Location: http://localhost:8080/api/v1/clientes/1
Content-Type: application/json
```

```json
{
  "id": 1,
  "nome": "Maria Silva",
  "email": "maria@email.com",
  "cpf": "52998224725",
  "dataCadastro": "2026-10-11T21:28:50Z"
}
```

O `id` e a `dataCadastro` são gerados pelo servidor. Se forem enviados no corpo da requisição, são ignorados.

### Buscar cliente por id

```bash
curl http://localhost:8080/api/v1/clientes/1
```

```json
{
  "id": 1,
  "nome": "Maria Silva",
  "email": "maria@email.com",
  "cpf": "52998224725",
  "dataCadastro": "2026-10-11T21:28:50Z"
}
```

### Listar clientes

```bash
curl "http://localhost:8080/api/v1/clientes?pagina=0&tamanho=10"
```

```json
{
  "conteudo": [
    {
      "id": 1,
      "nome": "Maria Silva",
      "email": "maria@email.com",
      "cpf": "52998224725",
      "dataCadastro": "2026-10-11T21:28:50Z"
    }
  ],
  "pagina": 0,
  "tamanho": 10,
  "totalElementos": 1,
  "totalPaginas": 1
}
```

### Remover cliente

```bash
curl -i -X DELETE http://localhost:8080/api/v1/clientes/1
```

```http
HTTP/1.1 204
```

## Regras de validação

| Campo | Regras |
|---|---|
| `nome` | Obrigatório, entre 3 e 100 caracteres. Antes da validação, espaços no início e no fim são removidos, espaços repetidos entre as palavras (inclusive espaços não separáveis) são reduzidos a um e caracteres invisíveis são descartados. |
| `email` | Obrigatório, formato de e-mail válido com domínio completo (ex.: `nome@dominio.com`), no máximo 255 caracteres. |
| `cpf` | Obrigatório, exatamente 11 dígitos numéricos (sem pontuação), com dígitos verificadores válidos. Não pode haver dois clientes com o mesmo CPF. |
| `dataCadastro` | Gerada pelo servidor no momento do cadastro, em UTC (ISO-8601). |

Exemplo: `"  Maria   Silva  "` é armazenado como `"Maria Silva"`, e `"  a "` é rejeitado por ter apenas 1 caractere útil. Um nome formado apenas por caracteres invisíveis também é rejeitado.

## Paginação

A listagem recebe dois parâmetros opcionais de query string:

| Parâmetro | Padrão | Mínimo | Máximo |
|---|---|---|---|
| `pagina` | `0` | `0` | `42949672` |
| `tamanho` | `10` | `1` | `50` |

- A primeira página é a `0`.
- O resultado é sempre ordenado por `id`. Parâmetros de ordenação enviados pelo cliente são ignorados, o que evita consultas por campos inexistentes ou não previstos.
- Valores fora dos limites ou em formato inválido (ex.: `pagina=abc`) retornam `400` indicando o parâmetro com problema, em vez de serem corrigidos silenciosamente.
- O limite de `pagina` existe porque o deslocamento da consulta (`pagina × tamanho`) precisa caber em um `int`. Com `tamanho` máximo de 50, o maior valor seguro é `Integer.MAX_VALUE / 50`.
- Uma página além do total retorna `conteudo` vazio, mantendo os totais corretos.

## Tratamento de erros

Todos os erros seguem o padrão Problem Details ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457)), com `Content-Type: application/problem+json`. O tratamento é centralizado em um `@RestControllerAdvice` (`GlobalExceptionHandler`). Títulos e mensagens são sempre em português, inclusive nos erros tratados pelo próprio Spring, como JSON malformado ou método não suportado.

| Status | Quando ocorre |
|---|---|
| `400 Bad Request` | Campos inválidos, JSON malformado, id em formato inválido ou parâmetros de paginação fora dos limites |
| `404 Not Found` | Cliente inexistente ou rota inexistente |
| `405 Method Not Allowed` | Método HTTP não suportado na rota |
| `406 Not Acceptable` | Cabeçalho `Accept` pede um formato de resposta diferente de JSON |
| `409 Conflict` | CPF já cadastrado |
| `415 Unsupported Media Type` | Corpo enviado sem `Content-Type: application/json` |
| `500 Internal Server Error` | Erro inesperado. A resposta traz uma mensagem genérica e os detalhes ficam apenas no log da aplicação |

### Erro de validação

Os erros de validação trazem a propriedade `erros`, com a lista de mensagens de cada campo. Campos e mensagens são ordenados alfabeticamente, então a mesma entrada sempre gera a mesma resposta.

```json
{
  "type": "about:blank",
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Um ou mais campos são inválidos",
  "instance": "/api/v1/clientes",
  "erros": {
    "cpf": [
      "deve conter exatamente 11 dígitos numéricos",
      "número do registro de contribuinte individual brasileiro (CPF) inválido"
    ],
    "email": ["deve ser um endereço de e-mail bem formado"],
    "nome": ["tamanho deve ser entre 3 e 100"]
  }
}
```

### Cliente não encontrado

```json
{
  "type": "about:blank",
  "title": "Cliente não encontrado",
  "status": 404,
  "detail": "Cliente com id 99 não encontrado",
  "instance": "/api/v1/clientes/99"
}
```

### CPF duplicado

```json
{
  "type": "about:blank",
  "title": "CPF duplicado",
  "status": 409,
  "detail": "CPF já cadastrado",
  "instance": "/api/v1/clientes"
}
```

## Arquitetura

```
src/main/java/br/com/matheuslima/clientesapi
├── config/
│   ├── ClockConfig.java              # Clock usado para gerar a data de cadastro
│   └── OpenApiConfig.java            # Informações gerais e schema de erro da documentação OpenAPI
├── controller/
│   ├── ClienteApi.java               # Contrato da API com as anotações do OpenAPI
│   └── ClienteController.java        # Endpoints REST
├── domain/
│   └── Cliente.java                  # Entidade JPA
├── dto/
│   ├── ClienteRequest.java           # Entrada do cadastro (record + Bean Validation)
│   ├── ClienteResponse.java          # Saída com os dados do cliente
│   ├── PaginacaoRequest.java         # Parâmetros de paginação validados
│   └── PaginaResponse.java           # Envelope da resposta paginada
├── exception/
│   ├── ClienteNaoEncontradoException.java
│   ├── CpfDuplicadoException.java
│   └── GlobalExceptionHandler.java   # Tradução de exceções para Problem Details
├── repository/
│   └── ClienteRepository.java        # Spring Data JPA
└── service/
    └── ClienteService.java           # Regras de negócio e transações
```

O fluxo de uma requisição segue `Controller → Service → Repository`, com todas as dependências injetadas por construtor. A entidade `Cliente` não sai da camada de serviço: o controller recebe e devolve apenas DTOs.

## Decisões técnicas

**Unicidade do CPF em duas camadas.** O service verifica com `existsByCpf` antes de salvar, o que cobre o caso comum com uma resposta clara. Isso sozinho não basta: duas requisições simultâneas podem passar pela verificação ao mesmo tempo. Por isso o banco também tem uma constraint única com nome (`uk_clientes_cpf`). Quando ela é violada, o `GlobalExceptionHandler` identifica a constraint pelo nome e devolve o mesmo `409`. Outras violações de integridade recebem uma resposta genérica de conflito e nunca são confundidas com CPF duplicado.

**Remoção atômica.** O `DELETE` executa um único comando `delete ... where id = :id` e usa o número de linhas afetadas para decidir entre `204` e `404`. Verificar a existência e remover em etapas separadas abriria espaço para que requisições simultâneas de remoção do mesmo cliente terminassem em erro. Com um único comando, uma delas recebe `204` e as demais `404`, e o banco é consultado uma vez só.

**Records como DTOs.** As entradas e saídas são records imutáveis. O construtor compacto do `ClienteRequest` normaliza o nome e o e-mail antes da validação, e o `PaginacaoRequest` concentra os valores padrão e os limites da paginação.

**Envelope de paginação próprio.** A listagem devolve um `PaginaResponse` em vez de serializar o `Page` do Spring Data diretamente. Assim o contrato da API fica estável, com nomes de campos em português, e não depende da estrutura interna do framework.

**Datas em UTC.** A `dataCadastro` é um `Instant`, armazenado e devolvido em UTC e truncado em segundos. O valor não depende do fuso horário do servidor, e a resposta do cadastro é idêntica à de uma consulta posterior. O `Clock` é injetado no service, o que torna os testes determinísticos.

**Documentação separada do controller.** As anotações do OpenAPI ficam na interface `ClienteApi`, implementada pelo `ClienteController`. O controller mantém apenas o código dos endpoints, e a documentação inclui exemplos reais de cada resposta de erro. O schema de erro documentado reflete exatamente a resposta da API, incluindo a propriedade `erros`, e os campos sempre presentes nas respostas são marcados como obrigatórios, o que permite gerar clientes tipados a partir do spec.

**Erros inesperados não expõem detalhes.** Exceções não previstas retornam `500` com uma mensagem genérica. A exceção completa é registrada no log, sem vazar informações internas para quem consome a API.

**Mensagens sempre em português.** O idioma da aplicação é fixo em `pt_BR` (`spring.web.locale-resolver: fixed`). Sem isso, as mensagens de validação seguiriam o cabeçalho `Accept-Language` ou o idioma da máquina, e a mesma requisição poderia gerar respostas que misturam português e inglês. Os erros tratados pelo próprio Spring (JSON malformado, rota inexistente, método ou Content-Type não suportado) são traduzidos no `messages.properties`, pelos códigos de mensagem que o Spring consulta ao montar o Problem Details.

**Outros cuidados.** `spring.jpa.open-in-view` está desativado para que nenhuma consulta ao banco aconteça fora da camada de serviço, e as operações de leitura usam `@Transactional(readOnly = true)`.

## Testes

```bash
# Linux / macOS
./mvnw test

# Windows
mvnw.cmd test
```

São 44 testes no total, organizados em quatro níveis:

| Classe | Tipo | O que cobre | Testes |
|---|---|---|---|
| `ClienteServiceTest` | Unitário (JUnit 5 + Mockito) | Regras de negócio: cadastro, CPF duplicado, busca, remoção e listagem, incluindo os cenários de exceção | 7 |
| `ClienteControllerTest` | Web (`@WebMvcTest` + MockMvc) | Status HTTP, cabeçalho `Location`, validações, paginação, normalização de entrada, idioma das mensagens e formato das respostas de erro, inclusive dos erros tratados pelo Spring | 33 |
| `ClienteRepositoryTest` | Persistência (`@DataJpaTest`) | Consulta por CPF, constraint de unicidade aplicada pelo banco e remoção atômica por id | 3 |
| `ClienteIntegracaoTest` | Integração (`@SpringBootTest`) | Fluxo completo com a aplicação real: cadastro, consulta pelo `Location`, CPF duplicado, listagem, remoção e consulta após a remoção | 1 |

O `ClienteControllerTest` inclui testes parametrizados para paginação inválida (5 cenários), nomes formados por caracteres invisíveis (2) e erros tratados pelo Spring (5). Como o idioma da aplicação é fixo, os testes produzem o mesmo resultado em qualquer máquina, independentemente do idioma configurado no sistema.

## Atendimento aos requisitos

| Requisito | Onde está implementado |
|---|---|
| Endpoints `POST`, `GET` por id, `GET` paginado e `DELETE` | `ClienteController` |
| `201 Created` com `Location`, `404` e `204` | `ClienteController` e `GlobalExceptionHandler` |
| Cliente com id, nome, email, cpf e dataCadastro | `Cliente`, `ClienteRequest` e `ClienteResponse` |
| Records para DTOs e Bean Validation | Pacote `dto` |
| Camadas Controller → Service → Repository com injeção por construtor | Pacotes `controller`, `service` e `repository` |
| Tratamento global de erros com `@RestControllerAdvice` e ProblemDetail | `GlobalExceptionHandler` |
| CPF duplicado retorna `409 Conflict` | `ClienteService`, constraint `uk_clientes_cpf` e `GlobalExceptionHandler` |
| Persistência com Spring Data JPA e H2 | `ClienteRepository` e `application.yaml` |
| Testes unitários do Service com cenários de exceção | `ClienteServiceTest` |
| Teste do Controller com `@WebMvcTest` e MockMvc | `ClienteControllerTest` |
| Documentação com SpringDoc OpenAPI e Swagger UI | `ClienteApi`, `OpenApiConfig` e anotações `@Schema` nos DTOs |

## Próximos passos

Melhorias que fariam sentido em uma evolução do projeto:

- Atualização de clientes (`PUT`/`PATCH`) e busca por CPF ou nome
- Banco PostgreSQL com migrações versionadas (Flyway) e ambiente local via Docker Compose
- Testes de integração com Testcontainers, usando o mesmo banco de produção
- Perfis separados de desenvolvimento e produção, com o console do H2 habilitado apenas em desenvolvimento
- Mascaramento do CPF nas respostas e nos logs, em linha com a LGPD
- Autenticação e autorização (Spring Security com JWT)
- Monitoramento com Spring Boot Actuator

## Autor

Matheus Lima ([GitHub](https://github.com/matheusLima-jv))

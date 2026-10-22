# Microservice Email

Microsserviço de **envio de e-mails** em **Java 21 e Spring Boot**. Ele consome mensagens do **RabbitMQ**, envia o e-mail de boas-vindas pelo SMTP do Gmail e registra o resultado (`SENT` ou `ERROR`) no seu próprio banco **PostgreSQL**. As mensagens vêm do [Microservice_User](https://github.com/GustavoSoaresbcge/Microservice_User).

O foco aqui é a **resiliência**: se o envio falhar, a mensagem não se perde. Ela é tentada de novo e, se continuar falhando, vai para uma **Dead Letter Queue (DLQ)**, de onde pode ser reprocessada.

> 🐳 **Quer ver o sistema inteiro funcionando?** Use o repositório [Microservice_Build_Environment](https://github.com/GustavoSoaresbcge/Microservice_Build_Environment): com um único comando ele sobe os dois serviços, o PostgreSQL e o RabbitMQ.

## Como funciona

```mermaid
flowchart LR
    U[Microservice_User] -->|publica| Q[(Fila default.email)]
    Q --> E[Microservice_Email :8082]
    E --- P[(PostgreSQL ms-email)]
    E -->|envia| S[SMTP Gmail]
    E -->|falha: nova tentativa| Q
    E -.->|tentativas esgotadas| D[(DLQ email.dlq)]
    D -.->|POST /admin/dlq/reprocess| Q
```

1. O consumidor lê a mensagem da fila `default.email`.
2. O serviço tenta enviar o e-mail e grava o resultado no banco (`SENT` ou `ERROR`).
3. Se o envio falhar, a exceção é **relançada de propósito** para que o retry do Spring AMQP entre em ação.
4. Se falhar, o envio é repetido **3 vezes** (4 tentativas no total), com espera crescente de **2 s, 4 s e 8 s** entre elas. Esgotadas as tentativas, a mensagem vai para a fila `email.dlq` por meio da Dead Letter Exchange `email.dlx`.

## Endpoint

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/admin/dlq/reprocess` | Move de volta para a fila principal as mensagens que estão na DLQ |

```bash
curl -X POST http://localhost:8082/admin/dlq/reprocess
```

> ⚠️ Esse endpoint ainda **não tem autenticação**. Está nos próximos passos.

## Tecnologias

Java 21 · Spring Boot · Spring Data JPA · Spring AMQP · RabbitMQ · PostgreSQL · JavaMail (Gmail SMTP) · Docker · JUnit 5 · Mockito · Maven

## Rodando só este serviço (pela IDE ou pelo Maven)

Para rodar o sistema completo, o caminho mais simples é o [Microservice_Build_Environment](https://github.com/GustavoSoaresbcge/Microservice_Build_Environment). Para rodar só este serviço, você precisa de:

- Java 21 e Maven
- Um PostgreSQL com o banco `ms-email` criado (`CREATE DATABASE "ms-email";`), em `localhost:5432` com usuário `postgres`
- Um RabbitMQ acessível (local ou na nuvem, como o CloudAMQP)
- Uma conta Gmail com **verificação em duas etapas** e uma **senha de app** ([como criar](https://support.google.com/accounts/answer/185833))

As informações sensíveis vêm de **variáveis de ambiente**, e não do código:

| Variável | O que é |
|---|---|
| `DB_PASSWORD_EMAIL` | Senha do PostgreSQL |
| `MAIL_USERNAME` | Conta Gmail que envia os e-mails |
| `MAIL_PASSWORD` | Senha de app do Gmail (não é a senha normal da conta) |

```bash
# Linux / macOS
export DB_PASSWORD_EMAIL=minha_senha
export MAIL_USERNAME=seu_email@gmail.com
export MAIL_PASSWORD=sua_senha_de_app
mvn spring-boot:run
```

```powershell
# Windows (PowerShell)
$env:DB_PASSWORD_EMAIL="minha_senha"
$env:RABBITMQ_ADDRESSES_EMAIL="amqp://guest:guest@localhost:5672"
$env:MAIL_USERNAME="seu_email@gmail.com"
$env:MAIL_PASSWORD="sua_senha_de_app"
mvn spring-boot:run
```

Na IDE, as mesmas variáveis entram em *Run Configurations → Environment*.

## Ver o retry e a DLQ funcionando

1. Suba o serviço com uma `MAIL_PASSWORD` **inválida**.
2. Cadastre um usuário pelo [Microservice_User](https://github.com/GustavoSoaresbcge/Microservice_User).
3. O envio vai falhar: o console mostra as novas tentativas com espera crescente e, ao esgotá-las, a mensagem aparece na fila `email.dlq` (painel do RabbitMQ, aba **Queues**).
4. Corrija a senha, reinicie o serviço e chame `POST /admin/dlq/reprocess`. O e-mail é enviado e a DLQ esvazia.

## Testes

```bash
mvn test
```

Os testes unitários usam **JUnit 5 e Mockito** e cobrem o `EmailService` nos dois caminhos: envio com sucesso (`SENT`) e falha (`ERROR`, com a exceção relançada).

## Decisões de projeto e limitações

- **Falha relançada de propósito:** o serviço registra o erro e relança a exceção, para que o retry e a DLQ do Spring AMQP funcionem.
- **Banco próprio:** este serviço é o único dono dos registros de e-mail.
- **Sem idempotência (ainda):** se a mesma mensagem for entregue duas vezes (por exemplo, quando uma conexão cai antes do *ack*), o e-mail é enviado duas vezes.

## Próximos passos

- [ ] Idempotência no consumidor (evitar e-mail duplicado)
- [ ] Autenticação (Spring Security + JWT) no endpoint de reprocessamento
- [ ] Testes de integração com Testcontainers
- [ ] Pipeline de CI com GitHub Actions

## Autor

**Gustavo Soares** · [LinkedIn](https://www.linkedin.com/in/devgustavosoares) · [gustavosoaresbcge@gmail.com](mailto:gustavosoaresbcge@gmail.com)
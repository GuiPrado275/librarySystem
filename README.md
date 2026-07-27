# 📚 Library System

API REST para gerenciamento de uma biblioteca, desenvolvida com **Java + Spring Boot**. O sistema controla o acervo de livros, cadastro de usuários, empréstimos, devoluções, multas por atraso e fila de reservas, com autenticação e autorização via **JWT** e perfis de acesso (Usuário, Bibliotecário e Administrador).

## 🚀 Tecnologias

- **Java 21**
- **Spring Boot 4.1.0**
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Validation
- **JWT** (io.jsonwebtoken / jjwt 0.11.5) — autenticação stateless
- **MySQL** (produção) / **H2** (runtime, testes)
- **Lombok** — redução de boilerplate nas entidades
- **Apache Commons Lang3**
- **Maven** — gerenciador de dependências e build

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas, no padrão comum a aplicações Spring Boot:

```
controllers/   → Endpoints REST (camada de entrada da API)
services/      → Regras de negócio e orquestração
repositories/  → Acesso a dados (Spring Data JPA)
models/        → Entidades JPA (Book, User, Loan, Reservation)
models/dto/    → DTOs de entrada/saída (create, update, relatórios)
models/enums/  → Enums de domínio (ProfileEnum)
security/      → Filtros JWT, UserDetails e configuração de autenticação
configs/       → Configurações de segurança e CORS
exceptions/    → Tratamento global de exceções (RestControllerAdvice)
```

## ⚙️ Funcionalidades

### 👤 Usuários
- Cadastro de novos usuários (`ROLE_USER` por padrão)
- Consulta de usuário por ID (o próprio usuário ou um ADMIN)
- Consulta do usuário autenticado (`/user/me`)
- Atualização de senha
- Remoção de usuário (bloqueada se houver empréstimos em aberto)
- Senhas armazenadas com hash **BCrypt**

### 📖 Livros
- Cadastro, atualização e remoção (somente `LIBRARIAN`/`ADMIN`)
- Remoção lógica (*soft delete*) — preserva o histórico de empréstimos já realizados
- Busca por título ou autor (case-insensitive)
- Listagem de livros ativos
- Controle automático de exemplares disponíveis (`availableCopies`) a cada empréstimo/devolução
- Bloqueio de redução de exemplares abaixo da quantidade já emprestada
- Relatório dos livros mais emprestados (`/book/most-borrowed`)

### 🔄 Empréstimos
- Criação de empréstimo com validação de regras de negócio:
  - Livro precisa estar ativo e ter exemplares disponíveis
  - Usuário não pode ter empréstimos em atraso
  - Limite máximo de **5 empréstimos simultâneos** por usuário
  - Usuário não pode pegar o mesmo livro duas vezes sem devolver
- Prazo de devolução automático: **14 dias** a partir do empréstimo
- Registro de devolução (somente `LIBRARIAN`/`ADMIN`), com cálculo automático de multa por atraso (**R$ 2,00/dia**)
- Consulta de empréstimos por usuário (histórico completo ou apenas ativos)
- Listagem de todos os empréstimos ativos (uso administrativo)

### ⏳ Reservas (fila de espera)
- Reserva de um livro sem exemplares disponíveis, entrando na fila (FIFO)
- Cancelamento de reserva
- Consulta das reservas de um usuário
- Consulta da fila completa de um livro (uso administrativo)
- Ao devolver um livro, o próximo da fila é automaticamente sinalizado (`isNotified = true`) e o evento é registrado em log — pronto para, futuramente, ser conectado a um serviço real de notificação (e-mail, push, etc.)

### 🔐 Segurança e Autenticação
- Login via `POST /login`, retornando o token JWT no header `Authorization`
- Autorização stateless (sem sessão) com filtros customizados (`JWTAuthenticationFilter` / `JWTAuthorizationFilter`)
- Três perfis de acesso:
  - `ROLE_USER` — cliente da biblioteca
  - `ROLE_LIBRARIAN` — funcionário, gerencia livros e empréstimos
  - `ROLE_ADMIN` — acesso administrativo completo
- Regras de autorização centralizadas nos services (dono do recurso ou staff)
- CORS configurado para aceitar requisições de um front-end (`http://localhost:3000` por padrão)

## 🗺️ Endpoints principais

| Método | Rota                              | Descrição                                    | Acesso              |
|--------|------------------------------------|-----------------------------------------------|----------------------|
| POST   | `/user`                            | Cadastrar usuário                             | Público              |
| POST   | `/login`                           | Autenticar e obter token JWT                  | Público              |
| GET    | `/user/me`                         | Dados do usuário autenticado                  | Autenticado          |
| GET    | `/user/{id}`                       | Buscar usuário por ID                         | Dono ou ADMIN        |
| PUT    | `/user/{id}`                       | Atualizar senha                               | Dono ou ADMIN        |
| DELETE | `/user/{id}`                       | Remover usuário                               | Dono ou ADMIN        |
| GET    | `/book`                            | Listar livros (filtro por título/autor)       | Autenticado          |
| GET    | `/book/{id}`                       | Buscar livro por ID                           | Autenticado          |
| GET    | `/book/most-borrowed`              | Relatório de livros mais emprestados          | LIBRARIAN/ADMIN      |
| POST   | `/book`                            | Cadastrar livro                               | LIBRARIAN/ADMIN      |
| PUT    | `/book/{id}`                       | Atualizar livro                               | LIBRARIAN/ADMIN      |
| DELETE | `/book/{id}`                       | Remover livro (soft delete)                   | LIBRARIAN/ADMIN      |
| GET    | `/loan/{id}`                       | Buscar empréstimo por ID                      | Dono ou staff        |
| GET    | `/loan/user/{userId}`              | Histórico de empréstimos de um usuário        | Dono ou staff        |
| GET    | `/loan/user/{userId}/active`       | Empréstimos ativos de um usuário              | Dono ou staff        |
| GET    | `/loan/active`                     | Todos os empréstimos ativos                   | LIBRARIAN/ADMIN      |
| POST   | `/loan`                            | Criar empréstimo                              | Dono ou staff        |
| PUT    | `/loan/{id}/return`                | Registrar devolução                           | LIBRARIAN/ADMIN      |
| POST   | `/loan/reservation`                | Reservar livro (fila de espera)               | Dono ou staff        |
| DELETE | `/loan/reservation/{id}`           | Cancelar reserva                              | Dono ou staff        |
| GET    | `/loan/reservation/user/{userId}`  | Reservas de um usuário                        | Dono ou staff        |
| GET    | `/loan/reservation/book/{bookId}`  | Fila de reservas de um livro                  | LIBRARIAN/ADMIN      |

## 🛠️ Como executar o projeto

### Pré-requisitos
- JDK 21+
- Maven (ou use o `mvnw` incluído no projeto)
- MySQL rodando localmente (ou ajuste para usar o H2 em memória)

### Passos

1. Clone o repositório:
```bash
git clone https://github.com/GuiPrado275/librarySystem.git
cd librarySystem
```

2. Copie o arquivo de exemplo de configuração e ajuste com suas credenciais:
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

3. Edite `application.properties` com os dados do seu banco MySQL e um segredo JWT próprio:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/library_system?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=SUA_SENHA_AQUI
jwt.secret=SEU_SEGREDO_AQUI
```

4. Execute a aplicação:
```bash
./mvnw spring-boot:run
```

5. A API estará disponível em:
```
http://localhost:7777
```

## 📌 Regras de negócio (resumo)

| Regra                                            | Valor          |
|---------------------------------------------------|----------------|
| Prazo de empréstimo                               | 14 dias        |
| Máximo de empréstimos ativos por usuário           | 5              |
| Multa por dia de atraso                            | R$ 2,00        |
| Bloqueio de novo empréstimo com atraso pendente    | Sim            |

## 🧪 Testes

O projeto inclui testes com **Spring Boot Test**. Para rodar:
```bash
./mvnw test
```

## 📄 Licença

Projeto acadêmico/portfólio, de uso livre para fins de estudo.

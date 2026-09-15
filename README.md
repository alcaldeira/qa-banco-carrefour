# ServeRest API Tests

Suíte de testes automatizados para os endpoints de usuário da [ServeRest](https://serverest.dev/#/) (`/usuarios` e `/login`), construída com **Java 17 + RestAssured + JUnit 5**, seguindo o padrão **AAA (Arrange, Act, Assert)**, com **DTOs**, **validação de contrato (JSON Schema)**, **casos de negação de negócio** e **autenticação JWT reaproveitável**. A execução gera relatório **Allure**, publicado como artefato em uma pipeline de **GitHub Actions**.

## Sumário

- [Stack e decisões de arquitetura](#stack-e-decisões-de-arquitetura)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Como rodar localmente](#como-rodar-localmente)
- [Relatório de testes (Allure)](#relatório-de-testes-allure)
- [Pipeline de CI](#pipeline-de-ci)
- [Casos cobertos](#casos-cobertos)
- [Contrato da API e decisões de teste](#contrato-da-api-e-decisões-de-teste)
- [Sobre "100% de cobertura"](#sobre-100-de-cobertura)

## Stack e decisões de arquitetura

| Item | Escolha | Por quê |
|---|---|---|
| Linguagem / build | Java 17 + Maven | Padrão de mercado para automação de API com RestAssured; requisito do desafio já sugere RestAssured como opção. |
| Cliente HTTP | [RestAssured](https://github.com/rest-assured/rest-assured) | DSL fluente (`given().when().then()`), integra nativamente com JSON Schema e com Allure. |
| Runner | JUnit 5 | Padrão atual do ecossistema Java. |
| Serialização | Jackson + DTOs | Nenhum `Map`/JSON solto nos testes felizes — o corpo de requisição e resposta trafega como objeto tipado. `Map<String,String>` é usado apenas nos poucos casos de erro de validação, cujo formato é dinâmico (a API retorna uma chave por campo inválido). |
| Contrato | `json-schema-validator` (RestAssured) | Cada resposta relevante é validada contra um schema em `src/test/resources/schemas`, garantindo que a API não quebre o contrato silenciosamente. |
| Autenticação | `AuthManager` (código único, reaproveitado) | Cria um usuário administrador uma única vez por execução, faz `POST /login` e cacheia o token JWT (`Bearer ...`) para todas as chamadas autenticadas (`PUT`, `DELETE`). |
| Relatório | Allure (`allure-junit5` + `allure-rest-assured`) | Anexa request/response de cada chamada HTTP ao relatório, com histórico, categorização por `@Epic`/`@Feature` e captura de falhas. |
| Rate limit | `RateLimitRetryFilter` | A API impõe 100 req/min (requisito do desafio); o filtro reexecuta automaticamente qualquer chamada que receba `HTTP 429`, com backoff. A suíte também roda os testes **sequencialmente** (`junit-platform.properties`), evitando estourar o limite. |

### Padrão AAA

Todo teste segue explicitamente os três blocos comentados:

```java
@Test
void deveCriarUsuarioAdministradorComSucesso() {
    // Arrange
    UsuarioRequestDTO novoUsuario = UsuarioFactory.usuarioValidoAdministrador();

    // Act
    Response response = UsuariosClient.criar(novoUsuario);

    // Assert
    response.then().statusCode(201)...
}
```

O **Act** nunca encadeia asserções (`.then().statusCode(...)`) diretamente — a chamada retorna um `Response` puro, e a verificação acontece isolada no bloco **Assert**, deixando a intenção de cada etapa inequívoca.

### Separação por endpoint

Cada verbo/rota tem seu próprio arquivo de teste em `src/test/java/com/serverest/api/tests`:

| Arquivo | Endpoint |
|---|---|
| `GetUsuariosTest.java` | `GET /usuarios` (listagem, 1º GET) |
| `GetUsuarioPorIdTest.java` | `GET /usuarios/{id}` (consulta, 2º GET) |
| `PostUsuarioTest.java` | `POST /usuarios` |
| `PutUsuarioTest.java` | `PUT /usuarios/{id}` |
| `DeleteUsuarioTest.java` | `DELETE /usuarios/{id}` |
| `LoginTest.java` | `POST /login` (não é CRUD de usuário, mas é a base da autenticação JWT reaproveitada nos testes acima) |

## Estrutura do projeto

```
src/test/java/com/serverest/api/
├── auth/       AuthManager.java        → autenticação JWT reaproveitável (cria admin + login + cache do token)
├── base/       BaseTest.java           → setup único do RestAssured (baseURI, filtros, logging)
├── clients/    UsuariosClient.java     → wrapper único das chamadas HTTP de /usuarios
├── dto/        *.java                  → DTOs de request/response (UsuarioRequestDTO, UsuarioResponseDTO, ...)
├── factory/    UsuarioFactory.java     → massa de dados (usuário válido admin/não-admin, e-mail inválido, ...)
├── filters/    RateLimitRetryFilter.java → retry com backoff em HTTP 429
└── tests/      *.java                  → os 6 arquivos de teste listados acima

src/test/resources/
├── schemas/*.json                      → JSON Schemas para validação de contrato
└── junit-platform.properties           → execução sequencial (respeita o rate limit)
```

## Como rodar localmente

### Pré-requisitos

- JDK 17+
- Maven 3.9+ (ou use o `./mvnw` se você adicionar o wrapper; o projeto foi validado com Maven 3.9.9 instalado localmente)
- Acesso à internet (a suíte roda contra a API pública `https://serverest.dev`)

### Rodar a suíte

```bash
mvn clean test
```

Isso executa os 21 testes e grava os resultados em `target/surefire-reports` (JUnit) e `target/allure-results` (Allure).

Para apontar para outra instância da API (ex.: um mock local), sobrescreva a propriedade `api.base.url`:

```bash
mvn clean test -Dapi.base.url=http://localhost:3000
```

### Gerar e abrir o relatório Allure localmente

```bash
mvn allure:report
```

O relatório HTML estático é gerado em `target/site/allure-maven-plugin/index.html` — basta abrir esse arquivo no navegador.

> Alternativa, se você tiver o [Allure Commandline](https://allurereport.org/docs/gettingstarted-installation/) instalado: `allure serve target/allure-results`.

## Relatório de testes (Allure)

O relatório traz, por teste:

- Status (passou/falhou), duração e histórico entre execuções;
- Request e response completos (headers, body) anexados automaticamente via `AllureRestAssured`;
- Agrupamento por `@Epic("API de Usuários" / "Autenticação")` e `@Feature("GET /usuarios", "POST /usuarios", ...)`;
- Descrição de negócio de cada cenário via `@Description`.

## Pipeline de CI

Workflow: [`.github/workflows/ci.yml`](.github/workflows/ci.yml) (GitHub Actions).

Em todo `push`/`pull request` para `main` (e também disparável manualmente):

1. Configura o JDK 17 (com cache de dependências Maven);
2. Roda `mvn clean test`;
3. Publica um resumo dos resultados JUnit diretamente no resumo do workflow (`dorny/test-reporter`);
4. Gera o relatório HTML do Allure (`mvn allure:report`);
5. Publica três artefatos da execução (`actions/upload-artifact`), sempre — inclusive se algum teste falhar:
   - `allure-report` — relatório HTML navegável;
   - `allure-results` — dados brutos do Allure (útil para juntar histórico entre execuções);
   - `surefire-reports` — XML/TXT padrão do JUnit.

Para baixar: aba **Actions** → selecionar a execução → seção **Artifacts**, ao final da página do job.

## Casos cobertos

### `GET /usuarios`
- Listar todos os usuários com sucesso, validando o schema `{ quantidade, usuarios[] }` e que `quantidade` bate com o tamanho real da lista;
- Filtrar por `?nome=`, confirmando que **todos** os itens retornados correspondem ao filtro;
- Filtro sem correspondência retorna lista vazia (`quantidade: 0`), não erro — caso de borda.

### `GET /usuarios/{id}`
- Buscar por id existente, validando schema e que os dados batem com o que foi cadastrado;
- **Negação:** id em formato válido (16 caracteres alfanuméricos) mas inexistente → `400` + `"Usuário não encontrado"`;
- **Negação de contrato** (análogo a "CPF inválido"): id fora do formato esperado → `400` + `"id deve ter exatamente 16 caracteres alfanuméricos"`.

### `POST /usuarios`
- Criar usuário administrador com sucesso (`201`);
- Criar usuário não administrador com sucesso (`201`);
- **Negação:** e-mail duplicado → `400` + `"Este email já está sendo usado"`;
- **Negação:** corpo vazio → `400` com uma mensagem "é obrigatório" por campo (nome, email, password, administrador);
- **Negação:** e-mail em formato inválido → `400` + `"email deve ser um email válido"`;
- **Negação:** campo `administrador` fora do domínio `true`/`false` → `400` + `"administrador deve ser 'true' ou 'false'"`.

### `PUT /usuarios/{id}`
- Atualizar usuário existente com sucesso, usando o token JWT reaproveitável, e confirmar a persistência via `GET` subsequente;
- **Regra de negócio documentada:** `PUT` em um id **inexistente** cria um novo usuário (`201`, upsert) em vez de retornar `404` — comportamento real da API, coberto explicitamente;
- **Negação:** corpo vazio → mesmas validações de campo obrigatório do `POST`;
- **Negação:** e-mail inválido no update.

### `DELETE /usuarios/{id}`
- Excluir usuário existente com sucesso, usando o token JWT reaproveitável, e confirmar que o `GET` subsequente retorna `400`/"Usuário não encontrado";
- **Negação:** excluir id inexistente não é erro HTTP — retorna `200` + `"Nenhum registro excluído"`.

### `POST /login` (suporte à autenticação JWT)
- Login com sucesso retorna token no formato `Bearer <jwt>`, validado por regex no schema;
- **Negação:** credenciais inválidas → `401` + `"Email e/ou senha inválidos"`;
- **Negação:** corpo sem `email`/`password` → `400` com mensagem "é obrigatório" por campo.

**Total: 21 testes**, todos validados executando de fato contra `https://serverest.dev` (não há mocks).

## Contrato da API e decisões de teste

Alguns comportamentos da ServeRest foram confirmados empiricamente (chamadas reais) antes de virar asserção, para evitar testes que "parecem certos" mas não batem com a API real:

- **`_id` sempre tem exatamente 16 caracteres alfanuméricos** — usado tanto para gerar ids inexistentes válidos (`aaaaaaaaaaaaaaaa`) quanto para validar o schema.
- **`GET /usuarios/{id}` distingue "formato inválido" de "não encontrado"**: um id que não tem 16 caracteres alfanuméricos falha na validação de schema (`{"id": "..."}`) antes mesmo de consultar a base; um id no formato certo mas não cadastrado retorna `{"message": "Usuário não encontrado"}`. Os dois cenários são testados separadamente.
- **`PUT /usuarios/{id}` faz upsert**: id existente → `200`/atualiza; id inexistente → `201`/cria um novo registro. Isso é coberto como cenário de negócio, não como bug.
- **`DELETE` em id inexistente retorna `200`**, não `404`/`400` — a API usa o corpo (`"Nenhum registro excluído"`) para sinalizar o caso, não o status code. Testado como está.
- **`PUT`/`DELETE` de `/usuarios` não rejeitam chamadas sem token** na API pública (não há checagem de autorização nessa rota específica). Mesmo assim, a suíte **sempre** envia o header `Authorization: Bearer <jwt>` obtido via `AuthManager`, para exercitar o fluxo de autenticação JWT pedido no desafio e deixar a suíte pronta para o dia em que essa rota passar a exigir token.
- **Massa de dados sempre única (`UUID`)**: a base da ServeRest é pública e compartilhada por qualquer pessoa testando a API ao mesmo tempo; nomes/e-mails fixos causariam colisões e testes instáveis. Todo usuário criado pelos testes tem e-mail `algo.<uuid>@teste.com`.
- **Exclusão não é revertida**: por ser uma API sem endpoint de "reset", os testes não fazem cleanup explícito de tudo que criam — cada teste é responsável por deixar o sistema em um estado consistente para si mesmo (ex.: o teste de `DELETE` já remove o que cria). Isso é aceitável no escopo do desafio; em um ambiente com banco isolado por execução, o ideal seria um `@AfterEach` de limpeza.

## Sobre "100% de cobertura"

Como a ServeRest é uma API de terceiros consumida via HTTP (não há acesso ao código-fonte dela para instrumentar cobertura de linha/branch com uma ferramenta como o Jacoco), "100% de cobertura" aqui é interpretado como **cobertura funcional**: todos os endpoints pedidos (2× `GET`, `POST`, `PUT`, `DELETE`) têm pelo menos um caminho feliz e pelo menos um caso de negação/validação de negócio cobertos, incluindo os principais efeitos colaterais observáveis (ex.: confirmar via `GET` que um `PUT`/`DELETE` realmente persistiu a mudança).

Fora de escopo, por não pertencer ao CRUD de usuário pedido no desafio: exclusão de usuário vinculado a um carrinho (`/carrinhos`), filtros de listagem por `email`/`administrador`/`password`, e validação de parâmetros de query não suportados.

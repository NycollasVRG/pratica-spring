# Prática Spring — API de Notificação de Agravos (SINAN)

**Equipe:**
- **Nycollas Vinicius** — matrícula: `202422010021`
- **Carlos Victor** — matrícula: `202512010016`

CRUD REST completo de notificações de agravos, com regras de negócio (RN01–RN03),
padrão de erro **RFC 9457** (`application/problem+json`), paginação/ordenação com
*allowlist* e front-end vanilla servido pela própria aplicação.

---

## 1. Tecnologias

| Camada | Tecnologia |
| --- | --- |
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA, Bean Validation) |
| Persistência | Hibernate 7 + PostgreSQL (execução) |
| Testes | JUnit 5, MockMvc, Spring Data `@DataJpaTest` com **H2** (não precisa de Docker) |
| Build | Maven (wrapper `./mvnw`) |
| Front-end | HTML + CSS + JavaScript puro (sem frameworks, sem `innerHTML`) |
| Erros | RFC 9457 Problem Details |

Documentação de domínio: [`docs/NOTIFICACAO.md`](docs/NOTIFICACAO.md).

---

## 2. Como executar

### Pré-requisitos
- **JDK 21** instalado;
- **Docker** (apenas para subir o PostgreSQL de execução — os testes usam H2 e não precisam dele).

### Passo a passo

```bash
# 1) sobe o PostgreSQL (database "notificacao", usuário/senha "postgres")
docker compose up -d

# 2) sobe a aplicação (porta 8080)
./mvnw spring-boot:run        # Linux/macOS
mvnw.cmd spring-boot:run      # Windows

# 3) abre o front-end
#    http://localhost:8080
#    Credenciais padrão: admin / admin123
```

Alternativa com tudo em container (aplicação + banco):

```bash
docker compose --profile app up -d --build
```

### Testes (sem Docker, sem Postgres)

```bash
./mvnw test
```

Os testes usam H2 em memória (`src/test/resources/application.yaml`) e
MockMvc/Mockito — nenhum teste depende de Docker ou PostgreSQL.

### Configuração do banco

| Variável | Padrão | Descrição |
| --- | --- | --- |
| `GROUP_DB_USER` | `postgres` | usuário do PostgreSQL |
| `GROUP_DB_PASSWORD` | `postgres` | senha do PostgreSQL |

A URL padrão é `jdbc:postgresql://localhost:5432/notificacao`
(ao usar `--profile app`, ela é sobrescrita para `jdbc:postgresql://db:5432/notificacao`).

---

## 3. Endpoints

Base: `http://localhost:8080`

| Método | Rota | Sucesso | Erros principais |
| --- | --- | --- | --- |
| `POST` | `/notificacao` | `201 Created` + cabeçalho `Location` | `400` validação · `409` número já existe · `415` content-type |
| `GET` | `/notificacao` | `200` lista paginada (envelope) | `400` parâmetros inválidos |
| `GET` | `/notificacao/{numero}` | `200` | `404` |
| `PUT` | `/notificacao/{numero}` | `200` | `400` divergência URL×corpo ou validação · `404` |
| `DELETE` | `/notificacao/{numero}` | `204 No Content` | `404` |

Comportamentos gerais:
- todo corpo de erro é `Content-Type: application/problem+json` (inclusive `405`, `406`, `415` e o `500` de segurança);
- `PUT` **nunca** troca a chave primária: o número do corpo precisa ser igual ao da URL;
- método não suportado na rota devolve `405` com Problem Detail.

### Parâmetros de listagem (`GET /notificacao`)

| Parâmetro | Padrão | Descrição |
| --- | --- | --- |
| `uf` | — | igualdade com a UF (trim + maiúsculas) |
| `municipio` | — | igualdade ignorando caixa |
| `tipo` | — | `NEGATIVA`, `INDIVIDUAL`, `SURTO`, `TRACOMA` |
| `sexo` | — | `MASCULINO`, `FEMININO`, `IGNORADO` |
| `dataInicio` / `dataFim` | — | intervalo por `dataNotificacao` (inclusivo), formato `AAAA-MM-DD` |
| `duplicadas` | `false` | `true` → apenas notificações com duplicata (RN01, só nesta rota) |
| `pagina` | `0` | página inicial (negativa → `400`) |
| `tamanho` | `10` | itens por página, entre `1` e `100` (fora → `400`) |
| `ordenarPor` | `dataNotificacao` | *allowlist*: `dataNotificacao`, `numeroNotificacao`, `ufNotificacao`, `municipioNotificacao`, `nomePaciente`, `dataNascimento` (fora → `400`) |
| `direcao` | `desc` | `asc` ou `desc` (outra → `400`) |

Envelopa de listagem:

```json
{
  "conteudo": [ { "numeroNotificacao": "123", "agravoDoenca": "Febre", "paciente": { } } ],
  "pagina": 0,
  "tamanho": 10,
  "totalElementos": 1,
  "totalPaginas": 1
}
```

---

## 4. Regras de negócio

### RN01 — Duplicatas (`duplicadas=true`, somente na listagem)
Uma notificação é duplicata quando existe **outra** notificação (subquery `EXISTS`
correlacionada) com:
- o **mesmo agravo/doença**, comparado sem distinção de caixa e sem espaços das
  bordas (`lower(trim(...))` em ambos os lados);
- `dataNotificacao` dentro de **±3 dias** da data da linha atual (janela inclusiva);
- agravo **nulo ou em branco fica de fora** (nada duplica com vazio).

Como a escrita normaliza o texto (`Textos.normalizar`), espações internas repetidas
também não geram diferença.

### RN02 — Idade e gestante (`PacienteRequestDTO`)
- sem `dataNascimento`, a `idade` é obrigatória;
- sexo feminino: período gestacional obrigatório, **exceto** menores de 7 anos
  (esses podem omitir — a aplicação grava `NAO_SE_APLICA`);
- menores de 7 anos: `gestante` só pode ser nulo ou `NAO_SE_APLICA`;
- sexo diferente de feminino: `gestante` só pode ser nulo ou `NAO_SE_APLICA`;
- sexo nulo já é erro de `@NotNull` e as regras de gestante são puladas.

### RN03 — Residência (`EnderecoRequestDTO`)
- país nulo, em branco ou "Brasil" (ignorando caixa, acentos e espaços) →
  `ufResidencia` obrigatória;
- `ufResidencia` informada → `municipioResidencia` obrigatório;
- `paisResidencia` em branco é gravado como `Brasil`;
- a sigla de UF é validada estritamente (27 valores, maiúsculas).

---

## 5. Erros (RFC 9457)

Toda resposta de erro é `application/problem+json`, com `urn` próprio por status:

```http
HTTP/1.1 400 Bad Request
Content-Type: application/problem+json
```

```json
{
  "type": "urn:pratica:notificacao:requisicao-invalida",
  "title": "Requisição inválida",
  "status": 400,
  "detail": "A requisição contém erros de validação",
  "instance": "/notificacao",
  "errors": [
    { "campo": "paciente.nomePaciente", "mensagem": "O nome do paciente é obrigatório" }
  ]
}
```

| Status | `type` |
| --- | --- |
| 400 | `urn:pratica:notificacao:requisicao-invalida` |
| 404 | `urn:pratica:notificacao:nao-encontrado` |
| 405 | `urn:pratica:notificacao:metodo-nao-suportado` |
| 406 | `urn:pratica:notificacao:conteudo-nao-aceito` |
| 409 | `urn:pratica:notificacao:conflito` |
| 415 | `urn:pratica:notificacao:conteudo-nao-suportado` |
| 500 | `urn:pratica:notificacao:erro-interno` |

A extensão `errors` (lista de `{campo, mensagem}`) aparece quando há violações de
validação ou parâmetros de consulta inválidos.

---

## 6. API Collection e Exemplos (Postman / Insomnia)

Para facilitar os testes da API e simular a integração com o back-end, criamos uma **Collection completa** com payloads prontos para os 8 casos de uso mais importantes do sistema (incluindo tratamento das regras de negócio RN01 e RN02, respostas RFC 9457 e métodos de paginação). A Collection já possui a Autenticação HTTP Basic (`admin/admin123`) embutida.

> **Onde encontrar:** O arquivo está salvo na pasta raiz do repositório em `collection/SINAN_API_postman_collection.json`.

**Como utilizar:**
1. Abra o seu API Client preferido (Postman, Insomnia, Hoppscotch, Bruno, etc.).
2. Procure pelo botão **Import** e selecione o arquivo JSON que está na pasta `collection/`.
3. Pronto! As requisições aparecerão organizadas na barra lateral para você disparar com 1 clique (certifique-se de que a API está rodando na porta `8080`).

### Exemplos rápidos com `curl` (Via Terminal)

Caso você prefira ferramentas de linha de comando:

```bash
# criar (201 + Location)
curl -i -X POST http://localhost:8080/notificacao \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "numeroNotificacao": "123",
    "tipoNotificacao": "INDIVIDUAL",
    "agravoDoenca": "Febre",
    "dataNotificacao": "2026-01-10",
    "ufNotificacao": "PB",
    "municipioNotificacao": "João Pessoa",
    "unidadeSaudeNotificadora": "UBS Centro",
    "paciente": {
      "nomePaciente": "Maria Silva",
      "dataNascimento": "1990-05-20",
      "sexo": "FEMININO",
      "gestante": "NAO",
      "endereco": { "ufResidencia": "PB", "municipioResidencia": "João Pessoa" }
    }
  }'

# listar: ordenado por nome do paciente, só duplicadas da PB
curl -u admin:admin123 "http://localhost:8080/notificacao?uf=PB&duplicadas=true&sort=paciente.nomePaciente,asc&page=0&size=10"

# obter (200)
curl -u admin:admin123 http://localhost:8080/notificacao/123

# excluir (204)
curl -i -X DELETE -u admin:admin123 http://localhost:8080/notificacao/123
```

---

## 7. Front-end

Em `src/main/resources/static/` (`index.html`, `estilo.css`, `app.js`):

- listagem paginada com filtros (UF, município, tipo, sexo, período, duplicatas),
  escolha da ordenação e tamanho da página;
- formulário completo (notificação, paciente, residência, investigação) para
  criação e edição — o `PUT` usa o número original da edição, evitando troca
  acidental de chave primária;
- erros da API exibidos como Problem Detail (`title`, `detail` e lista `errors`);
- **sem `innerHTML`**: todo conteúdo dinâmico é montado com
  `createElement`/`createTextNode`.

---

## 8. Estrutura do projeto

```
src/main/java/com/pratica/notificacao/
├── common/          # Result<T,E>, Textos, ClockConfig
├── controller/      # NotificacaoController
├── domain/          # entidades, embeddables e enums (+ conversores)
├── dto/             # records de requisição/resposta (incl. PaginaRespostaDTO)
├── exception/       # ApiExceptionHandler, Problemas, ErroServico, ViolacaoCampo
├── mapper/          # DTO <-> entidade
├── repository/      # NotificacaoRepository (JPA + Specification)
├── service/         # regras de escrita (RN01–RN03 normalização) e listagem
├── specification/   # filtros, RN01 (EXISTS) e allowlist de ordenação
└── validation/      # constraints de classe (RegrasPaciente, RegrasResidencia, UfValida)

src/main/resources/static/   # front-end vanilla
src/test/                    # testes (H2 em memória — sem Docker)
docs/NOTIFICACAO.md          # documentação de domínio
docker-compose.yml           # PostgreSQL (+ perfil "app" para subir tudo)
```

---

## 9. Comandos úteis

```bash
./mvnw test                  # executa a suíte (H2, sem Docker)
./mvnw spring-boot:run       # sobe a aplicação na porta 8080
docker compose up -d         # só o PostgreSQL
docker compose --profile app up -d --build   # banco + aplicação em containers
docker compose down          # para e remove os containers
```

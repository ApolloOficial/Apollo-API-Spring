<div align="center">

# ☀️ Apollo API

### Gestão inteligente do ciclo de vida de painéis solares

API REST desenvolvida para centralizar o cadastro de lotes, painéis solares,
unidades empresariais e registros de manutenção do ecossistema **Apollo**.

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

</div>

---

## Sobre o projeto

O **Apollo API** é o back-end do Projeto Interdisciplinar Apollo. A solução foi
pensada para apoiar o acompanhamento de ativos fotovoltaicos, desde a aquisição
em lote e instalação dos painéis até o registro de manutenções.

Nesta etapa, a aplicação disponibiliza operações CRUD para **lotes** e
**painéis**, além de já possuir a modelagem inicial de empresas, unidades,
endereços, usuários, perfis de acesso, segmentos e manutenções.

## Funcionalidades atuais

- Cadastro, consulta, atualização e exclusão de lotes de painéis;
- Cadastro, consulta, atualização e exclusão de painéis solares;
- Associação de cada painel ao seu lote de origem;
- Persistência em banco de dados PostgreSQL com Spring Data JPA;
- Conversão entre entidades e DTOs na camada de serviço;
- Resposta HTTP `404 Not Found` centralizada para recursos inexistentes;
- Criação e atualização automática do esquema pelo Hibernate;
- Ambiente PostgreSQL reproduzível com Docker Compose.

## Arquitetura

O projeto segue uma arquitetura em camadas, mantendo as responsabilidades da
API separadas:

```mermaid
flowchart LR
    C[Cliente HTTP] --> CT[Controller]
    CT --> S[Service]
    S --> R[Repository]
    R --> J[Spring Data JPA]
    J --> DB[(PostgreSQL)]
    S <--> D[DTO]
    S <--> M[Model]
    E[Exception Handler] -. respostas de erro .-> C
```

```text
src/
├── main/
│   ├── java/org/apollo/api/
│   │   ├── controller/   # Endpoints REST
│   │   ├── dto/          # Objetos de entrada e saída
│   │   ├── enums/        # Estados controlados do domínio
│   │   ├── exception/    # Exceções e tratamento centralizado
│   │   ├── model/        # Entidades JPA
│   │   ├── repository/   # Acesso aos dados
│   │   └── service/      # Regras e casos de uso
│   └── resources/        # Configuração da aplicação
└── test/                 # Testes automatizados
```

## Tecnologias

| Tecnologia | Uso no projeto |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot 4.1 | Configuração e execução da aplicação |
| Spring Web MVC | Construção dos endpoints REST |
| Spring Data JPA | Persistência e acesso ao banco |
| PostgreSQL 17 | Banco de dados relacional |
| Hibernate | Mapeamento objeto-relacional |
| Lombok | Redução de código repetitivo |
| Maven Wrapper | Build e gerenciamento de dependências |
| Docker Compose | Banco local conteinerizado |

## Como executar

### Pré-requisitos

- [Java 21](https://adoptium.net/temurin/releases/?version=21);
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) com Docker
  Compose.

> O Maven não precisa ser instalado: o repositório inclui o Maven Wrapper.

### 1. Clone o repositório

```bash
git clone https://github.com/ApolloOficial/Apollo-API-Spring.git
cd Apollo-API-Spring
```

### 2. Inicie o PostgreSQL

```bash
docker compose up -d
```

O container será iniciado na porta `5432` e armazenará os dados no volume
`apollo_postgres_data`.

### 3. Execute a API

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

A aplicação ficará disponível em `http://localhost:8080`.

> O Spring Security já consta nas dependências, mas a autenticação definitiva
> da API ainda está em desenvolvimento. Enquanto não houver uma configuração
> própria, o Spring poderá gerar uma senha temporária no console ao iniciar.

## Variáveis de ambiente

A aplicação possui valores locais padrão, que podem ser sobrescritos sem
alterar o código-fonte:

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `dbapollo` | Nome do banco |
| `DB_USER` | `apollo` | Usuário do banco |
| `DB_PASSWORD` | `apollo123` | Senha do banco |

Exemplo no PowerShell:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "5432"
$env:DB_NAME = "dbapollo"
$env:DB_USER = "apollo"
$env:DB_PASSWORD = "sua_senha"
.\mvnw.cmd spring-boot:run
```

> Em produção, utilize segredos da plataforma de hospedagem e nunca versione
> credenciais reais.

## Integração com a Sungrow (iSolarCloud)

A API pode ler, na nuvem da Sungrow, a **tensão e a corrente de cada string** dos inversores Sungrow
cadastrados no Apollo e gravá-las em `string_performance_measurement` (via `pr_ingest_string_measurement`,
`data_source = SENSOR`). A integração vem **desligada** por padrão.

| Variável | Padrão | Descrição |
|---|---|---|
| `SUNGROW_ENABLED` | `false` | Liga a integração |
| `SUNGROW_BASE_URL` | `https://gateway.isolarcloud.com.hk` | Domínio da região (o portal não diz qual atende o Brasil: confirmar) |
| `SUNGROW_APPKEY` | — | `appkey` do aplicativo no portal de desenvolvedores |
| `SUNGROW_ACCESS_KEY` | — | `x-access-key` do aplicativo |
| `SUNGROW_REDIRECT_URI` | — | Mesmo redirect URI cadastrado no aplicativo |
| `SUNGROW_POLL_INTERVAL_SECONDS` | `300` | Intervalo da leitura (mínimo 300: a Sungrow atualiza a cada ~5 min) |
| `SUNGROW_BRAND` | `Sungrow` | Texto procurado em `inverter_model.brand` |
| `SUNGROW_DEVICE_TIME_ZONE` | `America/Sao_Paulo` | Fuso do `device_time` devolvido pela Sungrow |
| `SUNGROW_BATCH_SIZE` | `20` | Inversores por chamada |
| `SUNGROW_PLANT_MODE` | `live` | `live` (planta real, chama a API da Sungrow) ou `simulated` (planta simulada na própria API) |
| `SUNGROW_DATA_SOURCE` | `SENSOR` | Valor gravado em `data_source` na planta real. Planta simulada sempre grava `SIMULATOR` |

Como ligar:

1. Confirme que a tabela `sungrow_credential` existe no banco que a API usa (Neon).
2. Defina as variáveis acima (nunca versione `appkey`/`access key`) e suba a API.
3. O dono da planta autoriza o aplicativo na iSolarCloud; a Sungrow redireciona para o `redirect_uri` com `?code=...`.
4. Um **Gerente** envia o código: `POST /api/v1/integrations/sungrow/authorization` com `{"code": "..."}`
   (o front faz isso na rota `/integrations/sungrow/callback`; use essa rota do front como `SUNGROW_REDIRECT_URI`).
   `GET /api/v1/integrations/sungrow/status` (Gerente e Operador, em qualquer modo) devolve
   `{enabled, mode, authorized, accessTokenExpiresAt, authorizedPlants}`, com `mode` = `DISABLED`, `SIMULATED` ou `LIVE`
   (os tokens nunca são devolvidos; `authorized` só vale em `LIVE`).
5. A cada intervalo a API lê os inversores Sungrow **ativos** (a chave é `inverter.serial_number` = `device_sn`)
   e grava só as strings **ativas** (`ATIVA`/`MANUTENCAO`).

**Planta simulada (demonstração, sem inversor real):** com `SUNGROW_PLANT_MODE=simulated` a API continua com o mesmo
fluxo (mapeamento por string, `pr_ingest_string_measurement`, banco), mas a parte da **planta** é simulada dentro da
própria API (`SungrowSimulatedPlant`): ela gera, no formato da Sungrow, a leitura que um inversor devolveria (curva de
sol, variação por string, ruído). Não precisa de `appkey`, de autorização nem de rede, e as linhas são gravadas com
`data_source = SIMULATOR`. Basta `SUNGROW_ENABLED=true` e `SUNGROW_PLANT_MODE=simulated` (opcional:
`SUNGROW_POLL_INTERVAL_SECONDS=30`, `SUNGROW_SIMULATION_STEP_MINUTES=1`, `SUNGROW_SIMULATION_ALWAYS_SUN=true`,
`SUNGROW_SIMULATION_FAULT_SERIALS=SERIAL1` para o inversor reportar falha e `SUNGROW_SIMULATION_WEAK_STRINGS=SERIAL1:2:0.5`
para a string 2 produzir só 50%). Para usar a planta real basta voltar para `live`. Isso valida o fluxo da API, **não**
prova que um inversor real devolve os mesmos pontos.

Regras de mapeamento (ver `SungrowPointMapper`): potência = tensão × corrente (a Sungrow não tem potência nem energia
por string, então `generated_energy_kwh` fica nulo); a string N da Sungrow vira MPPT/entrada usando
`strings_per_mppt` do modelo; `fault_code = SUNGROW_FAULT` só quando `dev_fault_status = 1`
(o banco abre um alerta `FALHA_INVERSOR` a partir dele).

## Endpoints disponíveis

### Lotes

Base URL: `/api/v1/batches`

| Método | Rota | Ação |
|---|---|---|
| `GET` | `/api/v1/batches` | Lista todos os lotes |
| `GET` | `/api/v1/batches/{id}` | Consulta um lote pelo ID |
| `POST` | `/api/v1/batches` | Cadastra um lote |
| `PUT` | `/api/v1/batches/{id}` | Atualiza um lote |
| `DELETE` | `/api/v1/batches/{id}` | Exclui um lote |

Exemplo de corpo para criação:

```json
{
  "billNumber": "NF-2026-001",
  "manufacturer": "Solar Tech",
  "model": "ST-550M",
  "acquisitionDt": "2026-05-14",
  "panelsQtt": 100
}
```

### Painéis

Base URL: `/api/panels`

| Método | Rota | Ação |
|---|---|---|
| `GET` | `/api/panels` | Lista todos os painéis |
| `GET` | `/api/panels/{id}` | Consulta um painel pelo ID |
| `POST` | `/api/panels` | Cadastra um painel |
| `PUT` | `/api/panels/{id}` | Atualiza um painel |
| `DELETE` | `/api/panels/{id}` | Exclui um painel |

Exemplo de corpo para criação:

```json
{
  "batchId": 1,
  "coUnityId": 10,
  "estimatedLifeCycle": 25,
  "serialNumber": "APL-ST550-0001",
  "barcode": "789000000001",
  "operatingStatsEnum": "Operacional",
  "ratedEfficiency": 21.5,
  "installationDt": "2026-06-01"
}
```

O campo `batchId` deve apontar para um lote já cadastrado.

## Requisitos da disciplina — Desenvolvimento 2

Esta seção relaciona a implementação atual aos critérios definidos para a
disciplina em maio de 2026.

| Requisito | Situação | Evidência no projeto |
|---|:---:|---|
| API REST em Java com Spring MVC e PostgreSQL | ✅ | Controllers REST, Spring Web MVC e configuração do datasource PostgreSQL |
| Integração com PostgreSQL usando Spring Data JPA | ✅ | Entidades JPA e interfaces em `repository/` |
| Métodos CRUD por meio da API | ✅ | CRUD completo de lotes e painéis |
| Separação de responsabilidades no padrão MVC | ✅ | Pacotes `controller`, `service`, `repository`, `model` e `dto` |
| Validação das entradas recebidas | 🚧 | Restrições existem nas entidades; validação de DTOs com Bean Validation ainda é necessária |
| Tratamento centralizado de exceções e respostas HTTP úteis | 🚧 | `GlobalExceptionHandler` trata recursos não encontrados; demais erros ainda precisam ser padronizados |
| Documentação automática com Swagger | ⏳ | Planejado |
| Acionamento de procedures e functions do banco | ⏳ | Planejado |
| Autenticação e autorização com Spring Security | 🚧 | Dependências de Spring Security e JWT adicionadas; fluxo de autenticação ainda não concluído |
| API REST adicional para banco NoSQL | ⏳ | Extra planejado |

**Legenda:** ✅ concluído · 🚧 em desenvolvimento · ⏳ planejado

## Próximos passos

- Adicionar Bean Validation aos DTOs e `@Valid` nos controllers;
- padronizar respostas de erro para validação, regras de negócio e banco;
- concluir autenticação e autorização com Spring Security e JWT;
- documentar os endpoints com OpenAPI/Swagger;
- ampliar os CRUDs para as demais entidades do domínio;
- adicionar testes unitários, de integração e de controller;
- integrar procedures e functions desenvolvidas no PostgreSQL;
- preparar configuração segura para implantação em nuvem.

## Testes

Execute a suíte automatizada com:

No Windows:

```powershell
.\mvnw.cmd test
```

No Linux ou macOS:

```bash
./mvnw test
```

## Contribuição

1. Crie uma branch a partir de `main`: `git switch -c feat/minha-feature`;
2. faça commits seguindo o padrão
   [Conventional Commits](https://www.conventionalcommits.org/pt-br/v1.0.0/);
3. envie a branch para o GitHub;
4. abra um Pull Request descrevendo a alteração e como validá-la.

## Licença

Distribuído sob a licença MIT. Consulte o arquivo [LICENSE](LICENSE) para mais
informações.

---

<div align="center">

Desenvolvido pela equipe **Apollo** para o Projeto Interdisciplinar 2026. 🚀

</div>

## Event tracking (MongoDB)

A API recebe eventos de uso do app em `POST /api/tracking/events/batch` (autenticado) e grava no MongoDB, separado do PostgreSQL. Vem **desligado** por padrão.

| Variável | Padrão | Descrição |
|---|---|---|
| `TRACKING_ENABLED` | `false` | Liga o tracking |
| `TRACKING_MONGO_URI` | `MONGODB_URI` ou `mongodb://localhost:27017` | URI do MongoDB (nunca versione credenciais) |
| `TRACKING_SPOOL_DIR` | `./var/tracking-spool` (no Docker: `/var/lib/apollo/tracking-spool`) | Fila em disco para reenviar eventos de negócio |
| `TRACKING_FLUSH_DELAY_MS` | `10000` | Intervalo do reenvio da fila |

- O reenvio roda num agendador próprio (`trackingTaskScheduler`), para que uma demora no Mongo não atrase a ingestão da Sungrow nem o envio de push.
- No Docker Compose, a fila fica no volume `apollo_tracking_spool`.
- Configuração da collection e do validator: ver `APOLLO_MONGODB_SETUP.md` e `tracking-validator.json`.

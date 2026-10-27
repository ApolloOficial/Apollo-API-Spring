# ☀️ Apollo API

<p align="center">
  <strong>API de gestão de ativos fotovoltaicos da plataforma Apollo</strong>
</p>

<p align="center">
  <a href="https://github.com/ApolloOficial/Apollo-API-Spring/actions/workflows/ci.yml"><img alt="CI" src="https://github.com/ApolloOficial/Apollo-API-Spring/actions/workflows/ci.yml/badge.svg"></a>
  <img alt="Java 21" src="https://img.shields.io/badge/Java%2021-ED8B00">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-6DB33F">
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-4169E1">
  <img alt="MongoDB" src="https://img.shields.io/badge/MongoDB-47A248">
</p>

A Apollo API centraliza as operações utilizadas pelo Web e Mobile. Desenvolvida com Java, Spring Boot e JPA, mantém dados operacionais no PostgreSQL e oferece autenticação, gestão de ativos, manutenção e integrações.

## Como funciona

- **API REST:** controllers recebem as operações dos clientes.
- **Segurança:** Spring Security e JWT controlam autenticação e acesso.
- **Persistência:** serviços e repositories acessam o PostgreSQL; o Hibernate valida o schema existente.
- **Integrações:** gateway do chatbot, Sungrow, notificações Firebase e tracking em MongoDB possuem configurações próprias.

```text
Web / Mobile → filtro de segurança (JWT) → controller → service → repository → PostgreSQL
                                                       ├→ gateway ApolloAI
                                                       ├→ integração Sungrow
                                                       ├→ notificações Firebase
                                                       └→ tracking → MongoDB
```

Os controllers recebem e validam as requisições, os services aplicam as operações do domínio, e os repositories acessam os dados relacionais. As integrações são chamadas a partir dos serviços correspondentes.

## Início rápido

Requisitos: JDK 21, PostgreSQL com o schema do Apollo preparado e Maven Wrapper incluído no repositório.

```bash
git clone https://github.com/ApolloOficial/Apollo-API-Spring.git
cd Apollo-API-Spring
```

Configure as variáveis de ambiente ou crie um `.env` local em formato de propriedades:

```dotenv
DB_HOST=localhost
DB_PORT=5432
DB_NAME=apollodb
DB_USER=apollo
DB_PASSWORD=preencha-localmente
DB_SSLMODE=disable
JWT_SECRET=preencha-com-um-segredo-adequado-ao-ambiente
```

O exemplo de SSL é para banco local. Para o banco remoto, use a configuração exigida pelo provedor; o padrão do código é `require`. O schema deve existir antes da execução, pois `ddl-auto=validate` não cria tabelas.

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux/macOS, use `./mvnw spring-boot:run`. A porta padrão é 8080. Configure `PORT` para alterá-la.

## API

Os controllers abrangem autenticação, empresas, filiais, funcionários, painéis, inversores, strings, estoque, alertas, manutenções, realocações e configurações.

A documentação OpenAPI depende de `SWAGGER_ENABLED`. Exemplos de rotas implementadas:

| Método | Rota | Uso |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Autentica o usuário |
| `GET` | `/api/v1/panels/barcode/{barcode}` | Consulta uma placa pelo código |
| `POST` | `/api/v1/chat/messages` | Envia mensagem ao ApolloAI |
| `PATCH` | `/api/v1/maintenance-registers/{id}/start` | Inicia uma ordem de serviço |
| `GET` | `/api/v1/maintenance-registers/{id}/chain` | Consulta a sequência de uma ordem de serviço |

O gateway do chatbot utiliza `CHATBOT_BASE_URL` e `CHATBOT_API_TOKEN`. Consulte os controllers para os parâmetros, permissões e contratos completos.

Tracking em MongoDB e Sungrow são ativados por `TRACKING_ENABLED` e `SUNGROW_ENABLED`. As integrações possuem requisitos adicionais descritos no guia.

## Testes

```powershell
.\mvnw.cmd test
```

Além dos testes automatizados, valide os fluxos alterados com os clientes e bancos correspondentes. Registre separadamente os resultados de simulação e integração real.

## Documentação

- [Configuração da aplicação](src/main/resources/application.properties)
- [Integrações Sungrow e tracking](docs/INTEGRATIONS.md)
- [Configuração MongoDB](APOLLO_MONGODB_SETUP.md)
- [Dependências e build](pom.xml)
- [Web](https://github.com/ApolloOficial/Apollo-Web) · [Mobile](https://github.com/ApolloOficial/Apollo-Mobile)


# Apollo — MongoDB Atlas e Event Tracking

Guia conferido em 23/10/2026, para `C:/InterdisciplinarApollo/Apollo-API-Spring`, branch `feat/api-event-tracking`.

**Estado:** a integração local foi implementada. Conta, projeto, cluster, credenciais e IPs devem ser configurados por você. Nenhum recurso Atlas foi criado por automação e nenhuma conexão Atlas foi comprovada sem sua URI. Não enviar senhas/JWTs neste chat.

## 1. Criar a conta Atlas

1. Abra [MongoDB Atlas](https://www.mongodb.com/cloud/atlas) e use a opção de cadastro gratuito; quem já tem conta pode acessar [MongoDB Cloud](https://cloud.mongodb.com/).
2. Cadastre-se com a opção de identidade disponível ou e-mail, confirme o e-mail e faça login. Ative MFA na conta de administração.
3. Complete as perguntas iniciais. Elas personalizam o onboarding, não são configuração do banco.
4. Se o onboarding já criou organização e projeto, aproveite a organização; não é necessário duplicá-la.

A conta que acessa o site **não é o usuário do banco utilizado pelo Java**. [Cadastro oficial](https://www.mongodb.com/docs/atlas/tutorial/create-atlas-account/).

## 2. Organização, projeto Apollo e cluster gratuito

No seletor **Organizations**, use uma organização existente. Se precisar criar outra, abra a visão das organizações e escolha a ação de criação; dê um nome como `Apollo`. Uma organização só agrupa projetos, membros e políticas. [Organizações](https://www.mongodb.com/docs/atlas/access/orgs-create-view-edit-delete/).

No seletor **Projects**, selecione **+ New Project** (ou **All Projects → New Project**). Digite `Apollo`, avance com **Next**, mantenha os membros necessários e conclua em **Create Project**. [Projetos](https://www.mongodb.com/docs/atlas/tutorial/manage-projects/).

Para o cluster:

1. Entre no projeto `Apollo`, abra **Project Overview** e a ação **Create**. Em projetos já utilizados a mesma ação pode estar na área **Clusters**.
2. Selecione **Free / M0**. Confira o resumo gratuito antes de confirmar. **Flex e Dedicated não são a opção gratuita deste guia.** Não faça upgrade nem habilite extras pagos.
3. Provedor: use AWS como opção inicial de desenvolvimento. Região: se São Paulo estiver disponível no seletor **mantendo Free/M0**, pode utilizá-la. Caso contrário, selecione **Northern Virginia / us-east-1** se disponível gratuitamente. Para produção, prefira o mesmo provedor/região do backend; estar no Brasil não exige pagar por uma região que o plano gratuito não oferece. Essa escolha é uma recomendação de arquitetura, não garantia de menor latência.
4. No campo **Name**, informe `apollo-cluster` (não poderá renomeá-lo depois da criação).
5. Confira novamente o plano, selecione **Create** e aguarde o provisionamento. Dispense sample datasets para este módulo.

A interface exibe apenas regiões disponíveis para o plano escolhido; não confundir a lista de regiões dos planos pagos com a do Free. [Criação Free](https://www.mongodb.com/docs/atlas/tutorial/deploy-free-tier-cluster/), [regiões AWS](https://www.mongodb.com/docs/atlas/reference/amazon-aws/).

Os nomes de navegação podem variar entre onboarding e painel já configurado. Nas instruções abaixo, a documentação atual agrupa segurança em **Security → Database & Network Access**; algumas interfaces ainda mostram **Database Access** e **Network Access** separados.

## 3. Usuário do banco e permissões mínimas

Crie dois acessos com finalidades diferentes: um provisionador temporário para collection/índices e `apollo_tracking_app` para o backend.

1. Abra **Security → Database & Network Access** e a aba de usuários de banco (**Database Users** / **Database Access**, conforme a interface).
2. Use **Add New Database User**, autenticação por senha (**Password / SCRAM**), nome `apollo_tracking_app` e uma senha gerada e guardada em gerenciador de segredos.
3. Para restringir o aplicativo, vá antes à aba **Custom Roles → Add New Custom Role**.
4. Nomeie o papel `apollo_tracking_writer`. Em **Collection Actions**, acrescente `find`, `insert` e `update`, com database `apollo_analytics` e collection `tracking_events` em cada privilégio. Salve em **Add Custom Role**.
5. Volte ao usuário e atribua somente esse papel customizado. Não somar `atlasAdmin`, `readWriteAnyDatabase`, nem papéis globais: as permissões se acumulam.
6. Quando disponível, habilite **Restrict Access to Specific Clusters** para `apollo-cluster`. Salve o usuário e aguarde a propagação.

O aplicativo usa upsert e consulta de deduplicação: precisa de `insert`, `update` e `find`. Não precisa excluir eventos ou administrar índices. O provisionador usa privilégio de administração **somente de `apollo_analytics`**, por tempo limitado; não reutilize suas credenciais no Spring. Se a UI oferecer somente o editor de papéis por database, `readWrite` restrito a `apollo_analytics` é um ponto de partida mais amplo que o mínimo; prefira o papel customizado descrito acima.

Usuários/papéis Atlas devem ser administrados pelo painel/CLI Atlas, não por `db.createUser` no Compass. [Usuários](https://www.mongodb.com/docs/atlas/security-add-mongodb-users/), [papéis customizados](https://www.mongodb.com/docs/atlas/security-add-mongodb-roles/).

## 4. Acesso de rede

Em **Database & Network Access**, abra **IP Access List** (na interface antiga, **Network Access**). Use **Add IP Address** e a opção de adicionar o IP público atual quando oferecida. Salve uma entrada `/32`, com descrição do computador/rede e expiração curta para desenvolvimento. Se o IP doméstico mudar, atualize a entrada.

Quando o Spring rodar hospedado, autorize os **IPs públicos de saída do serviço do backend**, obtidos no painel do provedor. O IP do seu computador não autoriza automaticamente o servidor hospedado; o IP do celular também não é o que o Mongo vê, pois só o Spring conecta ao Atlas. Se houver VPN, considere o IP de saída dela. Não manter `0.0.0.0/0`.

Use saída TCP para os hosts/portas Atlas e DNS SRV/TXT funcionais. Não abra uma porta de entrada no celular. [IP access list](https://www.mongodb.com/docs/atlas/security/ip-access-list/), [pré-requisitos de conexão](https://www.mongodb.com/docs/atlas/connect-to-database-deployment/).

## 5. Conectar pelo Compass e criar database/collection

Depois de trocar a senha exposta, abra o **MongoDB Compass** (edição normal, não Readonly). Use **Add New Connection** na área **Connections** e cole a URI nova copiada em **Atlas → Connect → Compass**. Acrescente `/apollo_analytics` antes de `?` na URI e mantenha as opções de conexão recomendadas pelo Atlas. Nomeie a conexão `Apollo Tracking` e clique em **Connect**. A URI do banco fica somente no Compass/ambiente do backend, nunca no repositório. [Conexões Compass](https://www.mongodb.com/docs/compass/connect/).

Para provisionar, a credencial precisa de permissão de criação de collection, índices e validator. A conta mínima do Spring continua somente com `find/insert/update`; não aumentar permanentemente suas permissões para administrar o banco.

Na visão **Databases**, clique em **Create Database**:

| Campo | Valor |
|---|---|
| Database Name | `apollo_analytics` |
| Collection Name | `tracking_events` |

Confirme em **Create Database**. Se já existir, apenas abra o database/collection; não recrie nem apague dados. Use collection normal, sem time-series/capped. Não criar `tracking_sessions`: o `session_id` dos eventos já permite agrupamento.

Estrutura final:

```text
Project: Apollo
└── Cluster: apollo-cluster (ou o nome que você já criou no Atlas)
    └── Database: apollo_analytics
        └── Collection: tracking_events
```

O nome do cluster exibido na sua conta pode diferir do nome sugerido; o hostname da URI é o que efetivamente identifica a conexão. Não recriar cluster só para trocar nome. Project/cluster/usuários/IPs continuam sendo administrados no Atlas. Database/collection/índices/validator podem ser configurados pelo Compass, sem shell ou JavaScript.

## 6. Criar índices e validação pelo Compass

### 6.1 Índice único do evento

Abra `apollo_analytics → tracking_events → Indexes → Create Index`:

| Opção | Valor |
|---|---|
| Campo | `event_id` |
| Ordem | `1 (Ascending)` |
| Nome, quando disponível | `ux_tracking_event_id` |
| Unique | Ativado |

Confirme **Create Index**. Não alterar o índice padrão `_id_`.

### 6.2 Índice para leitura incremental

Crie outro índice com **dois campos**, nesta ordem. Use a ação de acrescentar campo do formulário:

| Ordem | Campo | Direção |
|---|---|---|
| 1 | `received_at` | `1 (Ascending)` |
| 2 | `event_id` | `1 (Ascending)` |

Nome: `ix_tracking_incremental`, se o formulário permitir. **Unique desativado**. Salve e confira os dois índices na lista. Se já existirem índices equivalentes, não duplicar por diferença de nome. E11000 durante criação significa duplicatas anteriores a investigar, não autorização para apagar a collection. [Índices Compass](https://www.mongodb.com/docs/compass/indexes/).

### 6.3 Validator em JSON

ATUALIZAÇÃO UX: o arquivo `tracking-validator.json` na raiz contém o validator completo atualizado.
Para os novos eventos, substitua a regra antiga por esse JSON na aba Validation do Compass,
mantendo Strict/Error. O exemplo abaixo também contém a allowlist atualizada.
Não exclua a collection nem os documentos. Reinicie o Spring após alterar o .env e instale o APK novo.

Na mesma collection, abra **Validation**. No editor de regras, use o JSON abaixo. Se o editor estiver somente leitura, conferir edição do Compass e permissões. Escolha **Validation Action: Error** e **Validation Level: Strict** e aplique usando a ação de salvar/atualizar regras da sua versão. JSON de configuração não é um script JavaScript; não precisa abrir o shell. [Validação Compass](https://www.mongodb.com/docs/compass/validation/).

```json
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": ["event_id", "event_name", "user_id", "company_id", "session_id", "screen", "timestamp", "received_at", "platform", "source", "schema_version", "properties"],
    "additionalProperties": false,
    "properties": {
      "_id": {"bsonType": "objectId"},
      "event_id": {"bsonType": "string", "pattern": "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"},
      "event_name": {"enum": ["app_opened","screen_viewed","work_order_viewed","work_order_start_clicked","barcode_scanned","report_started","action_failed","app_foregrounded","app_backgrounded","screen_exited","ui_action","repeated_action","form_started","form_submitted","form_succeeded","form_abandoned","validation_failed","filter_changed","search_completed","empty_state_viewed","retry_clicked","feedback_shown","permission_result","api_request_completed","api_request_failed","work_order_started","report_submitted","work_order_completed"]},
      "user_id": {"bsonType": "string", "pattern": "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"},
      "company_id": {"bsonType": ["long", "int"]},
      "session_id": {"bsonType": "string", "pattern": "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"},
      "screen": {"bsonType": "string", "pattern": "^[A-Za-z][A-Za-z0-9_]{0,79}$"},
      "timestamp": {"bsonType": "date"},
      "received_at": {"bsonType": "date"},
      "platform": {"enum": ["android", "api"]},
      "source": {"enum": ["android", "api"]},
      "schema_version": {"bsonType": "int", "enum": [1]},
      "properties": {
        "bsonType": "object",
        "maxProperties": 16,
        "additionalProperties": false,
        "properties": {
          "work_order_id": {"bsonType": "string", "maxLength": 128},
          "maintenance_type_id": {"bsonType": "string", "maxLength": 128},
          "action": {"bsonType": "string", "maxLength": 128},
          "error_type": {"bsonType": "string", "maxLength": 128},
          "input_method": {"bsonType": "string", "maxLength": 128},
          "previous_screen": {"bsonType": "string", "maxLength": 128},
          "duration_ms": {"bsonType": "string", "maxLength": 128},
          "exit_reason": {"bsonType": "string", "maxLength": 128},
          "form_id": {"bsonType": "string", "maxLength": 128},
          "field": {"bsonType": "string", "maxLength": 128},
          "rule": {"bsonType": "string", "maxLength": 128},
          "target": {"bsonType": "string", "maxLength": 128},
          "attempts": {"bsonType": "string", "maxLength": 128},
          "filter_name": {"bsonType": "string", "maxLength": 128},
          "filter_value": {"bsonType": "string", "maxLength": 128},
          "query_length": {"bsonType": "string", "maxLength": 128},
          "result_count": {"bsonType": "string", "maxLength": 128},
          "state": {"bsonType": "string", "maxLength": 128},
          "feedback_type": {"bsonType": "string", "maxLength": 128},
          "permission": {"bsonType": "string", "maxLength": 128},
          "granted": {"bsonType": "string", "maxLength": 128},
          "http_status": {"bsonType": "string", "maxLength": 128},
          "method": {"bsonType": "string", "maxLength": 128},
          "endpoint": {"bsonType": "string", "maxLength": 128},
          "error_code": {"bsonType": "string", "maxLength": 128}
        }
      }
    }
  }
}
```

Em collection já populada, revisar os documentos antes de ativar regras estritas. A validação não converte documentos antigos. O Spring também valida nomes, tipos e propriedades; alterar a allowlist exige coordenar API, Android e schema. Não criar índices isolados para todo campo, nem TTL sem política de retenção.

## 7. Obter e montar a connection string

No cluster `apollo-cluster`, use **Connect → Drivers**, escolha **Java** e uma versão de driver compatível com a gerenciada pelo Maven. Copie a URI que o Atlas mostrar; o domínio exato vem da sua conta, não do nome do cluster sozinho. [Conexão oficial](https://www.mongodb.com/docs/atlas/connect-to-database-deployment/).

Formato de exemplo, **não é uma credencial funcional**:

```text
mongodb+srv://USUARIO:SENHA_PERCENT_ENCODED@HOST_REAL.mongodb.net/apollo_analytics?retryWrites=true&w=majority&authSource=admin&appName=ApolloTracking
```

Substitua usuário e senha pelos do banco (não pelo login Atlas). Codifique caracteres especiais da senha na URI; por exemplo, `@` vira `%40`. Não codifique a URI inteira, apenas os componentes de credencial. Não usar `<`/`>` dos placeholders na URI final. SCRAM do Atlas autentica em `admin`, embora os dados sejam de `apollo_analytics`. SRV usa TLS; não configurar `tls=false`, certificados inseguros ou SSL desativado.

## 8. Integração Spring Boot implementada

O projeto usa Java 21 e Spring Boot 4.1.0. Foram preservados o endpoint único `POST /api/tracking/events/batch`, JWT existente, `TenantContext`, procedures e transações JPA.

Dependências novas/atuais em `pom.xml`, ambas com versões herdadas do parent:

```xml
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-mongodb</artifactId>
</dependency>
```

`MongoTrackingConfig` configura client com timeouts/write concern e um `MongoTemplate` **nomeado** `trackingMongoTemplate`, apenas quando tracking está ligado. `TrackingRepository` usa `MongoOperations` e `$setOnInsert` com upsert; não sobrescreve evento existente. A comparação de identidade e payload detecta colisão de UUID e responde 409. `TrackingService` converte falhas Mongo/Spring Data em 503 sem confirmar o lote.

Foi utilizada a biblioteca Spring Data diretamente, sem adicionar um starter que configure outra conexão automática. Não existe MongoTransactionManager; `@Transactional` do negócio continua no PostgreSQL. As datas do `TrackingEventDocument` são convertidas explicitamente em `java.util.Date` antes de persistir o documento, inclusive ao usar MongoTemplate. [Configuração Spring Data](https://docs.spring.io/spring-data/mongodb/reference/mongodb/template-config.html).

Os sucessos de OS são capturados após commit e passam pelo spool de disco existente. Mongo indisponível não provoca rollback operacional; diretório persistente é obrigatório na hospedagem. Ainda existe janela de perda entre commit PostgreSQL e gravação do spool: não é uma outbox transacional nem auditoria com garantia de perda zero.

## 9. Arquivos envolvidos

Caminhos relativos à raiz Spring:

| Arquivo | Estado nesta etapa |
|---|---|
| `pom.xml` | Complementado com Spring Data MongoDB; driver existente mantido |
| `src/main/resources/application.properties` | Acrescentado nome da collection |
| `src/main/java/org/apollo/api/tracking/config/MongoTrackingConfig.java` | Client existente + MongoTemplate isolado |
| `src/main/java/org/apollo/api/tracking/repository/TrackingRepository.java` | Persistência convertida para MongoOperations, mantendo idempotência |
| `src/main/java/org/apollo/api/tracking/service/TrackingService.java` | Trata também exceções traduzidas do Spring Data |
| `src/test/java/org/apollo/api/tracking/TrackingRepositoryIntegrationTest.java` | Usa o repository atualizado e collection temporária de teste |
| `src/test/java/org/apollo/api/tracking/TrackingRepositoryTest.java` | Novo: upsert, replay concorrente e identidade |
| `src/test/java/org/apollo/api/tracking/TrackingServiceTest.java` | Acrescentada falha de conexão traduzida pelo Spring Data |
| `APOLLO_MONGODB_SETUP.md` | Este tutorial |

Os DTOs/controller/model/spool e a regra de segurança já existiam do módulo anterior e foram reutilizados, não duplicados. Não houve alteração no aplicativo Android nesta tarefa.

## 10. Variáveis de ambiente

As propriedades customizadas atuais são:

```properties
apollo.tracking.enabled=${TRACKING_ENABLED:false}
apollo.tracking.mongo-uri=${TRACKING_MONGO_URI:${MONGODB_URI:mongodb://localhost:27017}}
apollo.tracking.database=apollo_analytics
apollo.tracking.collection=tracking_events
apollo.tracking.spool-dir=${TRACKING_SPOOL_DIR:./var/tracking-spool}
apollo.tracking.flush-delay-ms=${TRACKING_FLUSH_DELAY_MS:10000}
```

Local, preferir **Run → Edit Configurations → Environment variables** da execução Spring no IntelliJ:

```text
TRACKING_ENABLED=true
MONGODB_URI=URI_NOVA_GUARDADA_LOCALMENTE
TRACKING_SPOOL_DIR=C:/apollo-runtime/tracking-spool
TRACKING_FLUSH_DELAY_MS=10000
```

Em PowerShell 7, é possível ler a URI sem gravá-la no histórico:

```powershell
$env:TRACKING_ENABLED = 'true'
$env:MONGODB_URI = Read-Host 'URI Atlas com senha nova (entrada oculta)' -MaskInput
$env:TRACKING_SPOOL_DIR = 'C:/apollo-runtime/tracking-spool'
.\mvnw.cmd spring-boot:run
```

A configuração agora aceita `MONGODB_URI`, como no seu ambiente. `TRACKING_MONGO_URI` tem prioridade se ambas existirem; evite manter uma URI antiga nessa variável. A variável só vale para o processo iniciado naquele terminal. O app não lê `.env` automaticamente; arquivo `.env` ignorado no Git não implica que o Spring o carregará. Na hospedagem, use o painel de Environment/Secrets do serviço do **backend**, configure as mesmas variáveis e reinicie/reimplante. Configure também os segredos PostgreSQL/JWT já exigidos pelo Apollo. Use volume persistente por instância para o spool. Nunca colocar URI no Android, Git, prints, logs ou argumentos de comando públicos.

## 11. Contrato e consultas

Exemplo de JSON HTTP aceito (identidade e `received_at` não vêm do Android):

```json
{"events":[{"event_id":"d174e279-44a2-4f35-8063-785cbb6bad6e","event_name":"screen_viewed","session_id":"1db94bc7-3c6d-4cd3-9020-c475f24783bb","screen":"HomeActivity","timestamp":"2026-10-23T10:30:00Z","platform":"android","properties":{}}]}
```

`previous_screen` do exemplo conceitual não pertence à allowlist atual. Use `{}` neste teste; só acrescente novas propriedades com alteração coordenada em API, Android e schema. UUIDs reais de usuário vêm do JWT verificado; `tech_102` não é o tipo usado pelo Apollo.

Representação Extended JSON de um documento armazenado:

```json
{
  "event_id":"d174e279-44a2-4f35-8063-785cbb6bad6e",
  "event_name":"screen_viewed",
  "user_id":"e2fcb9c2-9fbc-4d96-9759-1bdcb84d2f61",
  "company_id":10,
  "session_id":"1db94bc7-3c6d-4cd3-9020-c475f24783bb",
  "screen":"HomeActivity",
  "timestamp":{"$date":"2026-10-23T10:30:00Z"},
  "received_at":{"$date":"2026-10-23T10:30:01Z"},
  "platform":"android","source":"android","schema_version":1,"properties":{}
}
```

HTTP não tem tipo Date, então envia uma string ISO UTC. Mongo armazena BSON Date (milissegundos desde epoch); `$date` é sua representação de exportação, não uma string salva no campo. `_id` ObjectId é criado pelo Mongo. `source=api` identifica os sucessos confirmados no servidor; `platform=android` pode existir nesses eventos quando há correlação com o app.

No Compass, abra a aba **Documents** da collection. No campo **Filter**, use JSON e clique em **Find**:

```json
{"event_name": "screen_viewed"}
```

Para localizar um evento específico:

```json
{"event_id": "d174e279-44a2-4f35-8063-785cbb6bad6e"}
```

Para ver os mais recentes, em **Options → Sort**, use:

```json
{"received_at": -1}
```

Para verificar o tipo BSON pela própria consulta, aplique este filtro:

```json
{"timestamp": {"$type": "date"}, "received_at": {"$type": "date"}}
```

Os tipos esperados são `date`. O guia não altera os documentos para esconder tipo incorreto.

## 12. Teste completo

### 12.1 Cluster e conexão

Confirme cluster provisionado no Atlas, usuário e IP autorizados. No Compass, selecione a conexão e abra `apollo_analytics.tracking_events` sem erro. Isso confirma o acesso do Compass, mas **não** o processo Spring.

Para confirmar o Spring, iniciar com as variáveis reais e obter **200 com IDs aceitos no endpoint**, verificando esses IDs no Compass. `/actuator/health` sozinho não comprova essa integração customizada. Compass e Spring podem usar usuários e redes diferentes.

### 12.2 Enviar evento, replay e lote

Use um JWT válido obtido pelo login já existente do Apollo. O token não é a senha Atlas. Em PowerShell 7, com backend em execução:

```powershell
$apolloBase = 'http://localhost:8080'
$apolloJwt = Read-Host 'JWT do Apollo (entrada oculta)' -MaskInput
$apolloHeaders = @{ Authorization = "Bearer $apolloJwt" }
$apolloEventId = [guid]::NewGuid().ToString()
$apolloSessionId = [guid]::NewGuid().ToString()
$apolloTimestamp = [DateTime]::UtcNow.ToString('yyyy-MM-ddTHH:mm:ss.fffZ')
$apolloEvent = @{
  event_id=$apolloEventId; event_name='screen_viewed'; session_id=$apolloSessionId
  screen='HomeActivity'; timestamp=$apolloTimestamp; platform='android'; properties=@{}
}
$apolloBody = @{events=@($apolloEvent)} | ConvertTo-Json -Depth 6
Invoke-RestMethod "$apolloBase/api/tracking/events/batch" -Method Post -Headers $apolloHeaders -ContentType 'application/json' -Body $apolloBody

# Replay EXATO: manter event_id, timestamp, session_id e payload.
Invoke-RestMethod "$apolloBase/api/tracking/events/batch" -Method Post -Headers $apolloHeaders -ContentType 'application/json' -Body $apolloBody

$apolloEvent2 = @{
  event_id=[guid]::NewGuid().ToString(); event_name='screen_viewed'; session_id=$apolloSessionId
  screen='ServiceOrdersActivity'; timestamp=$apolloTimestamp; platform='android'; properties=@{}
}
$apolloBatch = @{events=@($apolloEvent,$apolloEvent2)} | ConvertTo-Json -Depth 6
Invoke-RestMethod "$apolloBase/api/tracking/events/batch" -Method Post -Headers $apolloHeaders -ContentType 'application/json' -Body $apolloBatch
```

No primeiro request, o ID é confirmado após persistência. Replay retorna o mesmo ID sem incrementar contagem. O lote confirma ambos; apenas o novo evento cria documento. No Compass, filtre pelo `event_id` gerado e confira que existe um único documento. Reaplique o filtro depois do replay.

Resultado esperado: `1`. Se reutilizar UUID mudando timestamp/propriedades, isso é colisão e deve retornar 409, não replay legítimo. Sem Bearer deve retornar 401; nome de evento/propriedades inválidos, 400; lote acima de 50 eventos, 400; body maior que 128 KiB, 413.

### 12.3 Falhas e PostgreSQL

Em ambiente de desenvolvimento, configure temporariamente `MONGODB_URI=mongodb://127.0.0.1:1/apollo_analytics` (sem uma `TRACKING_MONGO_URI` antiga sobrepondo) e reinicie. Esse endereço local fechado simula indisponibilidade sem derrubar o cluster ou alterar acesso de outros usuários. Enviar **novo** evento deve retornar 503; não há confirmação. Restaurar URI e reiniciar; reenvio pode persistir.

Enquanto o Mongo estiver indisponível, autentique e consulte um endpoint operacional PostgreSQL já usado pelo app, por exemplo `GET /api/v1/mobile/home`. Testar uma OS de desenvolvimento: iniciar/concluir deve seguir as regras de negócio e não falhar por Mongo; eventos confirmados ficam no spool para envio posterior. Não usar OS reais de produção para testar transições.

No Android existente, autentique, abra Home/OS e verifique os eventos no Atlas. Depois fique offline, gere eventos de navegação em telas disponíveis e reconecte. WorkManager deve reenviar UUIDs já salvos. O celular conversa somente com o endereço do Spring; nunca com o Atlas.

### 12.4 Testes automatizados

```powershell
.\mvnw.cmd '-Dtest=TrackingServiceTest,TrackingBusinessEventsTest,TrackingControllerTest,TrackingRepositoryTest,SecurityConfigTest' test
```

Para teste real Mongo, use outro usuário, com `readWrite`/provisionamento **somente** no banco `apollo_analytics_test`, nunca as credenciais de produção. Ele precisa criar índice e apagar sua collection de teste. A conta mínima da aplicação não tem esses privilégios, por design.

```powershell
$env:TRACKING_TEST_MONGO_URI = Read-Host 'URI do Mongo de TESTE (entrada oculta)' -MaskInput
.\mvnw.cmd '-Dtest=TrackingRepositoryIntegrationTest' test
```

O teste cria collection de nome aleatório `tracking_test_*` exclusivamente em `apollo_analytics_test`, cria índice, testa persistência/replay/collision e apaga apenas aquela collection. Sem URI, o teste fica skipped: não considerar Atlas validado. Os testes Mockito não provam disponibilidade real de rede.

Resultado local: **BUILD SUCCESS**, 33 testes considerados, 32 aprovados, zero falhas/erros e 1 ignorado (`TrackingRepositoryIntegrationTest`, pois a URI de teste não foi configurada). API e testes compilaram com Java 21. Isso não comprova a conexão Atlas ou o fluxo PostgreSQL de ponta a ponta.

## 13. Erros comuns

| Sintoma | Conferir |
|---|---|
| `Cannot resolve symbol mongodb` | Reimportar Maven e conferir download de `spring-data-mongodb` e `mongodb-driver-sync`, sem fixar versões incompatíveis |
| Authentication failed | Usuário/senha do banco, percent-encoding, `authSource=admin`; login Atlas não autentica o driver |
| Timeout/server selection | IP **de saída** do Spring, VPN, rede/firewall, cluster disponível; conferir DNS SRV |
| Unknown host / SRV | Copiar hostname real pelo Connect; testar resolvedor DNS, não inventar domínio pelo nome do cluster |
| TLS/PKIX | JDK/truststore/certificados corporativos; não desligar validação TLS |
| Not authorized / code 13 | Custom role `find/insert/update` na collection exata; conta de app não administra índices |
| Document failed validation / code 121 | Datas string em vez de BSON Date, propriedades fora da allowlist, campos/versionamento divergentes |
| E11000 | UUID replay/colisão ou duplicatas anteriores impedindo índice; não apagar eventos automaticamente |
| API tracking 503 com resto funcionando | `TRACKING_ENABLED`, URI do processo, rede, schema e permissões; usar teste autenticado e não só health |
| Dados no banco errado | `apollo.tracking.database` manda na configuração do template; não usa `spring.data.mongodb.uri` nem `spring.mongodb.uri` |
| Spool cresce | Mongo indisponível, arquivo inválido/conflitante, falta de permissão ou disco; analisar logs sem expor payload/segredo |

Limitações do Free e inatividade podem afetar disponibilidade/desempenho; para produção dimensionar com a equipe, sem promover plano automaticamente. [Limites Free](https://www.mongodb.com/docs/atlas/reference/free-shared-limitations/).

## 14. Preparação Databricks

Futuramente criar `apollo_tracking_reader` em **Database Users**, com papel customizado `find` somente em `apollo_analytics.tracking_events`, ou `read` restrito a `apollo_analytics` se o conector exigir listar metadados. Autorizar na rede os IPs de saída do ambiente de ingestão. Change streams exigirão privilégios adicionais e verificação de suporte do plano, não presumidos aqui.

Bronze guarda documentos brutos; Silver normaliza UTC/esquema e deduplica `event_id`; Gold calcula métricas. `timestamp` descreve a ação, podendo ser antigo por offline. `received_at` e `event_id` ajudam a ordenar lotes, mas o índice **não garante extração sem perdas**: spool pode inserir depois com recebimento antigo, e transações concorrentes chegam fora da ordem do cursor. Usar janela de sobreposição e deduplicação, reconciliação periódica e, quando apropriado, change streams com resume token/CDC. Só aumentar watermark depois de persistir com sucesso o lote de ingestão. Não interpretar dados de intenção do cliente como confirmação de conclusão de OS.

Pendências futuras: credencial somente leitura, egress IPs, política de retenção, volume/atraso máximo esperado, checkpoints, reprocessamento, conector e custos. Nenhum pipeline Databricks foi criado nesta tarefa.

## 15. Branch, commits e checklist

Branch solicitada: `feat/api-event-tracking`. As alterações anteriores locais foram mantidas. Nenhum commit ou push foi feito automaticamente.

Commits sugeridos, selecionando apenas os arquivos relacionados:

1. `feat(tracking): configure isolated mongodb atlas integration` — `pom.xml`, properties, config Mongo, repository e tratamento de falhas do service.
2. `test(tracking): validate mongodb replay and spring data failures` — testes do repository/service e integração.
3. `docs(tracking): add atlas and compass setup guide` — este Markdown com configuração visual de collection, índices e validator.

O model/controller e captura de negócio anteriores ainda não commitados podem entrar em commits próprios do módulo de tracking; não duplicar alterações já commitadas só para reproduzir os nomes sugeridos.

- [ ] Conta, organização, projeto `Apollo` e cluster `apollo-cluster` criados manualmente.
- [ ] Resumo de custo confirmou **Free/M0**.
- [ ] Usuário de app com papel mínimo; provisionador separado e temporário.
- [ ] IP local e egress do backend autorizados, sem liberação global permanente.
- [ ] `apollo_analytics.tracking_events` com schema e índices confirmados.
- [ ] Segredos somente no ambiente do backend; TLS ligado; URI ausente do Android/Git.
- [ ] Spool em volume persistente privado e monitorado.
- [ ] Endpoint autenticado retorna IDs e eles aparecem no Atlas com datas BSON.
- [ ] Replay, lote, falha de rede e PostgreSQL validados.
- [ ] Teste de integração real executado com usuário/banco de teste.
- [ ] Databricks permanece como etapa futura.

### Informações que faltam para a conexão real

Você precisa concluir a criação do cluster/usuário, obter o hostname real pelo Connect e configurar a URI **no seu ambiente**, com o IP correto autorizado. Para execução hospedada, também é necessário informar/configurar o provedor onde o Spring roda e seu egress IP/volume persistente. Para teste automatizado real, configurar `TRACKING_TEST_MONGO_URI` com acesso somente ao banco de testes. Não é necessário compartilhar senha ou token no chat.

# Integrações da Apollo API

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

1. Crie a tabela `sungrow_credential` (está em `db/schema.sql`) no banco que a API usa.
2. Defina as variáveis acima (nunca versione `appkey`/`access key`) e suba a API.
3. O dono da planta autoriza o aplicativo na iSolarCloud; a Sungrow redireciona para o `redirect_uri` com `?code=...`.
4. Um **Gerente** envia o código: `POST /api/v1/integrations/sungrow/authorization` com `{"code": "..."}`.
   `GET /api/v1/integrations/sungrow/status` mostra se está autorizado (os tokens nunca são devolvidos).
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


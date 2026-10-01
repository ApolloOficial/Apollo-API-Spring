# Postman – Apollo API (esquema 2º ano)

1. Importe `apollo-api.postman_collection.json` e `apollo-local.postman_environment.json`; selecione o ambiente **Apollo local**.
2. Suba a API (`./mvnw spring-boot:run`) – por padrão em http://localhost:8080 (ou altere `baseUrl`).
3. Rode a coleção na ordem pelo **Runner**. Cada pasta faz o seu próprio login:

| Pasta | Usuário | Senha |
|---|---|---|
| 0, 1, 5 (decisão) | renata.moraes@ambarenergia.demo (Gerente) | Apollo@2026 |
| 2, 5 (sugestões) | gabriel.rocha@ambarenergia.demo (Operador) | Apollo@2026 |
| 3 | vanessa.mendes@ambarenergia.demo (Técnica) | Apollo@2026 |
| 4 | juliana.ribeiro@ambarenergia.demo (Analista) | Apollo@2026 |

Observações
- Os IDs (filial, string, placa, alerta, OS…) são descobertos pelas próprias listagens.
- `destinationCompanyId` = 2 (Cooperativa Vale do Cerrado, destino das realocações externas); ajuste se o ID no seu banco for outro.
- Regras do banco (triggers/procedures) respondem **400** com a mensagem original; alguns testes aceitam 400 porque dependem do estado dos dados (ex.: alerta que já tem OS aberta).
- Os testes de escrita alteram dados da demonstração (OS, realocações, relato de placa).

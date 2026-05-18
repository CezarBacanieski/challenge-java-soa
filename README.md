# Ford VIN Share API

API REST acadêmica para retenção de clientes pós-venda Ford. O sistema permite cadastrar clientes e veículos, registrar manutenções, monitorar uso da rede oficial e gerar leads proativos de serviço.

## Equipe

| Nome | RM |
|---|---|
| Milton Cezar Bacanieski | RM555206 |
| Victorio Bastelli | RM554723 |
| Vitor Bebiano | RM555026 |
| Lorenzo Mangini | RM554901 |

## Stack

- Java 17
- Spring Boot
- Spring Data JPA
- Hibernate
- PostgreSQL
- Lombok
- SpringDoc OpenAPI
- Maven

## Configuração do Banco

Crie o banco PostgreSQL:

```sql
CREATE DATABASE fordvinshare;
```

Configuração em `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fordvinshare
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
springdoc.swagger-ui.path=/swagger-ui.html
```

## Executar

```bash
./mvnw spring-boot:run
```

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

## Enums

Status de lead:

```text
PENDENTE, CONTATADO, CONVERTIDO, PERDIDO
```

Prioridade de lead:

```text
BAIXA, MEDIA, ALTA
```

## Endpoints

### Clientes

`POST /api/clientes`

```json
{
  "nome": "Maria Silva",
  "email": "maria@email.com",
  "telefone": "11999999999"
}
```

`GET /api/clientes`

Lista todos os clientes.

`GET /api/clientes/{id}`

Busca cliente por ID.

`PUT /api/clientes/{id}`

```json
{
  "nome": "Maria Silva",
  "email": "maria.silva@email.com",
  "telefone": "11888888888"
}
```

`DELETE /api/clientes/{id}`

Remove cliente por ID.

### Veículos

`POST /api/veiculos`

```json
{
  "vin": "9BFZH55L3P8123456",
  "marca": "Ford",
  "modelo": "Ranger",
  "anoFabricacao": 2024,
  "clienteId": 1,
  "utilizaRedeOficial": true
}
```

`GET /api/veiculos`

Lista todos os veículos.

`GET /api/veiculos/{id}`

Busca veículo por ID.

`GET /api/veiculos/cliente/{clienteId}`

Lista veículos vinculados a um cliente.

`PUT /api/veiculos/{id}`

```json
{
  "vin": "9BFZH55L3P8123456",
  "marca": "Ford",
  "modelo": "Ranger Limited",
  "anoFabricacao": 2024,
  "clienteId": 1,
  "utilizaRedeOficial": true
}
```

`DELETE /api/veiculos/{id}`

Remove veículo por ID.

### Manutenções

`POST /api/manutencoes`

```json
{
  "veiculoId": 1,
  "dataServico": "2026-05-18",
  "tipoServico": "Revisão 10.000km",
  "valor": 750.00,
  "realizadaNaRedeOficial": true,
  "observacoes": "Serviço realizado sem pendências"
}
```

Quando `realizadaNaRedeOficial` for `false`, o veículo passa automaticamente para `utilizaRedeOficial = false`.

`GET /api/manutencoes/veiculo/{veiculoId}`

Lista histórico de manutenções de um veículo.

`GET /api/manutencoes`

Lista todas as manutenções.

`DELETE /api/manutencoes/{id}`

Remove manutenção por ID.

### Leads

`POST /api/leads/gerar/{veiculoId}`

```json
{
  "motivoLead": "Garantia expirando",
  "prioridade": "ALTA"
}
```

Gera lead manual com status inicial `PENDENTE`.
O corpo da requisição é opcional. Se omitido, o motivo será `Lead gerado manualmente` e a prioridade será `MEDIA`.

`POST /api/leads/gerar-automatico`

Varre todos os veículos e gera leads pendentes quando:

- não existe manutenção nos últimos 180 dias: motivo `Sem manutenção há mais de 6 meses`, prioridade `ALTA`;
- `utilizaRedeOficial = false`: motivo `Veículo utilizando rede não oficial`, prioridade `MEDIA`.

Não cria novo lead automático se já existir lead `PENDENTE` para o mesmo veículo.

`GET /api/leads`

Lista todos os leads.

`GET /api/leads/status/{status}`

Filtra leads por status. Exemplo:

```text
GET /api/leads/status/PENDENTE
```

`PUT /api/leads/{id}/status`

```json
{
  "status": "CONTATADO"
}
```

Atualiza o status do lead.

### Dashboard

`GET /api/dashboard/resumo`

Retorna:

```json
{
  "totalClientes": 10,
  "totalVeiculos": 15,
  "percentualVeiculosRedeOficial": 86.67,
  "totalLeadsPendentes": 3,
  "totalManutencoesMesAtual": 5
}
```

O VIN Share é calculado por:

```text
(veículos com utilizaRedeOficial = true / total de veículos) * 100
```

O resultado é arredondado para 2 casas decimais.

## Tratamento de Erros

Formato padrão:

```json
{
  "status": 404,
  "erro": "Cliente não encontrado",
  "timestamp": "2026-05-18T10:00:00"
}
```

Validação retorna também a lista de campos inválidos:

```json
{
  "status": 400,
  "erro": "Campos inválidos",
  "timestamp": "2026-05-18T10:00:00",
  "campos": [
    {
      "campo": "email",
      "mensagem": "E-mail inválido"
    }
  ]
}
```

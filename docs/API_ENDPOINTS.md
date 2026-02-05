# 📡 API Endpoints - ZabbixIncidentService

Documentação detalhada de todos os endpoints REST da aplicação.

## 📋 Índice

- [Criar Incidente](#criar-incidente)
- [Listar Incidentes](#listar-incidentes)
- [Buscar por ID](#buscar-por-id)
- [Buscar por Zabbix Event ID](#buscar-por-zabbix-event-id)
- [Atualizar Status](#atualizar-status)
- [Deletar Incidente](#deletar-incidente)
- [Health Check](#health-check)
- [Códigos de Status](#códigos-de-status)
- [Valores de Enum](#valores-de-enum)

---

## ➕ Criar Incidente

Cria um novo incidente no sistema e publica na fila RabbitMQ.

### Requisição

```http
POST /api/incidents
Content-Type: application/json
```

### Parâmetros

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `zabbixEventId` | string | ✅ Sim | ID único do evento no Zabbix |
| `title` | string | ✅ Sim | Título do incidente |
| `description` | string | ❌ Não | Descrição detalhada |
| `severity` | string | ✅ Sim | Nível de severidade (CRITICAL, HIGH, MEDIUM, LOW, INFO) |
| `source` | string | ✅ Sim | Origem do incidente (ex: zabbix) |

### Exemplo de Requisição

```bash
curl -X POST http://localhost:8080/api/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "zabbixEventId": "evt_12345",
    "title": "CPU alta no servidor prod-01",
    "description": "Utilização de CPU acima de 90% por mais de 5 minutos",
    "severity": "CRITICAL",
    "source": "zabbix"
  }'
```

### Response (201 Created)

```json
{
  "status": 201,
  "message": "Incidente criado com sucesso",
  "data": {
    "id": 1,
    "zabbixEventId": "evt_12345",
    "title": "CPU alta no servidor prod-01",
    "description": "Utilização de CPU acima de 90% por mais de 5 minutos",
    "severity": "CRITICAL",
    "status": "OPEN",
    "source": "zabbix",
    "createdAt": "2025-02-04T10:30:45",
    "updatedAt": "2025-02-04T10:30:45"
  },
  "timestamp": "2025-02-04T10:30:45"
}
```

### Response (400 Bad Request - Validação Falhou)

```json
{
  "status": 400,
  "message": "Validação falhou",
  "path": "/api/incidents",
  "timestamp": "2025-02-04T10:30:45",
  "details": {
    "zabbixEventId": "zabbixEventId não pode estar vazio",
    "title": "title não pode estar vazio",
    "severity": "severity não pode ser nulo"
  }
}
```

---

## 📋 Listar Incidentes

Lista todos os incidentes com suporte a paginação e ordenação.

### Requisição

```http
GET /api/incidents?page=0&size=10&sort=createdAt,desc
```

### Parâmetros de Query

| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| `page` | integer | 0 | Número da página (começa em 0) |
| `size` | integer | 20 | Quantidade de registros por página |
| `sort` | string | createdAt,desc | Campo e direção de ordenação |

### Exemplo de Requisição

```bash
curl -X GET "http://localhost:8080/api/incidents?page=0&size=5&sort=createdAt,desc" \
  -H "Content-Type: application/json"
```

### Response (200 OK)

```json
{
  "status": 200,
  "message": "Incidentes listados com sucesso",
  "data": {
    "content": [
      {
        "id": 2,
        "zabbixEventId": "evt_12346",
        "title": "Disco cheio no servidor app-02",
        "description": "Espaço em disco abaixo de 5%",
        "severity": "HIGH",
        "status": "IN_PROGRESS",
        "source": "zabbix",
        "createdAt": "2025-02-04T09:15:30",
        "updatedAt": "2025-02-04T10:00:00"
      },
      {
        "id": 1,
        "zabbixEventId": "evt_12345",
        "title": "CPU alta no servidor prod-01",
        "description": "Utilização de CPU acima de 90% por mais de 5 minutos",
        "severity": "CRITICAL",
        "status": "OPEN",
        "source": "zabbix",
        "createdAt": "2025-02-04T10:30:45",
        "updatedAt": "2025-02-04T10:30:45"
      }
    ],
    "totalElements": 2,
    "totalPages": 1,
    "number": 0,
    "size": 5,
    "numberOfElements": 2,
    "first": true,
    "last": true,
    "empty": false
  },
  "timestamp": "2025-02-04T10:35:20"
}
```

---

## 🔍 Buscar por ID

Busca um incidente específico pelo seu ID interno.

### Requisição

```http
GET /api/incidents/{id}
```

### Parâmetros

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | integer | ID do incidente (path parameter) |

### Exemplo de Requisição

```bash
curl -X GET http://localhost:8080/api/incidents/1 \
  -H "Content-Type: application/json"
```

### Response (200 OK)

```json
{
  "status": 200,
  "message": "Incidente encontrado",
  "data": {
    "id": 1,
    "zabbixEventId": "evt_12345",
    "title": "CPU alta no servidor prod-01",
    "description": "Utilização de CPU acima de 90% por mais de 5 minutos",
    "severity": "CRITICAL",
    "status": "OPEN",
    "source": "zabbix",
    "createdAt": "2025-02-04T10:30:45",
    "updatedAt": "2025-02-04T10:30:45"
  },
  "timestamp": "2025-02-04T10:35:20"
}
```

### Response (404 Not Found)

```json
{
  "status": 404,
  "message": "Incidente não encontrado",
  "data": null,
  "timestamp": "2025-02-04T10:35:20"
}
```

---

## 🔗 Buscar por Zabbix Event ID

Busca um incidente pelo seu ID de evento no Zabbix.

### Requisição

```http
GET /api/incidents/zabbix/{zabbixEventId}
```

### Parâmetros

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `zabbixEventId` | string | ID do evento no Zabbix (path parameter) |

### Exemplo de Requisição

```bash
curl -X GET http://localhost:8080/api/incidents/zabbix/evt_12345 \
  -H "Content-Type: application/json"
```

### Response (200 OK)

```json
{
  "status": 200,
  "message": "Incidente encontrado",
  "data": {
    "id": 1,
    "zabbixEventId": "evt_12345",
    "title": "CPU alta no servidor prod-01",
    "description": "Utilização de CPU acima de 90% por mais de 5 minutos",
    "severity": "CRITICAL",
    "status": "OPEN",
    "source": "zabbix",
    "createdAt": "2025-02-04T10:30:45",
    "updatedAt": "2025-02-04T10:30:45"
  },
  "timestamp": "2025-02-04T10:35:20"
}
```

### Response (404 Not Found)

```json
{
  "status": 404,
  "message": "Incidente não encontrado",
  "data": null,
  "timestamp": "2025-02-04T10:35:20"
}
```

---

## 🔄 Atualizar Status

Atualiza o status de um incidente existente.

### Requisição

```http
PUT /api/incidents/{id}/status
Content-Type: application/json
```

### Parâmetros

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `id` | integer | ✅ Sim | ID do incidente (path parameter) |
| `status` | string | ✅ Sim | Novo status (OPEN, IN_PROGRESS, RESOLVED, CLOSED) |

### Exemplo de Requisição

```bash
curl -X PUT http://localhost:8080/api/incidents/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "RESOLVED"
  }'
```

### Response (200 OK)

```json
{
  "status": 200,
  "message": "Status do incidente atualizado com sucesso",
  "data": {
    "id": 1,
    "zabbixEventId": "evt_12345",
    "title": "CPU alta no servidor prod-01",
    "description": "Utilização de CPU acima de 90% por mais de 5 minutos",
    "severity": "CRITICAL",
    "status": "RESOLVED",
    "source": "zabbix",
    "createdAt": "2025-02-04T10:30:45",
    "updatedAt": "2025-02-04T10:45:30"
  },
  "timestamp": "2025-02-04T10:45:30"
}
```

### Response (400 Bad Request)

```json
{
  "status": 400,
  "message": "Validação falhou",
  "path": "/api/incidents/1/status",
  "timestamp": "2025-02-04T10:45:30",
  "details": {
    "status": "status não pode ser nulo"
  }
}
```

---

## 🗑️ Deletar Incidente

Remove um incidente do sistema.

### Requisição

```http
DELETE /api/incidents/{id}
```

### Parâmetros

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | integer | ID do incidente (path parameter) |

### Exemplo de Requisição

```bash
curl -X DELETE http://localhost:8080/api/incidents/1
```

### Response (204 No Content)

```
(Sem corpo na resposta)
```

### Response Alternativo com JSON (204 No Content)

```json
{
  "status": 204,
  "message": "Incidente deletado com sucesso",
  "data": null,
  "timestamp": "2025-02-04T10:50:00"
}
```

### Response (404 Not Found)

```json
{
  "status": 404,
  "message": "Incidente não encontrado",
  "data": null,
  "timestamp": "2025-02-04T10:50:00"
}
```

---

## ❤️ Health Check

Verifica se a aplicação está operacional.

### Requisição

```http
GET /api/health
```

### Exemplo de Requisição

```bash
curl -X GET http://localhost:8080/api/health
```

### Response (200 OK)

```json
{
  "status": 200,
  "message": "Serviço está operacional",
  "data": {
    "status": "UP",
    "service": "ZabbixIncidentService",
    "timestamp": "2025-02-04T10:55:15"
  },
  "timestamp": "2025-02-04T10:55:15"
}
```

---

## 📊 Códigos de Status

| Código | Significado | Descrição |
|--------|------------|-----------|
| **200** | OK | Requisição bem-sucedida |
| **201** | Created | Recurso criado com sucesso |
| **204** | No Content | Requisição bem-sucedida, sem conteúdo |
| **400** | Bad Request | Dados inválidos ou incompletos |
| **404** | Not Found | Recurso não encontrado |
| **500** | Internal Server Error | Erro no servidor |

---

## 🔢 Valores de Enum

### SeverityLevel (Severidade)

```
CRITICAL  - Crítico, requer ação imediata
HIGH      - Alto, requer atenção urgente
MEDIUM    - Médio, requer atenção
LOW       - Baixo, informativo
INFO      - Informativo, apenas notificação
```

### IncidentStatus (Status)

```
OPEN        - Incidente recém-criado, não iniciado
IN_PROGRESS - Incidente sendo tratado
RESOLVED    - Incidente resolvido, aguardando fechamento
CLOSED      - Incidente finalizado e fechado
```

---

## 🧪 Testar com Postman

Você pode importar esta coleção no Postman:

1. Abra o Postman
2. Clique em "Import"
3. Selecione "Raw text"
4. Cole o JSON abaixo:

```json
{
  "info": {
    "name": "ZabbixIncidentService",
    "version": "1.0.0"
  },
  "item": [
    {
      "name": "Create Incident",
      "request": {
        "method": "POST",
        "url": "{{base_url}}/api/incidents",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"zabbixEventId\":\"evt_12345\",\"title\":\"CPU alta\",\"severity\":\"CRITICAL\",\"source\":\"zabbix\"}"
        }
      }
    },
    {
      "name": "List Incidents",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/incidents?page=0&size=10"
      }
    },
    {
      "name": "Get Incident by ID",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/incidents/1"
      }
    },
    {
      "name": "Update Status",
      "request": {
        "method": "PUT",
        "url": "{{base_url}}/api/incidents/1/status",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"status\":\"RESOLVED\"}"
        }
      }
    },
    {
      "name": "Delete Incident",
      "request": {
        "method": "DELETE",
        "url": "{{base_url}}/api/incidents/1"
      }
    },
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/health"
      }
    }
  ]
}
```

**Nota:** Defina a variável `base_url` como `http://localhost:8080`

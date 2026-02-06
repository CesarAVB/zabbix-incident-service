# 🎛️ Controllers - ZabbixIncidentService

Documentação detalhada de todos os controllers da aplicação, explicando endpoints, validações e tratamento de respostas.

## 📋 Índice

- [HealthController](#healthcontroller)
- [GlobalExceptionHandler](#globalexceptionhandler)
- [IncidentController (Planejado)](#incidentcontroller-planejado)

---

## 🏥 HealthController

**Localização:** `api/controller/HealthController.java`

**Responsabilidades:**
- Fornecer endpoint de health check
- Verificar se a aplicação está operacional
- Retornar informações básicas do serviço

**Endpoint:** `GET /health`

### Método `checkHealth()`

**O que faz:**
- Cria um mapa com informações básicas
- Retorna status "UP" se aplicação está rodando
- Inclui timestamp atual

**Resposta de sucesso (200 OK):**
```json
{
  "status": 200,
  "message": "Serviço está operacional",
  "data": {
    "status": "UP",
    "service": "ZabbixIncidentService",
    "timestamp": "2025-02-04T10:30:45"
  },
  "timestamp": "2025-02-04T10:30:45"
}
```

**Uso típico:**
- Monitoramento de infraestrutura
- Load balancers verificam se instância está saudável
- Ferramentas de observabilidade (Prometheus, etc.)

**Exemplo de chamada:**
```bash
curl -X GET http://localhost:8080/health
```

---

## 🚨 GlobalExceptionHandler

**Localização:** `api/handler/GlobalExceptionHandler.java`

**Responsabilidades:**
- Centralizar tratamento de exceções
- Padronizar respostas de erro
- Fornecer mensagens claras para debugging

**Anotação:** `@RestControllerAdvice`
- Aplica-se globalmente a todos os controllers
- Intercepta exceções não tratadas

### Tratamento de Validação (`MethodArgumentNotValidException`)

**Quando ocorre:**
- Campos obrigatórios não preenchidos
- Dados inválidos nos DTOs (@Valid falha)
- Tipos incorretos

**O que faz:**
1. Extrai todos os erros de validação
2. Mapeia campo → mensagem de erro
3. Retorna 400 Bad Request com detalhes

**Exemplo de resposta:**
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

### Tratamento de RuntimeException

**Quando ocorre:**
- Erros de negócio (ex: "Incidente não encontrado")
- Falhas de integração
- Problemas de dados

**O que faz:**
- Retorna 500 Internal Server Error
- Inclui mensagem da exception
- Loga para debugging

### Tratamento Genérico (Exception)

**Quando ocorre:**
- Qualquer exception não tratada especificamente
- Erros inesperados do sistema

**O que faz:**
- Retorna 500 Internal Server Error
- Mensagem genérica "Erro interno do servidor"
- Protege informações sensíveis

---

## 📝 IncidentController (Planejado)

**Status:** Não implementado ainda

**Localização planejada:** `api/controller/IncidentController.java`

**Endpoints planejados:**
- `POST /api/incidents` - Criar incidente
- `GET /api/incidents` - Listar incidentes (paginação)
- `GET /api/incidents/{id}` - Buscar por ID
- `GET /api/incidents/zabbix/{eventId}` - Buscar por Zabbix Event ID
- `PUT /api/incidents/{id}/status` - Atualizar status
- `DELETE /api/incidents/{id}` - Deletar incidente

**Funcionalidades:**
- Validação de entrada com Bean Validation
- Tratamento de paginação e ordenação
- Respostas padronizadas (SuccessResponse)
- Integração com IncidentService

**Exemplo de implementação futura:**
```java
@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    @PostMapping
    public ResponseEntity<SuccessResponse<IncidentResponse>> createIncident(
            @Valid @RequestBody CreateIncidentRequest request) {
        IncidentResponse incident = incidentService.createIncident(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new SuccessResponse<>(HttpStatus.CREATED.value(),
                "Incidente criado com sucesso", incident, LocalDateTime.now()));
    }
}
```

---

## 🔄 Fluxo de Tratamento de Erros

### Validação Falha
```
Requisição com dados inválidos
    ↓
@Valid falha no DTO
    ↓
MethodArgumentNotValidException lançada
    ↓
GlobalExceptionHandler.handleValidationExceptions()
    ↓
Retorna 400 com detalhes dos erros
```

### Erro de Negócio
```
Serviço lança RuntimeException
    ↓
Exception propagada para controller
    ↓
GlobalExceptionHandler.handleRuntimeException()
    ↓
Retorna 500 com mensagem do erro
```

### Erro Inesperado
```
Exception genérica lançada
    ↓
GlobalExceptionHandler.handleGeneralException()
    ↓
Retorna 500 com mensagem genérica
```

---

## 📋 Padrões de Resposta

### SuccessResponse
```json
{
  "status": 200,
  "message": "Operação realizada com sucesso",
  "data": { /* dados da resposta */ },
  "timestamp": "2025-02-04T10:30:45"
}
```

### ErrorResponse
```json
{
  "status": 400,
  "message": "Mensagem de erro",
  "path": "/api/endpoint",
  "timestamp": "2025-02-04T10:30:45",
  "details": { /* detalhes adicionais */ }
}
```

---

## 🔧 Configurações Relacionadas

**Nenhuma configuração específica necessária**

**Dependências:**
- Spring Boot Validation (para @Valid)
- DTOs de response (SuccessResponse, ErrorResponse)</content>
<parameter name="filePath">D:\Documentos\PROGRAMAÇÃO\PROJETOS\BACKEND\zabbix-incident-service\docs\CONTROLLERS.md
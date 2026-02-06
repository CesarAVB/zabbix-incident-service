# 📋 Serviços - ZabbixIncidentService

Documentação detalhada de todos os serviços da aplicação, explicando suas responsabilidades, métodos e funcionamento interno.

## 📋 Índice

- [IncidentService](#incidentservice)
- [WebSocketNotificationService](#websocketnotificationservice)

---

## 🔧 IncidentService

**Localização:** `application/service/IncidentService.java`

**Responsabilidades:**
- Gerenciar operações CRUD de incidentes
- Publicar mensagens na fila RabbitMQ
- Converter dados entre DTOs e entidades
- Validar regras de negócio

**Dependências:**
- `IncidentRepository` - Acesso aos dados
- `IncidentMapper` - Conversão entre objetos
- `RabbitTemplate` - Publicação de mensagens

### Métodos Principais

#### `createIncident(CreateIncidentRequest request)`

**O que faz:**
1. Converte o DTO de request para entidade usando o mapper
2. Salva o incidente no banco de dados
3. Publica o incidente na fila RabbitMQ para processamento assíncrono
4. Retorna o incidente criado convertido para response

**Fluxo detalhado:**
```
Recebe CreateIncidentRequest
    ↓
incidentMapper.toEntity(request) → Incident
    ↓
incidentRepository.save(incident) → Incident salvo
    ↓
rabbitTemplate.convertAndSend() → Publica na fila
    ↓
incidentMapper.toResponse() → IncidentResponse
```

**Exemplo de uso:**
```java
CreateIncidentRequest request = new CreateIncidentRequest(
    "evt_123", "CPU alta", "CPU > 90%", "CRITICAL", "zabbix"
);
IncidentResponse response = incidentService.createIncident(request);
```

#### `getIncidentById(Long id)`

**O que faz:**
- Busca um incidente específico pelo ID interno
- Retorna Optional vazio se não encontrar
- Operação somente leitura (readOnly = true)

**Fluxo:**
```
Recebe ID
    ↓
incidentRepository.findById(id)
    ↓
Se encontrado: incidentMapper.toResponse()
Se não encontrado: Optional.empty()
```

#### `getIncidentByZabbixEventId(String zabbixEventId)`

**O que faz:**
- Busca incidente pelo ID único do evento Zabbix
- Útil para verificar se um evento já foi processado
- Retorna Optional para tratamento elegante de "não encontrado"

#### `getAllIncidents(Pageable pageable)`

**O que faz:**
- Lista todos os incidentes com paginação
- Suporte a ordenação (sort)
- Converte cada entidade para DTO de response

**Parâmetros de paginação:**
- `page` - Número da página (0-based)
- `size` - Quantidade por página
- `sort` - Campo e direção (ex: "createdAt,desc")

#### `updateIncidentStatus(Long id, UpdateIncidentStatusRequest request)`

**O que faz:**
1. Busca o incidente existente
2. Valida se existe (lança RuntimeException se não)
3. Converte string do status para enum
4. Atualiza o status
5. Salva no banco
6. Publica atualização na fila RabbitMQ

**Validações:**
- Incidente deve existir
- Status deve ser válido (enum IncidentStatus)

#### `deleteIncident(Long id)`

**O que faz:**
1. Verifica se o incidente existe
2. Se não existe, lança RuntimeException
3. Se existe, remove do banco

---

## 🌐 WebSocketNotificationService

**Localização:** `application/service/WebSocketNotificationService.java`

**Responsabilidades:**
- Enviar notificações em tempo real via WebSocket
- Comunicar mudanças de incidentes para clientes conectados
- Usar STOMP protocol para messaging

**Dependências:**
- `SimpMessagingTemplate` - Template para enviar mensagens
- `IncidentMapper` - Converter incidentes para response

### Métodos Principais

#### `notifyIncidentCreated(Incident incident)`

**O que faz:**
- Converte incidente para DTO de response
- Envia para tópico "/topic/incidents"
- Todos os clientes inscritos recebem a notificação

**Fluxo:**
```
Recebe Incident
    ↓
incidentMapper.toResponse() → IncidentResponse
    ↓
messagingTemplate.convertAndSend("/topic/incidents", response)
```

#### `notifyIncidentUpdated(Incident incident)`

**O que faz:**
- Mesmo fluxo do `notifyIncidentCreated`
- Usado quando incidente é atualizado (status alterado)

#### `notifyIncidentDeleted(Long incidentId)`

**O que faz:**
- Envia apenas o ID do incidente deletado
- Usa tópico específico "/topic/incidents/deleted"
- Cliente pode remover da lista local

**Diferença dos outros métodos:**
- Não precisa converter entidade completa
- Só envia o ID para otimização

---

## 🔄 Integração entre Serviços

### Fluxo Completo de Criação de Incidente

```
Zabbix envia evento
    ↓
IncidentController.createIncident()
    ↓
IncidentService.createIncident()
    ↓
Salva no banco
Publica na fila RabbitMQ
    ↓
IncidentListener.handleIncidentMessage()
    ↓
WebSocketNotificationService.notifyIncidentCreated()
    ↓
Clientes WebSocket recebem notificação
```

### Fluxo de Atualização

```
Requisição PUT /api/incidents/{id}/status
    ↓
IncidentService.updateIncidentStatus()
    ↓
Atualiza no banco
Publica na fila RabbitMQ
    ↓
IncidentListener.processa (se configurado)
    ↓
WebSocketNotificationService.notifyIncidentUpdated()
```

---

## ⚠️ Tratamento de Erros

**Erros comuns:**
- `RuntimeException("Incidente não encontrado com ID: " + id)` - Quando ID não existe
- Erro de conversão de enum - Status inválido

**Boas práticas:**
- Sempre validar existência antes de operações
- Usar Optional para buscas
- Logar operações importantes

---

## 🔧 Configurações Relacionadas

**application.properties:**
```properties
# RabbitMQ
app.rabbitmq.exchange.incident=zabbix.incident.exchange
app.rabbitmq.queue.incident=zabbix.incident.queue
app.rabbitmq.routing-key.incident=incident.created

# WebSocket
app.websocket.allowed-origins=http://localhost:4200
app.websocket.endpoint=/ws/incidents
app.websocket.broker-prefix=/topic
```</content>
<parameter name="filePath">D:\Documentos\PROGRAMAÇÃO\PROJETOS\BACKEND\zabbix-incident-service\docs\SERVICES.md
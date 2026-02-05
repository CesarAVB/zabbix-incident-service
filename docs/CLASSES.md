# 📚 Classes do Projeto - ZabbixIncidentService

Explicação simples e didática de cada classe do projeto, sua função e como ela funciona.

## 📋 Índice

- [Entidades](#entidades)
- [DTOs (Request)](#dtos-request)
- [DTOs (Response)](#dtos-response)
- [Repository](#repository)
- [Mapper](#mapper)
- [Services](#services)
- [Listeners](#listeners)
- [Controllers](#controllers)
- [Exception Handlers](#exception-handlers)
- [Configurations](#configurations)

---

## 🏛️ Entidades

### Incident.java

**Função:** Representa um incidente no banco de dados.

**O que faz:**
- Define como um incidente é armazenado no MySQL
- Controla quais informações um incidente tem
- Automaticamente adiciona datas de criação e atualização

**Campos principais:**
- `id` - Identificador único do incidente
- `zabbixEventId` - ID do evento vindo do Zabbix
- `title` - Título do incidente
- `description` - Descrição detalhada
- `severity` - Nível de severidade (CRITICAL, HIGH, etc)
- `status` - Estado atual (OPEN, IN_PROGRESS, etc)
- `source` - De onde veio (ex: zabbix)
- `createdAt` - Data de criação (automática)
- `updatedAt` - Data de última atualização (automática)

**Enums internos:**
- `SeverityLevel` - Define níveis de severidade
- `IncidentStatus` - Define estados possíveis do incidente

**Exemplo:**
```java
Incident incident = Incident.builder()
    .zabbixEventId("evt_123")
    .title("CPU alta")
    .severity(SeverityLevel.CRITICAL)
    .status(IncidentStatus.OPEN)
    .source("zabbix")
    .build();
```

---

## 📨 DTOs (Request)

Os DTOs de request recebem dados do frontend/Zabbix e validam se estão corretos.

### CreateIncidentRequest.java

**Função:** Receber dados do Zabbix e validar antes de criar um incidente.

**O que faz:**
- Valida se os campos obrigatórios foram preenchidos
- Converte dados JSON em um objeto Java
- É imutável (não pode ser alterado após criado)

**Campos:**
- `zabbixEventId` - ID único do evento no Zabbix (obrigatório)
- `title` - Título do incidente (obrigatório)
- `description` - Descrição (opcional)
- `severity` - Nível de severidade (obrigatório)
- `source` - Origem do incidente (obrigatório)

**Exemplo de uso:**
```java
// Recebido do Zabbix como JSON
{
  "zabbixEventId": "evt_12345",
  "title": "CPU alta",
  "severity": "CRITICAL",
  "source": "zabbix"
}

// Convertido para este objeto
CreateIncidentRequest request = new CreateIncidentRequest(
    "evt_12345",
    "CPU alta",
    null,
    "CRITICAL",
    "zabbix"
);
```

### UpdateIncidentStatusRequest.java

**Função:** Receber novo status de um incidente e validar.

**O que faz:**
- Valida se o novo status é válido
- É simples, pois apenas atualiza o status

**Campos:**
- `status` - Novo status (obrigatório)

**Exemplo:**
```java
// Recebido como JSON
{
  "status": "RESOLVED"
}

// Convertido para este objeto
UpdateIncidentStatusRequest request = new UpdateIncidentStatusRequest("RESOLVED");
```

---

## 📤 DTOs (Response)

Os DTOs de response enviam dados para o frontend no formato JSON.

### IncidentResponse.java

**Função:** Enviar dados do incidente para o frontend.

**O que faz:**
- Converte a entidade Incident em um formato seguro para enviar
- Oculta dados sensíveis que não devem ser enviados
- É imutável e apenas leitura

**Campos:**
- `id` - ID do incidente
- `zabbixEventId` - ID do evento no Zabbix
- `title` - Título
- `description` - Descrição
- `severity` - Severidade
- `status` - Status atual
- `source` - Origem
- `createdAt` - Data de criação
- `updatedAt` - Data de atualização

**Exemplo:**
```java
IncidentResponse response = new IncidentResponse(
    1L,
    "evt_12345",
    "CPU alta",
    "CPU acima de 90%",
    "CRITICAL",
    "OPEN",
    "zabbix",
    LocalDateTime.now(),
    LocalDateTime.now()
);
```

### ErrorResponse.java

**Função:** Enviar erros para o frontend de forma padronizada.

**O que faz:**
- Padroniza como os erros são retornados
- Inclui detalhes do erro para o frontend tratar
- Facilita debug e UX melhor no frontend

**Campos:**
- `status` - Código HTTP (400, 404, 500, etc)
- `message` - Mensagem de erro em português
- `path` - Qual endpoint gerou o erro
- `timestamp` - Quando ocorreu
- `details` - Detalhes adicionais (ex: campos inválidos)

**Exemplo:**
```json
{
  "status": 400,
  "message": "Validação falhou",
  "path": "/api/incidents",
  "timestamp": "2025-02-04T10:30:45",
  "details": {
    "title": "title não pode estar vazio"
  }
}
```

### SuccessResponse.java

**Função:** Enviar respostas bem-sucedidas de forma padronizada.

**O que faz:**
- Padroniza como retornar dados com sucesso
- Inclui metadados úteis como status e timestamp
- Facilita o frontend saber se tudo correu bem

**Campos:**
- `status` - Código HTTP (200, 201, etc)
- `message` - Mensagem em português
- `data` - Dados reais (pode ser um objeto, lista, etc)
- `timestamp` - Quando foi gerado

**Exemplo:**
```json
{
  "status": 201,
  "message": "Incidente criado com sucesso",
  "data": {
    "id": 1,
    "title": "CPU alta"
  },
  "timestamp": "2025-02-04T10:30:45"
}
```

---

## 💾 Repository

### IncidentRepository.java

**Função:** Acessar e modificar incidentes no banco de dados.

**O que faz:**
- Herda de `JpaRepository` que já tem métodos prontos
- `findAll()` - Busca todos os incidentes
- `findById()` - Busca um incidente por ID
- `save()` - Salva um incidente novo ou atualiza
- `delete()` - Deleta um incidente
- `findByZabbixEventId()` - Busca por ID do Zabbix (método customizado)

**Métodos custom:**
- `findByZabbixEventId(String zabbixEventId)` - Busca único incidente pelo ID do Zabbix

**Exemplo de uso:**
```java
// Buscar por ID
Optional<Incident> incident = repository.findById(1L);

// Buscar por Zabbix Event ID
Optional<Incident> incident = repository.findByZabbixEventId("evt_12345");

// Salvar novo
Incident saved = repository.save(incident);

// Deletar
repository.deleteById(1L);
```

---

## 🗺️ Mapper

### IncidentMapper.java

**Função:** Converter entre Incident (entidade) e DTOs.

**O que faz:**
- Usa MapStruct para gerar código de mapeamento automaticamente
- `toEntity()` - Converte CreateIncidentRequest em Incident
- `toResponse()` - Converte Incident em IncidentResponse
- Evita código manual repetitivo

**Por que usar:**
- Evita copiar/colar manualmente valores
- Menos chance de erro
- Mais fácil manter

**Exemplo:**
```java
// Sem o mapper (manual)
Incident incident = new Incident();
incident.setTitle(request.title());
incident.setDescription(request.description());
// ... copiar vários campos

// Com o mapper (automático)
Incident incident = mapper.toEntity(request);
```

---

## 🧠 Services

Serviços contêm a lógica de negócio. Eles usam repositories, mappers e outras dependências.

### IncidentService.java

**Função:** Gerenciar a lógica completa de incidentes.

**O que faz:**
- `createIncident()` - Cria novo incidente e publica na fila RabbitMQ
- `getIncidentById()` - Busca um incidente por ID
- `getIncidentByZabbixEventId()` - Busca por ID do Zabbix
- `getAllIncidents()` - Lista com paginação
- `updateIncidentStatus()` - Atualiza status
- `deleteIncident()` - Remove incidente

**Fluxo de createIncident:**
1. Recebe `CreateIncidentRequest`
2. Converte para `Incident` usando mapper
3. Salva no banco de dados
4. **Publica na fila RabbitMQ** (para notificar WebSocket depois)
5. Converte para `IncidentResponse` e retorna

**Exemplo:**
```java
// Service
public IncidentResponse createIncident(CreateIncidentRequest request) {
    // 1. Converte DTO para Entidade
    Incident incident = mapper.toEntity(request);
    
    // 2. Salva no banco
    Incident saved = repository.save(incident);
    
    // 3. Publica na fila (RabbitMQ)
    rabbitTemplate.convertAndSend(exchange, routingKey, saved);
    
    // 4. Retorna como DTO
    return mapper.toResponse(saved);
}
```

### WebSocketNotificationService.java

**Função:** Enviar notificações para o frontend via WebSocket.

**O que faz:**
- `notifyIncidentCreated()` - Notifica novo incidente
- `notifyIncidentUpdated()` - Notifica atualização
- `notifyIncidentDeleted()` - Notifica deleção

**Como funciona:**
- Usa `SimpMessagingTemplate` para enviar mensagens
- Envia para tópicos específicos (`/topic/incidents`)
- O frontend que estiver inscrito recebe a mensagem em tempo real

**Exemplo:**
```java
// No service
public void notifyIncidentCreated(Incident incident) {
    // Converte para DTO
    IncidentResponse response = mapper.toResponse(incident);
    
    // Envia para todos os clientes inscritos em /topic/incidents
    messagingTemplate.convertAndSend("/topic/incidents", response);
}

// No frontend (JavaScript)
stompClient.subscribe('/topic/incidents', (message) => {
    const incident = JSON.parse(message.body);
    console.log('Novo incidente:', incident);
});
```

---

## 📨 Listeners (RabbitMQ)

### IncidentListener.java

**Função:** Ouvir mensagens da fila RabbitMQ e processar.

**O que faz:**
- `handleIncidentMessage()` - Recebe mensagem da fila e notifica WebSocket

**Como funciona:**
```
IncidentService publica na fila RabbitMQ
            ↓
      RabbitMQ armazena
            ↓
IncidentListener consome
            ↓
WebSocketNotificationService envia para frontend
```

**Fluxo completo:**
1. Zabbix envia POST com novo incidente
2. `IncidentController` recebe
3. `IncidentService.createIncident()` salva no banco E publica na fila
4. `IncidentListener` consome da fila
5. `WebSocketNotificationService` envia para frontend
6. Frontend recebe em tempo real via WebSocket

**Exemplo:**
```java
@RabbitListener(queues = "zabbix.incident.queue")
public void handleIncidentMessage(Incident incident) {
    // Assim que uma mensagem chegar na fila
    webSocketService.notifyIncidentCreated(incident);
}
```

---

## 🎮 Controllers

Controllers recebem requisições HTTP e coordenam com services.

### IncidentController.java

**Função:** Gerenciar requisições HTTP para incidentes.

**Endpoints:**
- `POST /api/incidents` - Criar
- `GET /api/incidents` - Listar
- `GET /api/incidents/{id}` - Buscar por ID
- `GET /api/incidents/zabbix/{zabbixEventId}` - Buscar por Zabbix
- `PUT /api/incidents/{id}/status` - Atualizar status
- `DELETE /api/incidents/{id}` - Deletar

**Como funciona cada endpoint:**

```java
@PostMapping
public ResponseEntity<SuccessResponse<IncidentResponse>> createIncident(
        @Valid @RequestBody CreateIncidentRequest request) {
    // 1. Service cria o incidente
    IncidentResponse response = service.createIncident(request);
    
    // 2. Retorna 201 Created com os dados
    return ResponseEntity.status(HttpStatus.CREATED).body(
        new SuccessResponse<>(201, "Sucesso", response, LocalDateTime.now())
    );
}
```

**Validações:**
- `@Valid` valida o DTO automaticamente
- Se inválido, retorna 400 com detalhes

### HealthController.java

**Função:** Verificar se a aplicação está rodando.

**Endpoint:**
- `GET /api/health` - Retorna status UP

**Exemplo:**
```json
{
  "status": 200,
  "message": "Serviço está operacional",
  "data": {
    "status": "UP",
    "service": "ZabbixIncidentService"
  }
}
```

---

## ⚠️ Exception Handlers

### GlobalExceptionHandler.java

**Função:** Capturar erros e retornar respostas padronizadas.

**O que faz:**
- `@ExceptionHandler(MethodArgumentNotValidException.class)` - Erros de validação (400)
- `@ExceptionHandler(RuntimeException.class)` - Erros genéricos (500)
- `@ExceptionHandler(Exception.class)` - Qualquer erro não previsto (500)

**Exemplo:**
```java
// Se um campo obrigatório estiver vazio:
{
  "status": 400,
  "message": "Validação falhou",
  "details": {
    "title": "title não pode estar vazio"
  }
}

// Se um incidente não existir:
{
  "status": 404,
  "message": "Incidente não encontrado"
}

// Se houver erro interno:
{
  "status": 500,
  "message": "Erro interno do servidor"
}
```

---

## ⚙️ Configurations

Configurações iniciais da aplicação.

### WebSocketConfig.java

**Função:** Configurar WebSocket para comunicação em tempo real.

**O que faz:**
- `configureMessageBroker()` - Define como as mensagens são roteadas
  - `/topic` - Para broadcast (todos recebem)
  - `/app` - Para handlers da aplicação
- `registerStompEndpoints()` - Define a URL do WebSocket
  - `/ws/incidents` - Endpoint que o frontend se conecta

**Exemplo (Frontend):**
```javascript
const socket = new SockJS('http://localhost:8080/ws/incidents');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
    stompClient.subscribe('/topic/incidents', (msg) => {
        const incident = JSON.parse(msg.body);
        console.log('Novo incidente:', incident);
    });
});
```

### RabbitMQConfig.java

**Função:** Configurar filas e exchanges do RabbitMQ.

**O que faz:**
- `incidentExchange()` - Cria o exchange (hub de mensagens)
- `incidentQueue()` - Cria a fila (armazena mensagens)
- `incidentBinding()` - Conecta fila ao exchange

**Fluxo:**
```
Mensagem → Exchange → Fila → Listener
```

**Propriedades (application.properties):**
```properties
app.rabbitmq.exchange.incident=zabbix.incident.exchange
app.rabbitmq.queue.incident=zabbix.incident.queue
app.rabbitmq.routing-key.incident=incident.created
```

### CorsConfig.java

**Função:** Permitir requisições do frontend (Angular).

**O que faz:**
- Permite requisições de `http://localhost:4200` (Angular)
- Permite GET, POST, PUT, DELETE
- Permite credenciais (cookies, tokens)

**Sem isso:** Frontend não consegue acessar a API.

### ApplicationConfig.java

**Função:** Configurações gerais da aplicação.

**O que faz:**
- `@EnableAsync` - Permite métodos assíncronos

---

## 📊 Fluxo Completo - Do Zabbix ao Frontend

```
1. ZABBIX envia POST /api/incidents
   ↓
2. IncidentController recebe e valida
   ↓
3. IncidentService.createIncident() é chamado
   ├─ Mapper converte DTO → Entidade
   ├─ Repository salva no MySQL
   └─ RabbitTemplate publica na fila
   ↓
4. RabbitMQ recebe a mensagem
   ↓
5. IncidentListener consome a mensagem
   ↓
6. WebSocketNotificationService envia para /topic/incidents
   ↓
7. FRONTEND recebe via WebSocket (em tempo real!)
   ↓
8. IncidentController retorna SuccessResponse ao Zabbix
```

---

## 🎓 Resumo - O que cada classe faz

| Classe | Tipo | Função |
|--------|------|--------|
| `Incident` | Entity | Representa um incidente no BD |
| `CreateIncidentRequest` | DTO | Recebe dados do Zabbix |
| `UpdateIncidentStatusRequest` | DTO | Recebe novo status |
| `IncidentResponse` | DTO | Envia dados para frontend |
| `ErrorResponse` | DTO | Envia erros |
| `SuccessResponse` | DTO | Envia sucessos |
| `IncidentRepository` | Repository | Acessa BD |
| `IncidentMapper` | Mapper | Converte DTO ↔ Entity |
| `IncidentService` | Service | Lógica de incidentes |
| `WebSocketNotificationService` | Service | Notificações via WebSocket |
| `IncidentListener` | Listener | Consome RabbitMQ |
| `IncidentController` | Controller | Gerencia requisições HTTP |
| `HealthController` | Controller | Health check |
| `GlobalExceptionHandler` | Handler | Trata erros |
| `WebSocketConfig` | Config | Configura WebSocket |
| `RabbitMQConfig` | Config | Configura RabbitMQ |
| `CorsConfig` | Config | Configura CORS |
| `ApplicationConfig` | Config | Configurações gerais |


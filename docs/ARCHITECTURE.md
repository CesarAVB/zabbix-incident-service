# 🏗️ Arquitetura - ZabbixIncidentService

Documentação técnica detalhada sobre a arquitetura do projeto.

## 📋 Índice

- [Padrão Arquitetural](#padrão-arquitetural)
- [Camadas da Aplicação](#camadas-da-aplicação)
- [Fluxo de Dados](#fluxo-de-dados)
- [Tecnologias e Integração](#tecnologias-e-integração)
- [Padrões de Design](#padrões-de-design)
- [Segurança](#segurança)
- [Escalabilidade](#escalabilidade)

---

## 🎯 Padrão Arquitetural

O projeto segue a arquitetura em **camadas** (Layered Architecture) combinada com **Event-Driven Architecture**.

### Estrutura em Camadas

```
┌─────────────────────────────────────────────────┐
│           PRESENTATION LAYER                     │
│  (Controllers, Exception Handlers, DTOs)        │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│         APPLICATION LAYER                        │
│  (Services, Listeners, Mappers)                 │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│           DOMAIN LAYER                           │
│  (Entities, Repositories, Business Logic)       │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│       INFRASTRUCTURE LAYER                       │
│  (Database, RabbitMQ, WebSocket, Configs)       │
└─────────────────────────────────────────────────┘
```

### Benefícios dessa arquitetura:

1. **Separação de Responsabilidades** - Cada camada tem um propósito
2. **Testabilidade** - Fácil testar cada camada isoladamente
3. **Manutenibilidade** - Mudanças em uma camada não afetam outras
4. **Escalabilidade** - Fácil adicionar novas funcionalidades

---

## 📚 Camadas da Aplicação

### 1. Presentation Layer (Apresentação)

**Localização:** `api/` (controller, handler)

**Responsabilidades:**
- Receber requisições HTTP
- Validar entrada de dados
- Retornar respostas HTTP padronizadas
- Tratar exceções

**Componentes:**
- `IncidentController` - Gerencia requisições de incidentes
- `HealthController` - Health check
- `GlobalExceptionHandler` - Trata exceções globalmente

**Fluxo:**
```
POST /api/incidents (JSON)
    ↓
IncidentController.createIncident()
    ↓
Valida @Valid
    ↓
Chama IncidentService
    ↓
Retorna SuccessResponse (201)
```

**Exemplo:**
```java
@RestController
@RequestMapping("/incidents")
public class IncidentController {
    @PostMapping
    public ResponseEntity<SuccessResponse<IncidentResponse>> createIncident(
            @Valid @RequestBody CreateIncidentRequest request) {
        // Validação automática via @Valid
        // Converte para resposta padronizada
    }
}
```

---

### 2. Application Layer (Aplicação)

**Localização:** `application/` (service, listener)

**Responsabilidades:**
- Implementar lógica de negócio
- Coordenar entre camadas
- Gerenciar transações
- Publicar eventos

**Componentes:**
- `IncidentService` - Lógica de incidentes
- `WebSocketNotificationService` - Notificações
- `IncidentListener` - Consome eventos RabbitMQ

**Padrões:**
- **Service Pattern** - Encapsula lógica de negócio
- **Observer Pattern** - Listeners observam eventos
- **Dependency Injection** - Spring injeta dependências

**Exemplo:**
```java
@Service
@Transactional  // Transação gerenciada pelo Spring
public class IncidentService {
    
    // Dependências injetadas
    private final IncidentRepository repository;
    private final IncidentMapper mapper;
    private final RabbitTemplate rabbitTemplate;
    
    public IncidentResponse createIncident(CreateIncidentRequest request) {
        // 1. Converter DTO para Entidade
        Incident incident = mapper.toEntity(request);
        
        // 2. Persistir no BD
        Incident saved = repository.save(incident);
        
        // 3. Publicar evento
        rabbitTemplate.convertAndSend(exchange, routingKey, saved);
        
        // 4. Converter para Response
        return mapper.toResponse(saved);
    }
}
```

---

### 3. Domain Layer (Domínio)

**Localização:** `domain/` (entity, repository)

**Responsabilidades:**
- Definir entidades (modelos de dados)
- Definir interfaces de acesso aos dados
- Encapsular regras de negócio do domínio

**Componentes:**
- `Incident` - Entidade JPA
- `IncidentRepository` - Interface para persistência

**Características:**
- Entidades são imutáveis quando possível
- Lógica de domínio está na entidade
- Repository segue o padrão Repository Pattern

**Exemplo:**
```java
@Entity
@Table(name = "incidents")
public class Incident {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Enums definem valores válidos
    @Enumerated(EnumType.STRING)
    private SeverityLevel severity;
    
    // Automaticamente gerencia timestamps
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

**Repository:**
```java
@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findByZabbixEventId(String zabbixEventId);
}
```

---

### 4. Infrastructure Layer (Infraestrutura)

**Localização:** `infrastructure/` (config, messaging, websocket)

**Responsabilidades:**
- Configurar tecnologias externas
- Gerenciar conexões com sistemas externos
- Implementar comunicações

**Componentes:**
- `WebSocketConfig` - Configura WebSocket (STOMP)
- `RabbitMQConfig` - Configura filas e exchanges
- `CorsConfig` - Configura CORS
- `ApplicationConfig` - Configurações gerais

**Tecnologias integradas:**
- **MySQL** - Banco de dados relacional
- **RabbitMQ** - Fila de mensagens
- **WebSocket** - Comunicação em tempo real

**Exemplo:**
```java
@Configuration
public class RabbitMQConfig {
    
    @Bean
    public TopicExchange incidentExchange() {
        return new TopicExchange("zabbix.incident.exchange", true, false);
    }
    
    @Bean
    public Queue incidentQueue() {
        return new Queue("zabbix.incident.queue", true);
    }
    
    @Bean
    public Binding incidentBinding() {
        return BindingBuilder.bind(incidentQueue())
                .to(incidentExchange())
                .with("incident.created");
    }
}
```

---

## 🔄 Fluxo de Dados

### Cenário 1: Criar Incidente (HTTP Request)

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. ZABBIX envia POST /api/incidents                             │
│    Body: {"zabbixEventId": "evt_123", "title": "CPU alta", ...} │
└────────────┬────────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. IncidentController.createIncident()                          │
│    - Recebe CreateIncidentRequest                               │
│    - Valida com @Valid (se falhar → 400 Bad Request)            │
│    - Chama service.createIncident(request)                      │
└────────────┬────────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. IncidentService.createIncident()                             │
│    a) mapper.toEntity(request) → Incident                       │
│    b) repository.save(incident) → Salva em MySQL                │
│    c) rabbitTemplate.convertAndSend() → Publica na fila        │
│    d) mapper.toResponse() → Retorna IncidentResponse            │
└────────────┬────────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4. RabbitMQ recebe a mensagem                                   │
│    - Armazena na fila "zabbix.incident.queue"                   │
│    - Aguarda consumer processar                                 │
└────────────┬────────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 5. IncidentListener.handleIncidentMessage()                     │
│    - Consome mensagem da fila                                   │
│    - Chama webSocketService.notifyIncidentCreated()             │
└────────────┬────────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 6. WebSocketNotificationService.notifyIncidentCreated()         │
│    - messagingTemplate.convertAndSend("/topic/incidents", data) │
│    - Envia para todos os clientes inscritos                     │
└────────────┬────────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 7. FRONTEND (Angular)                                           │
│    - Recebe mensagem via WebSocket em tempo real                │
│    - Atualiza UI com novo incidente                             │
└─────────────────────────────────────────────────────────────────┘
```

### Tempos Estimados

| Etapa | Tempo |
|-------|-------|
| HTTP Request → Controller | ~1ms |
| Validação + Service | ~5ms |
| Salvar em MySQL | ~10ms |
| Publicar RabbitMQ | ~2ms |
| Response HTTP (Zabbix) | ~20ms **TOTAL** |
| Consumir RabbitMQ | ~1ms |
| Enviar WebSocket | ~5ms |
| Frontend recebe | ~50ms **APÓS** Zabbix |

---

### Cenário 2: Atualizar Status (HTTP Request)

```
PUT /api/incidents/1/status → UpdateIncidentStatusRequest("RESOLVED")
                ↓
IncidentController.updateIncidentStatus()
                ↓
IncidentService.updateIncidentStatus()
  - repository.findById(1)
  - Atualiza status para RESOLVED
  - repository.save()
  - Publica na RabbitMQ
                ↓
RabbitMQ (mesma fila)
                ↓
IncidentListener consome
                ↓
WebSocketNotificationService.notifyIncidentUpdated()
                ↓
Frontend recebe atualização em tempo real
```

---

## 🔧 Tecnologias e Integração

### 1. Spring Boot 3.5.10

**O que é:** Framework Java para criar aplicações web.

**Por que usar:**
- Configuração automática
- Ecossistema grande
- Produção pronta

**Componentes usados:**
- `spring-boot-starter-web` - REST controllers
- `spring-boot-starter-data-jpa` - Persistência
- `spring-boot-starter-amqp` - RabbitMQ
- `spring-boot-starter-websocket` - WebSocket

---

### 2. MySQL (Banco de Dados)

**O que é:** Banco de dados relacional.

**Como é usado:**
- JPA (Hibernate) cria as tabelas automaticamente
- `application-local.properties` configura conexão
- Repository acessa os dados

**Tabelas criadas:**
```sql
CREATE TABLE incidents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    zabbix_event_id VARCHAR(255) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    severity VARCHAR(50),
    status VARCHAR(50),
    source VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

---

### 3. RabbitMQ (Message Broker)

**O que é:** Sistema de filas para mensagens assíncronas.

**Como funciona:**

```
Producer → Exchange → Queue → Consumer
(Service)  (Hub)    (Storage)  (Listener)
```

**Configuração:**
```java
@Configuration
public class RabbitMQConfig {
    
    // Exchange: recebe mensagens
    @Bean
    public TopicExchange incidentExchange() {
        return new TopicExchange("zabbix.incident.exchange");
    }
    
    // Queue: armazena
    @Bean
    public Queue incidentQueue() {
        return new Queue("zabbix.incident.queue");
    }
    
    // Binding: conecta Exchange à Queue
    @Bean
    public Binding incidentBinding() {
        return BindingBuilder.bind(incidentQueue())
                .to(incidentExchange())
                .with("incident.created");
    }
}
```

**Vantagens:**
- Desacoplamento entre componentes
- Confiabilidade (não perde mensagens)
- Escalabilidade

---

### 4. WebSocket (Comunicação Real-time)

**O que é:** Protocolo para comunicação bidirecional persistente.

**Diferença com HTTP:**

| HTTP | WebSocket |
|------|-----------|
| Requisição → Resposta | Conexão aberta |
| Polling (pull) | Push de dados |
| Stateless | Stateful |
| Lento para real-time | Rápido |

**Como funciona:**

```javascript
// Frontend se conecta
const socket = new SockJS('http://localhost:8080/ws/incidents');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
    // Se inscreve num tópico
    stompClient.subscribe('/topic/incidents', (message) => {
        // Recebe mensagens em tempo real
        const incident = JSON.parse(message.body);
        updateUI(incident);
    });
});
```

**Backend envia:**
```java
messagingTemplate.convertAndSend("/topic/incidents", incidentResponse);
// Todos conectados em /topic/incidents recebem
```

---

### 5. MapStruct (Mapeamento)

**O que é:** Gerador de código para mapear entre objetos.

**Sem MapStruct (Manual):**
```java
IncidentResponse response = new IncidentResponse(
    incident.getId(),
    incident.getZabbixEventId(),
    incident.getTitle(),
    // ... copiar 10+ campos manualmente
);
```

**Com MapStruct (Automático):**
```java
@Mapper(componentModel = "spring")
public interface IncidentMapper {
    IncidentResponse toResponse(Incident incident);
}

// Uso
IncidentResponse response = mapper.toResponse(incident);
```

---

## 🎨 Padrões de Design

### 1. Repository Pattern

**O que é:** Abstrai o acesso a dados.

**Benefício:** Trocar banco de dados sem alterar service.

```java
@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findByZabbixEventId(String zabbixEventId);
}
```

### 2. Dependency Injection

**O que é:** Spring injeta dependências automaticamente.

```java
@Service
public class IncidentService {
    // Spring injeta automaticamente
    private final IncidentRepository repository;
    
    // Construtor (recomendado para final fields)
    public IncidentService(IncidentRepository repository) {
        this.repository = repository;
    }
}
```

**Benefícios:**
- Testabilidade (fácil mockar)
- Desacoplamento

### 3. Service Pattern

**O que é:** Camada que encapsula lógica de negócio.

```java
@Service
public class IncidentService {
    public IncidentResponse createIncident(CreateIncidentRequest request) {
        // Lógica complexa aqui
        // Service coordena repository, mapper, etc
    }
}
```

### 4. Observer Pattern

**O que é:** Listener observa eventos.

```java
@Service
public class IncidentListener {
    @RabbitListener(queues = "zabbix.incident.queue")
    public void handleIncidentMessage(Incident incident) {
        // Observers (listeners) reagem a eventos
        webSocketService.notifyIncidentCreated(incident);
    }
}
```

### 5. DTO (Data Transfer Object) Pattern

**O que é:** Objetos para transferir dados entre camadas.

```java
// Request DTO
public record CreateIncidentRequest(
    String zabbixEventId,
    String title,
    String severity,
    String source
) {}

// Response DTO
public record IncidentResponse(
    Long id,
    String title,
    String severity,
    LocalDateTime createdAt
) {}
```

**Por que:**
- Validação de entrada
- Ocultar campos internos
- Facilitar manutenção

---

## 🔒 Segurança

### 1. Validação de Entrada

```java
public record CreateIncidentRequest(
    @NotBlank(message = "zabbixEventId não pode estar vazio")
    String zabbixEventId,
    
    @NotBlank(message = "title não pode estar vazio")
    String title
) {}
```

### 2. CORS (Cross-Origin Resource Sharing)

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")  // Apenas seu frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```

### 3. Tratamento de Exceções

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        // Log seguro (sem dados sensíveis)
        return ResponseEntity.status(500)
                .body(new ErrorResponse(500, "Erro interno"));
    }
}
```

### 4. Transações (Atomicidade)

```java
@Service
@Transactional  // Rollback automático se falhar
public class IncidentService {
    public IncidentResponse createIncident(CreateIncidentRequest request) {
        // Se falhar, tudo é desfeito
        repository.save(incident);
        rabbitTemplate.convertAndSend(...);
    }
}
```

---

## 📈 Escalabilidade

### Pontos de Escalabilidade

#### 1. **Database** (MySQL)
```
Problema: Uma instância pode ficar sobrecarregada
Solução: 
- Replicação (master-slave)
- Particionamento (sharding)
- Cache (Redis)
```

#### 2. **RabbitMQ**
```
Problema: Uma fila pode ficar lenta
Solução:
- Múltiplas instâncias (cluster)
- Múltiplos workers consumindo
- Priorização de mensagens
```

#### 3. **Aplicação**
```
Problema: Um servidor pode falhar
Solução:
- Load balancer (Nginx, HAProxy)
- Múltiplas instâncias
- Auto-scaling
```

#### 4. **WebSocket**
```
Problema: Muitos clientes simultâneos
Solução:
- Usar session repository distribuído
- Redis para compartilhar sessões
- Message broker distribuído
```

### Arquitetura Escalável

```
┌──────────────┐
│   Zabbix     │
└──────┬───────┘
       │
       ▼
┌──────────────────────────────────────────┐
│        Load Balancer (Nginx)             │
└──────┬──────────────┬──────────────┬─────┘
       │              │              │
   ┌───▼──┐     ┌──────┐     ┌──────┐
   │App#1 │     │App#2 │     │App#3 │
   └───┬──┘     └──┬───┘     └───┬──┘
       │           │              │
       └─────┬─────┴──────┬──────┘
             │            │
         ┌───▼────────────▼──┐
         │   RabbitMQ        │
         │   (Cluster)       │
         └─────┬─────────────┘
               │
     ┌─────────┴─────────┐
     │                   │
 ┌───▼───┐         ┌──────▼──┐
 │MySQL  │         │Redis    │
 │(Repli)│         │(Cache)  │
 └───────┘         └─────────┘
```

---

## 🚀 Performance

### Otimizações Implementadas

1. **Índices no Banco**
   ```sql
   CREATE UNIQUE INDEX idx_zabbix_event_id 
   ON incidents(zabbix_event_id);
   ```

2. **Paginação**
   ```java
   Page<IncidentResponse> getAllIncidents(Pageable pageable)
   // Recupera apenas uma página de dados
   ```

3. **Lazy Loading**
   ```java
   @Transactional(readOnly = true)  // Sem commit desnecessário
   public Optional<IncidentResponse> getIncidentById(Long id)
   ```

4. **Async Processing** (RabbitMQ)
   ```
   HTTP (síncrono) → 20ms
   RabbitMQ (assíncrono) → Não bloqueia
   ```

---

## 📊 Monitoramento

### Métricas Importantes

```properties
# Adicionar em application.properties para monitoramento
management.endpoints.web.exposure.include=health,metrics,prometheus
```

**Métricas:**
- Tempo de resposta HTTP
- Taxa de erro
- Tamanho da fila RabbitMQ
- Conexões ativas WebSocket
- Tempo de query no BD


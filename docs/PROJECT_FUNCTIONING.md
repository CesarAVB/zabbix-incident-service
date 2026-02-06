# 🚀 Funcionamento Completo do Projeto - ZabbixIncidentService

Documentação completa sobre como o projeto funciona do início ao fim, desde a arquitetura até o deployment.

## 📋 Índice

- [Visão Geral](#visão-geral)
- [Arquitetura em Camadas](#arquitetura-em-camadas)
- [Fluxo de Dados Completo](#fluxo-de-dados-completo)
- [Cenários de Uso](#cenários-de-uso)
- [Integrações Externas](#integrações-externas)
- [Processamento Assíncrono](#processamento-assíncrono)
- [Comunicação em Tempo Real](#comunicação-em-tempo-real)
- [Tratamento de Erros](#tratamento-de-erros)
- [Monitoramento e Observabilidade](#monitoramento-e-observabilidade)
- [Deployment e Escalabilidade](#deployment-e-escalabilidade)

---

## 🌟 Visão Geral

O **ZabbixIncidentService** é uma aplicação Spring Boot que integra o sistema de monitoramento Zabbix com um sistema de gestão de incidentes, proporcionando:

- **Recepção de eventos** do Zabbix via API REST
- **Processamento assíncrono** via RabbitMQ
- **Notificações em tempo real** via WebSocket
- **Persistência** em banco MySQL
- **Interface padronizada** com responses consistentes

### Propósito
- Centralizar incidentes reportados pelo Zabbix
- Proporcionar visibilidade em tempo real para equipes de operação
- Manter histórico de incidentes para análise
- Integrar com sistemas de gestão de incidentes

---

## 🏗️ Arquitetura em Camadas

O projeto segue arquitetura em camadas bem definida:

```
┌─────────────────────────────────────────────────┐
│           PRESENTATION LAYER                     │
│  (Controllers, Exception Handlers, DTOs)        │
│                                                 │
│  - IncidentController (planejado)               │
│  - HealthController                             │
│  - GlobalExceptionHandler                       │
│  - DTOs (Request/Response)                      │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│         APPLICATION LAYER                        │
│  (Services, Listeners, Mappers)                 │
│                                                 │
│  - IncidentService                               │
│  - WebSocketNotificationService                 │
│  - IncidentListener                              │
│  - IncidentMapper                                │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│           DOMAIN LAYER                           │
│  (Entities, Repositories, Business Logic)       │
│                                                 │
│  - Incident (Entity)                            │
│  - IncidentRepository                           │
│  - IncidentStatus (Enum)                        │
│  - SeverityLevel (Enum)                         │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│       INFRASTRUCTURE LAYER                       │
│  (Database, RabbitMQ, WebSocket, Configs)       │
│                                                 │
│  - MySQL Database                               │
│  - RabbitMQ Message Broker                      │
│  - WebSocket/STOMP                              │
│  - CorsConfig                                   │
│  - RabbitMQConfig                               │
│  - WebSocketConfig                              │
└─────────────────────────────────────────────────┘
```

### Benefícios da Arquitetura

1. **Separação de Responsabilidades** - Cada camada tem propósito claro
2. **Testabilidade** - Fácil testar camadas isoladamente
3. **Manutenibilidade** - Mudanças localizadas
4. **Escalabilidade** - Camadas podem ser escaladas independentemente

---

## 🔄 Fluxo de Dados Completo

### Cenário: Novo Incidente do Zabbix

```
1. ZABBIX DETECTA PROBLEMA
   ↓
2. Zabbix envia webhook/POST para /api/incidents
   ↓
3. IncidentController.createIncident() [PLANEJADO]
   ↓
4. Validação do CreateIncidentRequest (@Valid)
   ↓
5. IncidentService.createIncident()
   ↓
6. IncidentMapper.toEntity() → Incident
   ↓
7. IncidentRepository.save() → Persistir no MySQL
   ↓
8. rabbitTemplate.convertAndSend() → Publicar na fila
   ↓
9. IncidentListener.handleIncidentMessage() (assíncrono)
   ↓
10. WebSocketNotificationService.notifyIncidentCreated()
    ↓
11. messagingTemplate.convertAndSend("/topic/incidents")
    ↓
12. Clientes WebSocket recebem notificação em tempo real
```

### Cenário: Atualização de Status

```
1. REQUEST PUT /api/incidents/{id}/status [PLANEJADO]
   ↓
2. IncidentController.updateIncidentStatus() [PLANEJADO]
   ↓
3. IncidentService.updateIncidentStatus()
   ↓
4. IncidentRepository.findById() + validação
   ↓
5. Atualizar status + IncidentRepository.save()
   ↓
6. Publicar atualização na fila RabbitMQ
   ↓
7. WebSocket notification para clientes
```

### Cenário: Consulta de Incidentes

```
1. REQUEST GET /api/incidents?page=0&size=10 [PLANEJADO]
   ↓
2. IncidentController.getAllIncidents() [PLANEJADO]
   ↓
3. IncidentService.getAllIncidents(Pageable)
   ↓
4. IncidentRepository.findAll(pageable)
   ↓
5. IncidentMapper.toResponse() para cada incidente
   ↓
6. Retornar Page<IncidentResponse> paginado
```

---

## 📋 Cenários de Uso

### 1. Monitoramento de Infraestrutura

**Contexto:** Equipe de infraestrutura monitora servidores via Zabbix

**Fluxo:**
1. Zabbix detecta CPU alta > 90%
2. Dispara webhook para o serviço
3. Incidente criado automaticamente
4. Equipe recebe notificação em tempo real
5. Analista investiga e atualiza status
6. Problema resolvido, incidente fechado

### 2. Alertas de Aplicação

**Contexto:** Aplicação web com monitoring integrado

**Fluxo:**
1. Zabbix monitora endpoints de health check
2. Serviço fica indisponível
3. Incidente crítico criado
4. Equipe de desenvolvimento notificada
5. Deploy de correção realizado
6. Status atualizado para RESOLVED

### 3. Relatórios e Análises

**Contexto:** Gestão quer analisar incidentes do mês

**Fluxo:**
1. Consultas paginadas para listar incidentes
2. Filtros por severidade, status, período
3. Exportação de dados para análise
4. Identificação de padrões de falha

---

## 🔗 Integrações Externas

### Zabbix Integration

**Como funciona:**
- Zabbix configura webhooks para eventos
- Payload enviado em JSON para `/api/incidents`
- Mapeamento de campos Zabbix → Incident

**Campos mapeados:**
```json
{
  "zabbixEventId": "ID do evento Zabbix",
  "title": "Nome do trigger",
  "description": "Descrição do problema",
  "severity": "CRITICAL|HIGH|MEDIUM|LOW|INFO",
  "source": "zabbix",
  "host": "Nome do host",
  "item": "Nome do item",
  "trigger": "Nome do trigger"
}
```

### Frontend Integration

**WebSocket para tempo real:**
```javascript
// Conectar ao WebSocket
const socket = new SockJS('/ws/incidents');
const stompClient = Stomp.over(socket);

// Inscrever no tópico
stompClient.connect({}, function(frame) {
    stompClient.subscribe('/topic/incidents', function(message) {
        const incident = JSON.parse(message.body);
        // Atualizar UI em tempo real
    });
});
```

**API REST para operações CRUD:**
```javascript
// Criar incidente
fetch('/api/incidents', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(incidentData)
});

// Listar incidentes
fetch('/api/incidents?page=0&size=10&sort=createdAt,desc')
    .then(response => response.json())
    .then(data => {
        // data.content contém os incidentes
    });
```

---

## ⚡ Processamento Assíncrono

### Por que Assíncrono?

1. **Desacoplamento** - API responde imediatamente
2. **Resiliência** - Sistema continua funcionando se RabbitMQ cair
3. **Performance** - Operações pesadas não bloqueiam API
4. **Escalabilidade** - Múltiplas instâncias podem processar filas

### Fluxo RabbitMQ

```
API Instance 1          RabbitMQ Broker          API Instance 2
     │                        │                        │
     │  convertAndSend()      │                        │
     │────────────────────────>│                        │
     │                        │  routing key           │
     │                        │  "incident.created"    │
     │                        │                        │
     │                        │  deliver to queue      │
     │                        │────────────────────────>│
     │                        │                        │ @RabbitListener
     │                        │                        │ handleIncidentMessage()
     │                        │                        │
     │                        │                        │ WebSocket notification
```

### Benefícios Práticos

- **API Response Time** reduzido
- **Throughput** aumentado
- **Fault Tolerance** melhorada
- **Load Balancing** automático

---

## 🌐 Comunicação em Tempo Real

### WebSocket Architecture

```
Frontend Browser          Spring Boot              Frontend Browser
     │                        │                        │
     │  SockJS Connect        │                        │
     │  /ws/incidents          │                        │
     │────────────────────────>│                        │
     │                        │  STOMP handshake       │
     │                        │                        │
     │  SUBSCRIBE             │                        │
     │  /topic/incidents       │                        │
     │────────────────────────>│                        │
     │                        │                        │
     │                        │  Incident Created      │
     │                        │  notifyIncidentCreated │
     │                        │                        │
     │                        │  convertAndSend()      │
     │                        │  /topic/incidents      │
     │                        │ <──────────────────────┘
     │  MESSAGE               │                        │
     │  (incident data)       │                        │
     │<───────────────────────│                        │
```

### STOMP Protocol

**Comandos utilizados:**
- `CONNECT` - Estabelecer conexão
- `SUBSCRIBE` - Inscrever em tópico
- `MESSAGE` - Receber mensagens
- `DISCONNECT` - Fechar conexão

**Tópicos disponíveis:**
- `/topic/incidents` - Novos e atualizados incidentes
- `/topic/incidents/deleted` - Incidentes deletados

### Fallback Strategy

**SockJS** fornece fallback automático:
1. WebSocket nativo (preferido)
2. Server-Sent Events
3. Long polling
4. Polling simples

---

## 🚨 Tratamento de Erros

### Níveis de Tratamento

#### 1. Validação de Entrada
```java
@PostMapping
public ResponseEntity<?> createIncident(@Valid @RequestBody CreateIncidentRequest request) {
    // @Valid dispara MethodArgumentNotValidException se inválido
}
```

#### 2. Regras de Negócio
```java
public IncidentResponse updateIncidentStatus(Long id, UpdateIncidentStatusRequest request) {
    Incident incident = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Incidente não encontrado"));
}
```

#### 3. Tratamento Global
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(...) {
        // Retorna 400 com detalhes dos campos inválidos
    }
}
```

### Estratégias de Resiliência

#### Circuit Breaker
- Implementar para chamadas externas
- Prevenir cascata de falhas

#### Retry Logic
- Para operações transacionais
- Com backoff exponencial

#### Dead Letter Queue
- Para mensagens não processadas
- Análise de falhas

---

## 📊 Monitoramento e Observabilidade

### Health Checks

**Endpoint:** `GET /health`

**Verifica:**
- Status da aplicação: "UP"
- Serviço: "ZabbixIncidentService"
- Timestamp atual

### Métricas Possíveis

**Aplicação:**
- Número de incidentes criados por hora
- Tempo médio de resposta das APIs
- Taxa de erro por endpoint

**Infraestrutura:**
- Conexões ativas WebSocket
- Mensagens na fila RabbitMQ
- Performance do banco MySQL

### Logs Estruturados

**Níveis:**
- `ERROR` - Falhas críticas
- `WARN` - Problemas não críticos
- `INFO` - Operações importantes
- `DEBUG` - Detalhes para desenvolvimento

**Informações importantes:**
- IDs de correlação para rastreamento
- Timestamps em UTC
- Contexto da operação

---

## 🚀 Deployment e Escalabilidade

### Estratégias de Deployment

#### Desenvolvimento
```bash
# Executar localmente
./mvnw spring-boot:run

# Ou com perfil específico
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

#### Produção
```bash
# Build do JAR
./mvnw clean package -DskipTests

# Executar
java -jar target/zabbix-incident-service-1.0.0.jar --spring.profiles.active=prod
```

### Containerização (Docker)

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jdk as builder
WORKDIR /app
COPY pom.xml .
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Escalabilidade Horizontal

**API Layer:**
- Múltiplas instâncias atrás de load balancer
- Sessões WebSocket sticky ou broadcast

**Worker Layer:**
- Instâncias dedicadas para processamento RabbitMQ
- Auto-scaling baseado no tamanho da fila

**Database:**
- MySQL com réplicas de leitura
- Connection pooling (HikariCP)

### Variáveis de Ambiente

**Container Production:**
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod
  - MYSQLHOST=mysql-prod
  - MYSQLUSER=incident_user
  - MYSQLPASSWORD=${DB_PASSWORD}
  - RABBITMQ_HOST=rabbitmq-prod
  - WEBSOCKET_ALLOWED_ORIGINS=https://incidents.company.com
```

### Backup e Recuperação

**Database:**
- Backups automáticos diários
- Point-in-time recovery
- Réplicas para alta disponibilidade

**Mensagens:**
- Persistent queues no RabbitMQ
- Dead letter exchanges para mensagens não processadas

---

## 🔧 Manutenção e Operações

### Tarefas Recorrentes

#### Limpeza de Dados
- Arquivar incidentes antigos (> 1 ano)
- Limpar logs de auditoria

#### Monitoramento de Performance
- Verificar queries lentas
- Monitorar uso de memória/CPU
- Alertas para filas grandes

#### Atualizações
- Atualizar dependências regularmente
- Testar compatibilidade com novas versões
- Rolling updates em produção

### Troubleshooting Comum

#### Problema: Incidentes não aparecem em tempo real
**Causa:** WebSocket desconectado
**Solução:** Verificar configurações CORS, reconectar cliente

#### Problema: Mensagens acumulando na fila
**Causa:** Consumers parados ou lentos
**Solução:** Verificar logs dos listeners, aumentar instâncias

#### Problema: Database connection timeout
**Causa:** Pool esgotado ou rede
**Solução:** Ajustar pool size, verificar conectividade

---

## 🎯 Conclusão

O ZabbixIncidentService representa uma solução robusta para integração entre sistemas de monitoramento e gestão de incidentes, oferecendo:

- **Arquitetura escalável** com separação clara de responsabilidades
- **Processamento resiliente** com mensageria assíncrona
- **Comunicação eficiente** em tempo real
- **Observabilidade completa** para operações
- **Flexibilidade** para diferentes cenários de uso

A combinação de Spring Boot, RabbitMQ, WebSocket e MySQL proporciona uma base sólida para sistemas de missão crítica, com possibilidade de evolução e integração com outros sistemas empresariais.</content>
<parameter name="filePath">D:\Documentos\PROGRAMAÇÃO\PROJETOS\BACKEND\zabbix-incident-service\docs\PROJECT_FUNCTIONING.md
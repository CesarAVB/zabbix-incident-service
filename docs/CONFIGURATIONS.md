# ⚙️ Configurações - ZabbixIncidentService

Documentação detalhada de todas as configurações da aplicação, incluindo classes de configuração, propriedades e ambientes.

## 📋 Índice

- [Application Properties](#application-properties)
- [CorsConfig](#corsconfig)
- [RabbitMQConfig](#rabbitmqconfig)
- [WebSocketConfig](#websocketconfig)
- [Perfis de Ambiente](#perfis-de-ambiente)

---

## 📄 Application Properties

### application.properties (Principal)

**Localização:** `src/main/resources/application.properties`

**Configurações globais:**
```properties
# Nome da aplicação
spring.application.name=ZabbixIncidentService

# Perfil ativo (padrão: local)
spring.profiles.active=${SPRING_PROFILES_ACTIVE:local}

# Logging
logging.level.root=INFO
logging.level.br.com.cesaravb.zabbixincident=DEBUG
```

**Explicação:**
- `spring.application.name` - Identifica a aplicação nos logs e monitoramento
- `spring.profiles.active` - Define qual perfil carregar (local/prod)
- `logging.level` - Níveis de log (DEBUG para código da aplicação)

### application-local.properties

**Localização:** `src/main/resources/application-local.properties`

**Uso:** Ambiente de desenvolvimento

#### Server Configuration
```properties
server.port=8080
```
- Porta onde a aplicação roda localmente

#### Database - MySQL
```properties
spring.datasource.url=jdbc:mysql://45.187.224.251:3306/network_map?useUnicode=yes&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=Sql!RootP4ss
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```
- Conexão com banco MySQL remoto para desenvolvimento
- Database: `network_map`
- Credenciais hardcoded (apenas para dev)

#### JPA/Hibernate
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true
```
- `ddl-auto=update` - Atualiza schema automaticamente
- `show-sql=true` - Mostra SQLs executados nos logs
- `format_sql=true` - Formata SQLs para leitura

#### RabbitMQ
```properties
spring.rabbitmq.host=45.187.224.251
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=Rmq!RootP4ss
spring.rabbitmq.virtual-host=default
```
- Conexão com RabbitMQ remoto
- Virtual host padrão

#### Application Custom
```properties
app.rabbitmq.exchange.incident=zabbix.incident.exchange
app.rabbitmq.queue.incident=zabbix.incident.queue
app.rabbitmq.routing-key.incident=incident.created
```
- Exchange para publicar incidentes
- Fila para processar incidentes
- Routing key para mensagens

#### WebSocket
```properties
app.websocket.allowed-origins=http://localhost:4200,http://45.187.224.50,https://incidents.redelognet.com.br
app.websocket.endpoint=/ws/incidents
app.websocket.broker-prefix=/topic
app.websocket.app-prefix=/app
```
- Origins permitidos (localhost para dev, IPs para produção)
- Endpoint do WebSocket
- Prefixos STOMP

#### CORS
```properties
app.cors.allowed-origins=http://localhost:4200,http://45.187.224.50,https://incidents.redelognet.com.br
```
- Mesmas origins do WebSocket

### application-prod.properties

**Localização:** `src/main/resources/application-prod.properties`

**Uso:** Ambiente de produção

**Diferenças principais:**
- Todas as configurações usam variáveis de ambiente
- `ddl-auto=validate` (não altera schema)
- `show-sql=false` (não loga SQLs)
- Credenciais via environment variables

```properties
# Database
spring.datasource.url=jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=${MYSQLUSER}
spring.datasource.password=${MYSQLPASSWORD}

# RabbitMQ
spring.rabbitmq.host=${RABBITMQ_HOST}
spring.rabbitmq.port=${RABBITMQ_PORT}
spring.rabbitmq.username=${RABBITMQ_USERNAME}
spring.rabbitmq.password=${RABBITMQ_PASSWORD}
spring.rabbitmq.virtual-host=${RABBITMQ_VHOST}

# Application
app.rabbitmq.exchange.incident=${RABBITMQ_EXCHANGE_INCIDENT}
app.rabbitmq.queue.incident=${RABBITMQ_QUEUE_INCIDENT}
app.rabbitmq.routing-key.incident=${RABBITMQ_ROUTING_KEY_INCIDENT}

# WebSocket
app.websocket.allowed-origins=${WEBSOCKET_ALLOWED_ORIGINS}
app.websocket.endpoint=${WEBSOCKET_ENDPOINT}
app.websocket.broker-prefix=${WEBSOCKET_BROKER_PREFIX}
app.websocket.app-prefix=${WEBSOCKET_APP_PREFIX}

# CORS
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS}
```

---

## 🌐 CorsConfig

**Localização:** `infrastructure/config/CorsConfig.java`

**Responsabilidades:**
- Configurar CORS (Cross-Origin Resource Sharing)
- Permitir requisições do frontend

**Implementação:**
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

**Configurações:**
- `allowedOrigins` - Lista de domínios permitidos (split por vírgula)
- `allowedMethods` - Métodos HTTP permitidos
- `allowedHeaders` - Todos os headers permitidos
- `allowCredentials` - Permite cookies/autenticação
- `maxAge` - Cache da preflight request (1 hora)

**Por que necessário:**
- Frontend e backend em domínios diferentes
- Previne ataques CORS
- Permite comunicação segura

---

## 🐰 RabbitMQConfig

**Localização:** `infrastructure/config/RabbitMQConfig.java`

**Responsabilidades:**
- Configurar infraestrutura RabbitMQ
- Declarar exchanges, queues e bindings

**Componentes configurados:**

### TopicExchange
```java
@Bean
public TopicExchange incidentExchange() {
    return new TopicExchange(incidentExchange, true, false);
}
```
- Exchange do tipo topic
- `durable=true` - Sobrevive restart do broker
- `autoDelete=false` - Não deleta automaticamente

### Queue
```java
@Bean
public Queue incidentQueue() {
    return new Queue(incidentQueue, true);
}
```
- Fila durável
- Armazena mensagens mesmo com restart

### Binding
```java
@Bean
public Binding incidentBinding(Queue incidentQueue, TopicExchange incidentExchange) {
    return BindingBuilder.bind(incidentQueue).to(incidentExchange).with(incidentRoutingKey);
}
```
- Vincula fila ao exchange
- Routing key: `incident.created`

**Fluxo de mensagens:**
```
IncidentService → Exchange (incident.created) → Queue → IncidentListener
```

---

## 🌐 WebSocketConfig

**Localização:** `infrastructure/config/WebSocketConfig.java`

**Responsabilidades:**
- Configurar WebSocket com STOMP
- Habilitar comunicação em tempo real
- Configurar broker de mensagens

**Anotações:**
- `@Configuration` - Classe de configuração
- `@EnableWebSocketMessageBroker` - Habilita WebSocket

### Propriedades
```java
@Value("${app.websocket.allowed-origins}")
private String allowedOrigins;

@Value("${app.websocket.endpoint}")
private String endpoint;

@Value("${app.websocket.broker-prefix}")
private String brokerPrefix;

@Value("${app.websocket.app-prefix}")
private String appPrefix;
```

### configureMessageBroker()
```java
@Override
public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker(brokerPrefix);
    config.setApplicationDestinationPrefixes(appPrefix);
}
```
- `enableSimpleBroker("/topic")` - Broker em memória para broadcast
- `setApplicationDestinationPrefixes("/app")` - Prefixo para handlers

### registerStompEndpoints()
```java
@Override
public void registerStompEndpoints(StompEndpointRegistry registry) {
    String[] origins = allowedOrigins.split(",");
    registry.addEndpoint(endpoint).setAllowedOrigins(origins).withSockJS();
}
```
- Endpoint: `/ws/incidents`
- SockJS fallback para browsers sem WebSocket
- CORS habilitado

**Funcionamento:**
1. Cliente conecta em `/ws/incidents`
2. Negocia STOMP over WebSocket
3. Subscreve `/topic/incidents`
4. Recebe mensagens em tempo real

---

## 🏗️ Perfis de Ambiente

### Local (Desenvolvimento)
- **Ativação:** `SPRING_PROFILES_ACTIVE=local` ou padrão
- **Características:**
  - Configurações hardcoded
  - DDL auto update
  - Logs detalhados
  - Acesso remoto para facilitar desenvolvimento

### Produção
- **Ativação:** `SPRING_PROFILES_ACTIVE=prod`
- **Características:**
  - Variáveis de ambiente
  - DDL validate (schema deve existir)
  - Logs mínimos
  - Segurança reforçada

**Como alternar:**
```bash
# Desenvolvimento
java -jar app.jar

# Produção
java -jar app.jar --spring.profiles.active=prod
```

**Variáveis de ambiente para produção:**
```bash
MYSQLHOST=prod-db-server
MYSQLPORT=3306
MYSQLDATABASE=incidents_prod
MYSQLUSER=app_user
MYSQLPASSWORD=secure_password

RABBITMQ_HOST=rabbitmq-prod
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=prod_user
RABBITMQ_PASSWORD=secure_rabbit_pass
RABBITMQ_VHOST=/prod

RABBITMQ_EXCHANGE_INCIDENT=zabbix.incident.prod
RABBITMQ_QUEUE_INCIDENT=incident.queue.prod
RABBITMQ_ROUTING_KEY_INCIDENT=incident.created

WEBSOCKET_ALLOWED_ORIGINS=https://meuapp.com,https://admin.meuapp.com
WEBSOCKET_ENDPOINT=/ws/incidents
WEBSOCKET_BROKER_PREFIX=/topic
WEBSOCKET_APP_PREFIX=/app

CORS_ALLOWED_ORIGINS=https://meuapp.com,https://admin.meuapp.com
```

---

## 🔧 Validação das Configurações

**Como verificar se está funcionando:**

### CORS
```bash
curl -H "Origin: http://localhost:4200" \
     -H "Access-Control-Request-Method: GET" \
     -X OPTIONS http://localhost:8080/health
```

### RabbitMQ
- Verificar conexão nos logs da aplicação
- Verificar se queues existem no RabbitMQ Management

### WebSocket
- Conectar via browser dev tools
- Verificar mensagens chegando em `/topic/incidents`

### Database
- Verificar logs de conexão
- Testar queries via health check indireto</content>
<parameter name="filePath">D:\Documentos\PROGRAMAÇÃO\PROJETOS\BACKEND\zabbix-incident-service\docs\CONFIGURATIONS.md
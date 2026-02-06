# 🚨 Zabbix Incident Service

[![Java](https://img.shields.io/badge/Java-21-orange?logo=java)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen?logo=spring-boot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)](https://www.mysql.com/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-orange?logo=rabbitmq)](https://www.rabbitmq.com/)
[![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-green)](https://stomp.github.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

API REST para gerenciamento de incidentes do Zabbix. Recebe alertas via webhook, armazena em MySQL, processa com RabbitMQ e notifica via WebSocket. Frontend React exibe incidentes em tempo real. Utilizado pelo NetMap.

---

## 📑 Sumário

- [Visão Geral](#-visão-geral)
- [Arquitetura](#-arquitetura)
- [Tecnologias](#-tecnologias)
- [Instalação](#-instalação)
- [Configuração](#-configuração)
- [API Endpoints](#-api-endpoints)
- [WebSocket](#-websocket)
- [Docker](#-docker)
- [Desenvolvimento](#-desenvolvimento)
- [Deployment](#-deployment)
- [Troubleshooting](#-troubleshooting)
- [Contribuindo](#-contribuindo)
- [Licença](#-licença)

---

## 🎯 Visão Geral

O **Zabbix Incident Service** é um microserviço responsável por:

1. **Receber** alertas do Zabbix via webhook HTTP
2. **Armazenar** incidentes em banco de dados MySQL
3. **Processar** de forma assíncrona com RabbitMQ
4. **Notificar** o frontend em tempo real via WebSocket
5. **Exibir** incidentes no NetMap (React frontend)

### Fluxo Completo

```
Zabbix → POST /api/incidents → Controller → Service → MySQL
                                              ↓
                                          RabbitMQ
                                              ↓
                                          Listener
                                              ↓
                                          WebSocket
                                              ↓
                                    NetMap (Frontend)
                                    Atualização em
                                      tempo real! 🎉
```

---

## 🏗️ Arquitetura

### Camadas

```
┌────────────────────────────────────────────────────┐
│  PRESENTATION LAYER (api/controller/)              │
│  • IncidentController - Recebe requisições HTTP    │
│  • HealthController - Status da aplicação          │
└────────────────────────────────────────────────────┘
                         ↓
┌────────────────────────────────────────────────────┐
│  APPLICATION LAYER (application/service/)          │
│  • IncidentService - Lógica de negócio             │
│  • WebSocketNotificationService - Notificações     │
│  • IncidentListener - Consome fila RabbitMQ        │
└────────────────────────────────────────────────────┘
                         ↓
┌────────────────────────────────────────────────────┐
│  DOMAIN LAYER (domain/entity/)                     │
│  • Incident - Modelo de dados                      │
│  • SeverityLevel - Enum de severidades             │
│  • IncidentStatus - Enum de status                 │
└────────────────────────────────────────────────────┘
                         ↓
┌────────────────────────────────────────────────────┐
│  INFRASTRUCTURE LAYER (infrastructure/config/)    │
│  • MySQL - Banco de dados                          │
│  • RabbitMQ - Fila de mensagens                    │
│  • WebSocket - Comunicação tempo real              │
└────────────────────────────────────────────────────┘
```

### Componentes

- **Controller**: Recebe requisições HTTP do Zabbix
- **Service**: Coordena operações (save, publish, notify)
- **Repository**: Acessa dados no MySQL
- **Mapper**: Converte entre DTOs e Entities (MapStruct)
- **Listener**: Consome mensagens da fila RabbitMQ
- **WebSocketNotificationService**: Envia notificações em tempo real

---

## 🛠️ Tecnologias

### Backend

| Tecnologia | Versão | Uso |
|-----------|--------|-----|
| **Java** | 21 | Linguagem principal |
| **Spring Boot** | 3.5.10 | Framework web |
| **Spring Data JPA** | 3.5.10 | ORM |
| **Spring AMQP** | 3.5.10 | RabbitMQ client |
| **Spring WebSocket** | 3.5.10 | Comunicação tempo real |
| **MySQL** | 8.0 | Banco de dados |
| **RabbitMQ** | 3.13 | Fila assíncrona |
| **MapStruct** | 1.5.5 | Mapping de objetos |
| **Lombok** | 1.18.30 | Redução de boilerplate |
| **Validation** | Jakarta 3.0 | Validação de dados |

### DevOps

| Tecnologia | Uso |
|-----------|-----|
| **Docker** | Containerização |
| **Docker Compose** | Orquestração local |
| **Maven** | Build e dependências |
| **Git** | Controle de versão |

### Frontend

| Tecnologia | Versão |
|-----------|--------|
| **React** | 19+ |
| **SockJS** | Para WebSocket |
| **STOMP** | Protocolo WebSocket |

---

## 💻 Instalação

### Pré-requisitos

- Java 21+
- Maven 3.8+
- Docker e Docker Compose
- Git

### Clone o Repositório

```bash
git clone https://github.com/seu-usuario/zabbix-incident-service.git
cd zabbix-incident-service
```

### Instalação Local (sem Docker)

#### 1. Iniciar MySQL

```bash
docker run -d \
  --name mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=zabbix_incident_db \
  -e MYSQL_USER=incident_user \
  -e MYSQL_PASSWORD=incident_pass \
  -p 3306:3306 \
  mysql:8.0
```

#### 2. Iniciar RabbitMQ

```bash
docker run -d \
  --name rabbitmq \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3.13-management
```

#### 3. Compilar e Executar

```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

#### 4. Verificar Saúde

```bash
curl http://localhost:8080/health
```

---

## ⚙️ Configuração

### Variáveis de Ambiente

#### Desenvolvimento

```properties
# application-local.properties

server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/zabbix_incident_db
spring.datasource.username=incident_user
spring.datasource.password=incident_pass
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
app.cors.allowed-origins=http://localhost:4200,http://localhost:3000
app.websocket.allowed-origins=http://localhost:4200,http://localhost:3000
```

#### Produção

```bash
export DATABASE_URL=jdbc:mysql://mysql:3306/zabbix_incident_db
export DATABASE_USER=incident_user
export DATABASE_PASSWORD=<senha-segura>
export RABBITMQ_HOST=rabbitmq
export RABBITMQ_PORT=5672
export CORS_ALLOWED_ORIGINS=https://netmap.redelognet.com.br,https://api.incidents.redelognet.com.br
export WEBSOCKET_ALLOWED_ORIGINS=https://netmap.redelognet.com.br,https://api.incidents.redelognet.com.br
```

---

## 🔌 API Endpoints

### Criar Incidente

```http
POST /api/incidents
Content-Type: application/json

{
  "zabbixEventId": "28316936",
  "hostids": "10084",
  "title": "ICMP Ping Down",
  "severity": "High",
  "source": "zabbix"
}
```

**Response (201 Created):**

```json
{
  "status": 201,
  "message": "Incidente criado com sucesso",
  "data": {
    "id": 1,
    "zabbixEventId": "28316936",
    "title": "ICMP Ping Down",
    "severity": "High",
    "status": "OPEN",
    "createdAt": "2026-02-05T04:45:17",
    "updatedAt": "2026-02-05T04:45:17"
  },
  "timestamp": "2026-02-05T04:45:17"
}
```

### Listar Incidentes

```http
GET /api/incidents?page=0&size=10
```

### Buscar Incidente

```http
GET /api/incidents/1
GET /api/incidents/zabbix/28316936
GET /api/incidents/host/10084?page=0&size=10
```

### Atualizar Status

```http
PUT /api/incidents/1/status
Content-Type: application/json

{
  "status": "RESOLVED"
}
```

### Deletar Incidente

```http
DELETE /api/incidents/1
```

### Health Check

```http
GET /health
```

---

## 📡 WebSocket

### Conectar (React Frontend)

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const socket = new SockJS('https://incidents.redelognet.com.br/ws/incidents');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  stompClient.subscribe('/topic/incidents', (message) => {
    const incident = JSON.parse(message.body);
    console.log('Novo incidente:', incident);
  });
});
```

### Tópico

**`/topic/incidents`** - Recebe novos incidentes e atualizações

---

## 🐳 Docker

### Docker Compose

```bash
# Iniciar
docker-compose up -d

# Ver logs
docker-compose logs -f app

# Parar
docker-compose down
```

### Build

```bash
mvn clean package -DskipTests
docker build -t zabbix-incident-service:1.0.0 .
docker push seu-registry/zabbix-incident-service:1.0.0
```

---

## 🚀 Desenvolvimento

### Estrutura

```
src/main/java/br/com/cesaravb/zabbixincident/
├── api/controller/
├── application/service/
├── domain/entity/
├── domain/enums/
├── domain/repository/
├── dtos/request/
├── dtos/response/
├── mapper/
└── infrastructure/config/
```

### Git Workflow

```bash
git checkout -b feat/nova-funcionalidade
git add .
npm run commit  # Commitizen
git push origin feat/nova-funcionalidade
```

---

## 📦 Deployment

### Kubernetes

```bash
mvn clean package -DskipTests
docker build -t zabbix-incident-service:1.0.0 .
docker push seu-registry/zabbix-incident-service:1.0.0
kubectl apply -f k8s/deployment.yaml
```

---

## 🔍 Troubleshooting

### MySQL não conecta

```bash
docker ps | grep mysql
docker logs mysql
```

### RabbitMQ offline

```bash
docker ps | grep rabbitmq
docker logs rabbitmq
```

### WebSocket falha

```bash
curl http://localhost:8080/health
docker logs app
```

---

## 🤝 Contribuindo

1. Fork o repositório
2. Crie uma feature branch
3. Commit com Conventional Commits
4. Push e abra Pull Request

---

## 📄 Licença

MIT License - veja [LICENSE](LICENSE)

---

## 👥 Autores

- **César Augusto** - Desenvolvimento

---

**Status:** ✅ Production Ready  
**Versão:** 1.0.0  
**Última atualização:** 05/02/2026
# 🐳 Docker Compose - ZabbixIncidentService

Guia completo para executar a aplicação com Docker e Docker Compose.

## 📋 Índice

- [O que é Docker?](#o-que-é-docker)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Docker Compose File](#docker-compose-file)
- [Execução](#execução)
- [Comandos Úteis](#comandos-úteis)
- [Troubleshooting](#troubleshooting)

---

## 🐳 O que é Docker?

**Docker** é uma tecnologia que empacota sua aplicação com todas as dependências em um contêiner.

### Benefícios:

1. **"Funciona no meu computador"** - Mesma aplicação em qualquer lugar
2. **Sem conflitos de dependência** - Isolado
3. **Fácil deploy** - Mesmo contêiner em dev, test, prod
4. **Escalabilidade** - Múltiplos contêiners rodando

### Como funciona:

```
Seu Código
    ↓
Dockerfile (receita)
    ↓
Imagem Docker (template)
    ↓
Contêiner (aplicação rodando)
```

---

## 📦 Pré-requisitos

### 1. Instalar Docker

**Windows/Mac:**
- Download: [Docker Desktop](https://www.docker.com/products/docker-desktop)
- Instalar e abrir

**Linux (Ubuntu):**
```bash
sudo apt-get update
sudo apt-get install docker.io docker-compose
sudo usermod -aG docker $USER
```

### 2. Verificar Instalação

```bash
docker --version
docker-compose --version
```

**Esperado:**
```
Docker version 24.0.0
Docker Compose version 2.20.0
```

---

## 🔧 Instalação

### 1. Criar Dockerfile

Arquivo: `Dockerfile` (na raiz do projeto)

```dockerfile
# Usar imagem do Java 21
FROM eclipse-temurin:21-jdk as builder

# Diretório de trabalho
WORKDIR /app

# Copiar pom.xml e baixar dependências
COPY pom.xml .
RUN apt-get update && apt-get install -y maven
RUN mvn dependency:go-offline

# Copiar código-fonte
COPY src ./src

# Compilar e empacotar
RUN mvn clean package -DskipTests

# Usar imagem mais leve para runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar JAR da imagem anterior
COPY --from=builder /app/target/*.jar app.jar

# Expor porta
EXPOSE 8080

# Variáveis de ambiente
ENV SPRING_PROFILES_ACTIVE=docker

# Comando para iniciar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Criar .dockerignore

Arquivo: `.dockerignore` (na raiz do projeto)

```
.git
.gitignore
.idea
.vscode
target/
node_modules/
*.log
.env
.DS_Store
```

---

## 🔄 Docker Compose File

Arquivo: `docker-compose.yml` (na raiz do projeto)

```yaml
version: '3.8'

services:
  # ====================================
  # MySQL Database
  # ====================================
  mysql:
    image: mysql:8.0
    container_name: zabbix-mysql
    restart: unless-stopped
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: zabbix_incident_db
      MYSQL_USER: incident_user
      MYSQL_PASSWORD: incident_pass
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10
    networks:
      - zabbix-network

  # ====================================
  # RabbitMQ Message Broker
  # ====================================
  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    container_name: zabbix-rabbitmq
    restart: unless-stopped
    ports:
      - "5672:5672"      # AMQP port
      - "15672:15672"    # Management UI
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    healthcheck:
      test: rabbitmq-diagnostics ping
      timeout: 20s
      retries: 10
    networks:
      - zabbix-network

  # ====================================
  # ZabbixIncidentService Application
  # ====================================
  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: zabbix-incident-service
    restart: unless-stopped
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/zabbix_incident_db
      SPRING_DATASOURCE_USERNAME: incident_user
      SPRING_DATASOURCE_PASSWORD: incident_pass
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_RABBITMQ_PORT: 5672
      SPRING_RABBITMQ_USERNAME: guest
      SPRING_RABBITMQ_PASSWORD: guest
    volumes:
      - ./logs:/app/logs
    networks:
      - zabbix-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/health"]
      timeout: 20s
      retries: 10

# ====================================
# Volumes (dados persistentes)
# ====================================
volumes:
  mysql_data:
    driver: local
  rabbitmq_data:
    driver: local

# ====================================
# Networks (comunicação entre containers)
# ====================================
networks:
  zabbix-network:
    driver: bridge
```

### Explicação do arquivo:

**Services:** Cada serviço é um contêiner

```yaml
services:
  mysql:          # Nome do serviço
    image: ...    # Imagem Docker a usar
    ports: ...    # Portas expostas (host:container)
    environment:  # Variáveis de ambiente
    volumes: ...  # Dados persistentes
```

**Depends On:** Ordem de inicialização

```yaml
depends_on:
  mysql:
    condition: service_healthy  # Aguarda MySQL estar pronto
```

**Healthcheck:** Verifica se está funcionando

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/api/health"]
  timeout: 20s        # Tempo máximo
  retries: 10         # Tenta 10 vezes
```

---

## 🚀 Execução

### 1. Iniciar Todos os Serviços

```bash
docker-compose up
```

**Esperado:**
```
Creating zabbix-mysql ... done
Creating zabbix-rabbitmq ... done
Building app
...
[app] 2025-02-04 10:00:00 Started ZabbixIncidentServiceApplication
```

### 2. Executar em Background

```bash
docker-compose up -d
```

### 3. Verificar Status

```bash
docker-compose ps
```

**Esperado:**
```
NAME                        STATUS
zabbix-mysql               Up (healthy)
zabbix-rabbitmq            Up (healthy)
zabbix-incident-service    Up (healthy)
```

### 4. Ver Logs

```bash
# Todos os serviços
docker-compose logs -f

# Apenas um serviço
docker-compose logs -f app

# Últimas 100 linhas
docker-compose logs --tail=100
```

### 5. Parar Serviços

```bash
docker-compose down
```

### 6. Parar e Remover Volumes

```bash
docker-compose down -v
```

---

## 📋 Arquivo de Configuração Docker

Crie também: `application-docker.properties`

```properties
# ============================================
# Server Configuration
# ============================================
server.port=8080

# ============================================
# Database - MySQL
# ============================================
spring.datasource.url=jdbc:mysql://mysql:3306/zabbix_incident_db
spring.datasource.username=incident_user
spring.datasource.password=incident_pass
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ============================================
# JPA/Hibernate Configuration
# ============================================
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# ============================================
# RabbitMQ Configuration
# ============================================
spring.rabbitmq.host=rabbitmq
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.virtual-host=/

# ============================================
# Application Configuration
# ============================================
app.rabbitmq.exchange.incident=zabbix.incident.exchange
app.rabbitmq.queue.incident=zabbix.incident.queue
app.rabbitmq.routing-key.incident=incident.created

# ============================================
# Logging
# ============================================
logging.level.root=INFO
logging.level.br.com.cesaravb.zabbixincident=DEBUG
```

---

## 🔧 Comandos Úteis

### Reconstruir Imagens

```bash
docker-compose build --no-cache
```

### Entrar no Contêiner

```bash
# Entrar no MySQL
docker-compose exec mysql mysql -u root -p

# Entrar no App
docker-compose exec app /bin/bash
```

### Ver Porta Usada

```bash
docker-compose port app 8080
```

### Remover Imagens

```bash
docker-compose down --rmi all
```

### Ver Detalhes do Contêiner

```bash
docker-compose inspect app
```

---

## 🌐 Acessar Serviços

### Aplicação (Spring Boot)

```
http://localhost:8080/api/health
```

### MySQL

```bash
mysql -h localhost -u incident_user -p zabbix_incident_db
```

**Credenciais:**
- User: `incident_user`
- Password: `incident_pass`
- Database: `zabbix_incident_db`

### RabbitMQ Management UI

```
http://localhost:15672
```

**Credenciais:**
- Username: `guest`
- Password: `guest`

**O que você vê:**
- Exchanges criadas
- Filas criadas
- Mensagens na fila
- Conexões ativas

---

## 🧪 Testar com Docker

### 1. Criar um Incidente

```bash
curl -X POST http://localhost:8080/api/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "zabbixEventId": "evt_docker_test",
    "title": "Teste Docker",
    "severity": "HIGH",
    "source": "docker-test"
  }'
```

### 2. Listar Incidentes

```bash
curl http://localhost:8080/api/incidents
```

### 3. Verificar Saúde

```bash
curl http://localhost:8080/api/health
```

### 4. Ver Mensagens no RabbitMQ

1. Abra `http://localhost:15672`
2. Login com `guest/guest`
3. Vá em "Queues"
4. Procure por `zabbix.incident.queue`
5. Veja as mensagens

---

## 📊 Problema & Solução

### Problema: "Port 3306 already in use"

**Solução 1:** Usar porta diferente

```yaml
ports:
  - "3307:3306"  # Host:Container
```

Então conectar em `localhost:3307`

**Solução 2:** Parar contêiner anterior

```bash
docker-compose down
docker ps  # Ver se ainda existe
docker stop container_id
```

### Problema: "Cannot connect to RabbitMQ"

**Solução:**

```bash
# Verificar se está rodando
docker-compose ps

# Ver logs
docker-compose logs rabbitmq

# Reiniciar
docker-compose restart rabbitmq
```

### Problema: Aplicação não conecta ao MySQL

**Solução:**

```bash
# Entrar no contêiner
docker-compose exec app /bin/bash

# Tentar conectar ao MySQL
apt-get update && apt-get install -y mysql-client
mysql -h mysql -u incident_user -p zabbix_incident_db

# Se der erro, verificar logs
docker-compose logs mysql
```

### Problema: "health check failed"

**Solução:** Aumentar timeout nos healthchecks

```yaml
healthcheck:
  timeout: 30s    # De 20s para 30s
  retries: 15     # De 10 para 15
```

---

## 🚀 Workflow Recomendado

### Desenvolvimento Local

```bash
# 1. Iniciar tudo
docker-compose up -d

# 2. Verificar status
docker-compose ps

# 3. Desenvolver/Testar
# ... fazer mudanças

# 4. Parar
docker-compose down
```

### Rebuild da Aplicação

```bash
# Se mudou código Java
docker-compose build app
docker-compose up -d app
```

### Limpar Tudo (Resetar)

```bash
docker-compose down -v
docker-compose up
```

---

## 📈 Escalando com Docker

### Múltiplas Instâncias da Aplicação

```yaml
services:
  app:
    ...
    deploy:
      replicas: 3  # 3 instâncias
```

### Load Balancer com Nginx

Arquivo adicional: `docker-compose.nginx.yml`

```yaml
services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - app
```

---

## 📝 .env File (Opcional)

Para não hardcoding senhas, crie `.env`:

```bash
MYSQL_PASSWORD=seu_password
RABBITMQ_USER=seu_user
RABBITMQ_PASSWORD=seu_password
```

Referencie no `docker-compose.yml`:

```yaml
environment:
  MYSQL_PASSWORD: ${MYSQL_PASSWORD}
```

Execute:
```bash
docker-compose --env-file .env up
```

---

## 🎓 Próximos Passos

1. **Monitoramento:** Adicionar Prometheus + Grafana
2. **Logs Centralizados:** ELK Stack (Elasticsearch, Logstash, Kibana)
3. **CI/CD:** GitHub Actions para deploy automático
4. **Kubernetes:** Ir além de Docker Compose


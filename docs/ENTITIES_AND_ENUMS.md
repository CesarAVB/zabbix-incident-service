# 🏛️ Entidades e Enums - ZabbixIncidentService

Documentação detalhada das entidades do domínio, enums e repositórios da aplicação.

## 📋 Índice

- [Incident (Entidade)](#incident-entidade)
- [IncidentStatus (Enum)](#incidentstatus-enum)
- [SeverityLevel (Enum)](#severiylevel-enum)
- [IncidentRepository](#incidentrepository)

---

## 🏗️ Incident (Entidade)

**Localização:** `domain/entity/Incident.java`

**Responsabilidades:**
- Representar um incidente no banco de dados
- Controlar quais informações são persistidas
- Gerenciar timestamps automaticamente

**Anotações principais:**
- `@Entity` - Marca como entidade JPA
- `@Table(name = "incidents")` - Nome da tabela
- `@Getter @Setter` - Lombok para getters/setters
- `@NoArgsConstructor @AllArgsConstructor @Builder` - Construtores

### Campos da Entidade

#### Identificação
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```
- Chave primária auto-incremento
- Tipo: `Long`

```java
@Column(nullable = false, unique = true)
private String zabbixEventId;
```
- ID único do evento Zabbix
- Não pode ser nulo
- Deve ser único na tabela

#### Informações Básicas
```java
@Column(nullable = false)
private String title;
```
- Título do incidente
- Campo obrigatório

```java
@Column(columnDefinition = "TEXT")
private String description;
```
- Descrição detalhada
- Campo opcional
- Tipo TEXT (ilimitado)

#### Severidade e Status
```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private SeverityLevel severity;
```
- Nível de severidade
- Enum armazenado como string
- Campo obrigatório

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private IncidentStatus status;
```
- Estado atual do incidente
- Enum armazenado como string
- Campo obrigatório

#### Origem
```java
@Column(nullable = false)
private String source;
```
- Origem do incidente (ex: "zabbix")
- Campo obrigatório

#### Timestamps
```java
@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;

@Column(name = "updated_at")
private LocalDateTime updatedAt;
```
- `createdAt` - Data de criação (não atualizável)
- `updatedAt` - Data da última atualização

#### Campos Zabbix Específicos
```java
@Column(name = "host_ids", nullable = true)
private String hostids;

@Column(nullable = true)
private String host;

@Column(name = "host_ip", nullable = true)
private String hostIp;

@Column(nullable = true)
private String item;

@Column(name = "item_key", nullable = true)
private String itemKey;

@Column(name = "trigger_name", nullable = true)
private String trigger;

@Column(name = "alert_message", columnDefinition = "TEXT", nullable = true)
private String alertMessage;

@Column(name = "event_name", nullable = true)
private String eventName;

@Column(name = "event_opdata", nullable = true)
private String eventOpdata;

@Column(name = "url_zabbix", columnDefinition = "TEXT", nullable = true)
private String urlZabbix;

@Column(nullable = true)
private String valor;
```
- Campos específicos do Zabbix
- Mapeiam informações do evento/monitoramento
- Todos opcionais

### Métodos de Callback

#### `@PrePersist`
```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}
```
- Executado antes de inserir
- Define datas de criação e atualização

#### `@PreUpdate`
```java
@PreUpdate
protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
```
- Executado antes de atualizar
- Atualiza timestamp de modificação

### Exemplo de Uso

**Criando um incidente:**
```java
Incident incident = Incident.builder()
    .zabbixEventId("evt_12345")
    .title("CPU alta no servidor")
    .description("Utilização acima de 90%")
    .severity(SeverityLevel.CRITICAL)
    .status(IncidentStatus.OPEN)
    .source("zabbix")
    .host("servidor-prod-01")
    .build();
```

**Estrutura da Tabela MySQL:**
```sql
CREATE TABLE incidents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    zabbix_event_id VARCHAR(255) NOT NULL UNIQUE,
    host_ids VARCHAR(255),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    alert_message TEXT,
    event_name VARCHAR(255),
    event_opdata VARCHAR(255),
    host VARCHAR(255),
    host_ip VARCHAR(255),
    item VARCHAR(255),
    item_key VARCHAR(255),
    trigger VARCHAR(255),
    url_zabbix TEXT,
    valor VARCHAR(255),
    severity VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    source VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

---

## 📊 IncidentStatus (Enum)

**Localização:** `domain/enums/IncidentStatus.java`

**Responsabilidades:**
- Definir os possíveis estados de um incidente
- Controlar workflow de resolução

**Valores:**
```java
public enum IncidentStatus {
    OPEN,        // Incidente recém-criado, aguardando ação
    IN_PROGRESS, // Em análise/tratamento
    RESOLVED,    // Problema resolvido
    CLOSED       // Incidente finalizado
}
```

**Workflow típico:**
```
OPEN → IN_PROGRESS → RESOLVED → CLOSED
```

**Descrição de cada status:**

### OPEN
- **Significado:** Incidente recém-reportado
- **Ações possíveis:** Iniciar investigação, alterar para IN_PROGRESS
- **Cor típica:** Vermelho/alaranjado

### IN_PROGRESS
- **Significado:** Equipe trabalhando na resolução
- **Ações possíveis:** Resolver problema, alterar para RESOLVED
- **Cor típica:** Amarelo

### RESOLVED
- **Significado:** Problema foi corrigido
- **Ações possíveis:** Fechar incidente, alterar para CLOSED
- **Cor típica:** Verde

### CLOSED
- **Significado:** Incidente finalizado
- **Ações possíveis:** Nenhuma (status final)
- **Cor típica:** Cinza

---

## 🚨 SeverityLevel (Enum)

**Localização:** `domain/enums/SeverityLevel.java`

**Responsabilidades:**
- Classificar criticidade dos incidentes
- Priorizar tratamento baseado na severidade

**Valores (ordenados por criticidade):**
```java
public enum SeverityLevel {
    CRITICAL,  // Sistema indisponível, impacto crítico
    HIGH,      // Alto impacto, requer atenção imediata
    MEDIUM,    // Impacto moderado
    LOW,       // Baixo impacto
    INFO       // Informativo, sem impacto
}
```

**Descrição de cada nível:**

### CRITICAL
- **Impacto:** Sistema completamente indisponível
- **Exemplos:** Site fora do ar, database inoperante
- **SLA:** Resolução em minutos/horas
- **Cor típica:** Vermelho escuro

### HIGH
- **Impacto:** Funcionalidades críticas afetadas
- **Exemplos:** Lentidão extrema, falhas intermitentes
- **SLA:** Resolução em horas
- **Cor típica:** Vermelho

### MEDIUM
- **Impacto:** Algumas funcionalidades afetadas
- **Exemplos:** Relatórios lentos, avisos de sistema
- **SLA:** Resolução em dias
- **Cor típica:** Laranja

### LOW
- **Impacto:** Mínimo ou nenhum impacto no negócio
- **Exemplos:** Avisos de monitoramento, pequenas anomalias
- **SLA:** Resolução quando possível
- **Cor típica:** Amarelo

### INFO
- **Impacto:** Informativo apenas
- **Exemplos:** Métricas fora do padrão, notificações
- **SLA:** Não aplicável
- **Cor típica:** Azul

---

## 💾 IncidentRepository

**Localização:** `domain/repository/IncidentRepository.java`

**Responsabilidades:**
- Interface de acesso aos dados de incidentes
- Fornecer métodos de consulta ao banco

**Extende:** `JpaRepository<Incident, Long>`
- Herda métodos CRUD básicos
- Tipo da entidade: `Incident`
- Tipo da chave: `Long`

### Métodos Herdados (JpaRepository)

#### Básicos
- `save(entity)` - Salvar/atualizar
- `findById(id)` - Buscar por ID
- `findAll()` - Buscar todos
- `deleteById(id)` - Deletar por ID
- `existsById(id)` - Verificar existência

#### Com Paginação
- `findAll(Pageable pageable)` - Buscar com paginação
- `findAll(Sort sort)` - Buscar ordenado

### Métodos Customizados

#### `findByZabbixEventId(String zabbixEventId)`
```java
Optional<Incident> findByZabbixEventId(String zabbixEventId);
```
- **Propósito:** Evitar duplicatas de eventos Zabbix
- **Retorno:** `Optional<Incident>` (pode não existir)
- **Uso:** Verificar se evento já foi processado

**Exemplo de uso:**
```java
Optional<Incident> existing = repository.findByZabbixEventId("evt_123");
if (existing.isPresent()) {
    // Evento já processado
} else {
    // Criar novo incidente
}
```

### Consultas Derivadas Possíveis

O Spring Data JPA permite criar consultas por convenção de nomes:

```java
// Possíveis métodos (não implementados ainda)
List<Incident> findByStatus(IncidentStatus status);
List<Incident> findBySeverity(SeverityLevel severity);
List<Incident> findBySource(String source);
List<Incident> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
```

### Exemplo de Uso Completo

```java
@Service
public class IncidentService {

    @Autowired
    private IncidentRepository repository;

    public List<Incident> getOpenIncidents() {
        return repository.findAll().stream()
            .filter(i -> i.getStatus() == IncidentStatus.OPEN)
            .collect(Collectors.toList());
    }

    public boolean eventAlreadyProcessed(String zabbixEventId) {
        return repository.findByZabbixEventId(zabbixEventId).isPresent();
    }
}
```

---

## 🔗 Relacionamentos e Dependências

### Incident ↔ IncidentRepository
- Repository acessa Incident via JPA
- Incident é mapeado para tabela `incidents`

### Incident ↔ Enums
- `IncidentStatus` controla workflow
- `SeverityLevel` define prioridade

### Repository ↔ Services
- `IncidentService` usa `IncidentRepository`
- Injeção via `@Autowired` ou construtor

### Entidade ↔ DTOs
- Mapeamento via `IncidentMapper`
- Separação entre domínio e apresentação</content>
<parameter name="filePath">D:\Documentos\PROGRAMAÇÃO\PROJETOS\BACKEND\zabbix-incident-service\docs\ENTITIES_AND_ENUMS.md
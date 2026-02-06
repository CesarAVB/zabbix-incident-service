package br.com.cesaravb.zabbixincident.mapper;

import br.com.cesaravb.zabbixincident.domain.entity.Incident;
import br.com.cesaravb.zabbixincident.dtos.request.CreateIncidentRequest;
import br.com.cesaravb.zabbixincident.dtos.response.IncidentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper responsável por converter entre DTOs e Entidades de Incidente.
 *
 * Fluxo completo de processamento:
 *
 * FLUXO 1: Criar Incidente (CreateIncidentRequest → Incident)
 * ───────────────────────────────────────────────────────────
 * 1. O Zabbix envia dados via POST /api/incidents (JSON)
 * 2. O Controller recebe como CreateIncidentRequest (DTO)
 * 3. O Service chama: mapper.toEntity(createIncidentRequest)
 * 4. O MapStruct converte automaticamente:
 *    - Copia todos os campos do DTO para a Entity
 *    - Define status = "OPEN" automaticamente
 *    - Define createdAt e updatedAt como null (JPA preenche na persistência)
 *    - Converte enums automaticamente
 * 5. O Service recebe um objeto Incident pronto para salvar
 * 6. O Repository salva no MySQL
 * 7. O Service publica na fila RabbitMQ
 *
 * FLUXO 2: Retornar Incidente (Incident → IncidentResponse)
 * ──────────────────────────────────────────────────────────
 * 1. O Service recupera um Incident do banco de dados
 * 2. O Service chama: mapper.toResponse(incident)
 * 3. O MapStruct converte automaticamente:
 *    - Copia todos os campos da Entity para o DTO
 *    - Converte enums para String automaticamente
 *    - Preserva todos os dados incluindo IDs e timestamps
 * 4. O Controller recebe um IncidentResponse pronto para enviar
 * 5. O IncidentResponse é serializado em JSON
 * 6. O cliente recebe a resposta formatada
 */
@Mapper(componentModel = "spring")
public interface IncidentMapper {

    // ====================================
    // # toEntity - Converte CreateIncidentRequest para Incident
    // ====================================
    /**
     * Converte um DTO de requisição em uma entidade de domínio.
     *
     * Mapeamentos especiais:
     * • id: ignorado (será gerado pelo banco de dados)
     * • status: sempre setado como "OPEN" (novo incidente começa aberto)
     * • createdAt: ignorado (será preenchido por @PrePersist)
     * • updatedAt: ignorado (será preenchido por @PrePersist)
     * • severity: convertido automaticamente de String para SeverityLevel (enum)
     * • Todos os outros campos: copiados automaticamente
     *
     * @param request DTO com dados vindos do Zabbix
     * @return Entity pronta para ser salva no banco de dados
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "OPEN")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "severity", target = "severity")
    @Mapping(source = "hostids", target = "hostids")
    @Mapping(source = "alertMessage", target = "alertMessage")
    @Mapping(source = "eventName", target = "eventName")
    @Mapping(source = "eventOpdata", target = "eventOpdata")
    @Mapping(source = "host", target = "host")
    @Mapping(source = "hostIp", target = "hostIp")
    @Mapping(source = "item", target = "item")
    @Mapping(source = "itemKey", target = "itemKey")
    @Mapping(source = "trigger", target = "trigger")
    @Mapping(source = "urlZabbix", target = "urlZabbix")
    @Mapping(source = "valor", target = "valor")
    Incident toEntity(CreateIncidentRequest request);

    // ====================================
    // # toResponse - Converte Incident para IncidentResponse
    // ====================================
    /**
     * Converte uma entidade de domínio em um DTO de resposta.
     *
     * Mapeamentos especiais:
     * • severity: convertido automaticamente de SeverityLevel (enum) para String
     * • status: convertido automaticamente de IncidentStatus (enum) para String
     * • Todos os outros campos: copiados automaticamente
     * • Campos internos da Entity não presentes no DTO não são expostos
     *
     * @param incident Entity recuperada do banco de dados
     * @return DTO pronto para ser serializado em JSON e enviado ao cliente
     */
    @Mapping(source = "severity", target = "severity")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "hostids", target = "hostids")
    @Mapping(source = "alertMessage", target = "alertMessage")
    @Mapping(source = "eventName", target = "eventName")
    @Mapping(source = "eventOpdata", target = "eventOpdata")
    @Mapping(source = "host", target = "host")
    @Mapping(source = "hostIp", target = "hostIp")
    @Mapping(source = "item", target = "item")
    @Mapping(source = "itemKey", target = "itemKey")
    @Mapping(source = "trigger", target = "trigger")
    @Mapping(source = "urlZabbix", target = "urlZabbix")
    @Mapping(source = "valor", target = "valor")
    IncidentResponse toResponse(Incident incident);
}
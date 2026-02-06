package br.com.cesaravb.zabbixincident.application.service;

import br.com.cesaravb.zabbixincident.domain.entity.Incident;
import br.com.cesaravb.zabbixincident.domain.enums.IncidentStatus;
import br.com.cesaravb.zabbixincident.domain.repository.IncidentRepository;
import br.com.cesaravb.zabbixincident.dtos.request.CreateIncidentRequest;
import br.com.cesaravb.zabbixincident.dtos.response.IncidentResponse;
import br.com.cesaravb.zabbixincident.mapper.IncidentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por gerenciar incidentes.
 *
 * Fluxo:
 * 1. Recebe CreateIncidentRequest do Controller
 * 2. Valida e converte para Entity
 * 3. Salva no banco de dados
 * 4. PUBLICA na fila RabbitMQ
 * 5. Retorna IncidentResponse
 *
 * Logger rastreia cada etapa do processo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentService {

    // ====================================
    // # Dependências
    // ====================================
    private final IncidentRepository incidentRepository;
    private final IncidentMapper incidentMapper;
    private final RabbitTemplate rabbitTemplate;

    // ====================================
    // # Constantes RabbitMQ
    // ====================================
    private static final String EXCHANGE = "zabbix.incident.exchange";
    private static final String ROUTING_KEY = "incident.created";

    // ====================================
    // # createIncident - Criar novo incidente
    // ====================================
    /**
     * Cria um novo incidente a partir dos dados recebidos do Zabbix.
     *
     * Fluxo com Logger:
     * 1. ✅ INFO - Recebido request
     * 2. ✅ DEBUG - Validando dados
     * 3. ✅ DEBUG - Convertendo para Entity
     * 4. ✅ INFO - Salvando no BD
     * 5. ✅ DEBUG - Salvo com ID
     * 6. ✅ INFO - PUBLICANDO na fila RabbitMQ
     * 7. ✅ DEBUG - Enviado para fila
     * 8. ✅ INFO - Retornando response
     *
     * @param request DTO com dados do Zabbix
     * @return IncidentResponse criado
     * @throws RuntimeException se zabbixEventId já existe
     */
    public IncidentResponse createIncident(CreateIncidentRequest request) {
        log.info("=".repeat(80));
        log.info("📥 [CREATE INCIDENT] Recebido request do Zabbix");
        log.info("   - Zabbix Event ID: {}", request.zabbixEventId());
        log.info("   - Título: {}", request.title());
        log.info("   - Severidade: {}", request.severity());
        log.info("   - Host: {}", request.host());
        
        // ====================================
        // # Validar duplicação
        // ====================================
        try {
            var existente = incidentRepository.findByZabbixEventId(request.zabbixEventId());
            if (existente.isPresent()) {
                log.warn("⚠️  [CREATE INCIDENT] Incidente com ID {} já existe", request.zabbixEventId());
                throw new RuntimeException("Incidente com zabbixEventId " + request.zabbixEventId() + " já existe");
            }
            log.debug("✅ [CREATE INCIDENT] Validação: Nenhum duplicado encontrado");
        } catch (Exception e) {
            log.error("❌ [CREATE INCIDENT] Erro ao validar duplicação", e);
            throw e;
        }
        
        // ====================================
        // # Converter para Entity
        // ====================================
        log.debug("🔄 [CREATE INCIDENT] Convertendo CreateIncidentRequest → Incident Entity");
        Incident incident = incidentMapper.toEntity(request);
        log.debug("   - Status padrão setado: {}", incident.getStatus());
        log.debug("✅ [CREATE INCIDENT] Conversão concluída");
        
        // ====================================
        // # Salvar no banco de dados
        // ====================================
        log.info("💾 [CREATE INCIDENT] Salvando incidente no MySQL");
        Incident saved = incidentRepository.save(incident);
        log.info("✅ [CREATE INCIDENT] Salvo com sucesso!");
        log.info("   - ID gerado: {}", saved.getId());
        log.info("   - Created At: {}", saved.getCreatedAt());
        log.info("   - Updated At: {}", saved.getUpdatedAt());
        
        // ====================================
        // # PUBLICAR na fila RabbitMQ
        // ====================================
        log.info("📤 [PUBLISH TO RABBITMQ] Publicando incidente na fila");
        log.info("   - Exchange: {}", EXCHANGE);
        log.info("   - Routing Key: {}", ROUTING_KEY);
        log.info("   - Incident ID: {}", saved.getId());
        log.debug("   - Payload: {}", saved);
        
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, saved);
            log.info("✅ [PUBLISH TO RABBITMQ] Publicado com sucesso!");
            log.info("   - Mensagem enfileirada em: zabbix.incident.queue");
        } catch (Exception e) {
            log.error("❌ [PUBLISH TO RABBITMQ] Erro ao publicar: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao publicar na fila RabbitMQ", e);
        }
        
        // ====================================
        // # Retornar Response
        // ====================================
        log.debug("🔄 [CREATE INCIDENT] Convertendo Incident → IncidentResponse");
        IncidentResponse response = incidentMapper.toResponse(saved);
        log.info("✅ [CREATE INCIDENT] Response montado e retornado");
        log.info("=".repeat(80));
        
        return response;
    }

    // ====================================
    // # getIncidentById - Buscar por ID
    // ====================================
    public IncidentResponse getIncidentById(Long id) {
        log.info("🔍 [GET INCIDENT] Buscando incidente com ID: {}", id);
        
        Incident incident = incidentRepository.findById(id).orElseThrow(() -> {
                    log.error("❌ [GET INCIDENT] Incidente com ID {} não encontrado", id);
                    return new RuntimeException("Incidente não encontrado");
                });
        
        log.info("✅ [GET INCIDENT] Encontrado: {}", incident.getTitle());
        return incidentMapper.toResponse(incident);
    }

    // ====================================
    // # getIncidentByZabbixEventId - Buscar por Zabbix Event ID
    // ====================================
    public IncidentResponse getIncidentByZabbixEventId(String zabbixEventId) {
        log.info("🔍 [GET INCIDENT] Buscando incidente com Zabbix Event ID: {}", zabbixEventId);
        
        Incident incident = incidentRepository.findByZabbixEventId(zabbixEventId).orElseThrow(() -> {
                    log.error("❌ [GET INCIDENT] Incidente com Zabbix Event ID {} não encontrado", zabbixEventId);
                    return new RuntimeException("Incidente não encontrado");
                });
        
        log.info("✅ [GET INCIDENT] Encontrado: {} (ID: {})", incident.getTitle(), incident.getId());
        return incidentMapper.toResponse(incident);
    }

    // ====================================
    // # getAllIncidents - Listar todos
    // ====================================
    public Page<IncidentResponse> getAllIncidents(Pageable pageable) {
        log.info("📋 [LIST INCIDENTS] Listando incidentes com paginação");
        log.info("   - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Incident> incidents = incidentRepository.findAll(pageable);
        log.info("✅ [LIST INCIDENTS] Retornados {} incidentes de {}", incidents.getNumberOfElements(), incidents.getTotalElements());
        
        return incidents.map(incidentMapper::toResponse);
    }

    // ====================================
    // # getIncidentByHostids - Buscar por Host ID
    // ====================================
    public Page<IncidentResponse> getIncidentByHostids(String hostids, Pageable pageable) {
        log.info("🔍 [GET INCIDENT BY HOST] Buscando incidentes do host: {}", hostids);
        log.info("   - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Incident> incidents = incidentRepository.findByHostids(hostids, pageable);
        log.info("✅ [GET INCIDENT BY HOST] Encontrados {} incidentes", incidents.getTotalElements());
        
        return incidents.map(incidentMapper::toResponse);
    }

    // ====================================
    // # updateIncidentStatus - Atualizar status
    // ====================================
    public IncidentResponse updateIncidentStatus(Long id, String newStatus) {
        log.info("=".repeat(80));
        log.info("🔄 [UPDATE STATUS] Atualizando status do incidente");
        log.info("   - ID: {}", id);
        log.info("   - Novo Status: {}", newStatus);
        
        Incident incident = incidentRepository.findById(id).orElseThrow(() -> {
                    log.error("❌ [UPDATE STATUS] Incidente com ID {} não encontrado", id);
                    return new RuntimeException("Incidente não encontrado");
                });
        
        IncidentStatus oldStatus = incident.getStatus();
        log.debug("   - Status Anterior: {}", oldStatus);
        
        incident.setStatus(IncidentStatus.valueOf(newStatus));
        log.debug("✅ [UPDATE STATUS] Status convertido para Enum");
        
        Incident updated = incidentRepository.save(incident);
        log.info("✅ [UPDATE STATUS] Salvo no banco com novo status");
        
        // ====================================
        // # PUBLICAR alteração na fila
        // ====================================
        log.info("📤 [PUBLISH TO RABBITMQ] Publicando atualização na fila");
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, updated);
            log.info("✅ [PUBLISH TO RABBITMQ] Atualização publicada com sucesso!");
        } catch (Exception e) {
            log.error("❌ [PUBLISH TO RABBITMQ] Erro ao publicar atualização", e);
        }
        
        log.info("=".repeat(80));
        return incidentMapper.toResponse(updated);
    }

    // ====================================
    // # deleteIncident - Deletar
    // ====================================
    public void deleteIncident(Long id) {
        log.info("🗑️  [DELETE INCIDENT] Deletando incidente com ID: {}", id);
        
        Incident incident = incidentRepository.findById(id).orElseThrow(() -> {
                    log.error("❌ [DELETE INCIDENT] Incidente com ID {} não encontrado", id);
                    return new RuntimeException("Incidente não encontrado");
                });
        
        incidentRepository.delete(incident);
        log.info("✅ [DELETE INCIDENT] Deletado com sucesso!");
    }
}
package br.com.cesaravb.zabbixincident.application.service;

import br.com.cesaravb.zabbixincident.domain.entity.Incident;
import br.com.cesaravb.zabbixincident.domain.enums.IncidentStatus;
import br.com.cesaravb.zabbixincident.domain.repository.IncidentRepository;
import br.com.cesaravb.zabbixincident.dtos.request.CreateIncidentRequest;
import br.com.cesaravb.zabbixincident.dtos.request.UpdateIncidentStatusRequest;
import br.com.cesaravb.zabbixincident.dtos.response.IncidentResponse;
import br.com.cesaravb.zabbixincident.mapper.IncidentMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentMapper incidentMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.incident}")
    private String incidentExchange;

    @Value("${app.rabbitmq.routing-key.incident}")
    private String incidentRoutingKey;

    public IncidentService(IncidentRepository incidentRepository, IncidentMapper incidentMapper, RabbitTemplate rabbitTemplate) {
        this.incidentRepository = incidentRepository;
        this.incidentMapper = incidentMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    // ====================================
    // # createIncident - Cria novo incidente e publica na fila RabbitMQ
    // ====================================
    public IncidentResponse createIncident(CreateIncidentRequest request) {
        Incident incident = incidentMapper.toEntity(request);
        Incident savedIncident = incidentRepository.save(incident);
        rabbitTemplate.convertAndSend(incidentExchange, incidentRoutingKey, savedIncident);
        return incidentMapper.toResponse(savedIncident);
    }

    // ====================================
    // # getIncidentById - Busca incidente por ID
    // ====================================
    @Transactional(readOnly = true)
    public Optional<IncidentResponse> getIncidentById(Long id) {
        return incidentRepository.findById(id).map(incidentMapper::toResponse);
    }

    // ====================================
    // # getIncidentByZabbixEventId - Busca incidente por ID do evento Zabbix
    // ====================================
    @Transactional(readOnly = true)
    public Optional<IncidentResponse> getIncidentByZabbixEventId(String zabbixEventId) {
        return incidentRepository.findByZabbixEventId(zabbixEventId).map(incidentMapper::toResponse);
    }

    // ====================================
    // # getAllIncidents - Busca todos os incidentes com paginação
    // ====================================
    @Transactional(readOnly = true)
    public Page<IncidentResponse> getAllIncidents(Pageable pageable) {
        return incidentRepository.findAll(pageable).map(incidentMapper::toResponse);
    }

    // ====================================
    // # updateIncidentStatus - Atualiza status do incidente
    // ====================================
    public IncidentResponse updateIncidentStatus(Long id, UpdateIncidentStatusRequest request) {
        Incident incident = incidentRepository.findById(id).orElseThrow(() -> new RuntimeException("Incidente não encontrado com ID: " + id));
        IncidentStatus newStatus = IncidentStatus.valueOf(request.status());
        incident.setStatus(newStatus);
        Incident updatedIncident = incidentRepository.save(incident);
        rabbitTemplate.convertAndSend(incidentExchange, incidentRoutingKey, updatedIncident);
        return incidentMapper.toResponse(updatedIncident);
    }

    // ====================================
    // # deleteIncident - Deleta incidente por ID
    // ====================================
    public void deleteIncident(Long id) {
        if (!incidentRepository.existsById(id)) {
            throw new RuntimeException("Incidente não encontrado com ID: " + id);
        }
        incidentRepository.deleteById(id);
    }
}
package br.com.cesaravb.zabbixincident.application.service;

import br.com.cesaravb.zabbixincident.domain.entity.Incident;
import br.com.cesaravb.zabbixincident.dtos.response.IncidentResponse;
import br.com.cesaravb.zabbixincident.mapper.IncidentMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final IncidentMapper incidentMapper;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate, IncidentMapper incidentMapper) {
        this.messagingTemplate = messagingTemplate;
        this.incidentMapper = incidentMapper;
    }

    // ====================================
    // # notifyIncidentCreated - Envia notificação de novo incidente via WebSocket
    // ====================================
    public void notifyIncidentCreated(Incident incident) {
        IncidentResponse incidentResponse = incidentMapper.toResponse(incident);
        messagingTemplate.convertAndSend("/topic/incidents", incidentResponse);
    }

    // ====================================
    // # notifyIncidentUpdated - Envia notificação de incidente atualizado via WebSocket
    // ====================================
    public void notifyIncidentUpdated(Incident incident) {
        IncidentResponse incidentResponse = incidentMapper.toResponse(incident);
        messagingTemplate.convertAndSend("/topic/incidents", incidentResponse);
    }

    // ====================================
    // # notifyIncidentDeleted - Envia notificação de incidente deletado via WebSocket
    // ====================================
    public void notifyIncidentDeleted(Long incidentId) {
        messagingTemplate.convertAndSend("/topic/incidents/deleted", incidentId);
    }
}
package br.com.cesaravb.zabbixincident.application.service;

import br.com.cesaravb.zabbixincident.domain.entity.Incident;
import br.com.cesaravb.zabbixincident.dtos.response.IncidentResponse;
import br.com.cesaravb.zabbixincident.mapper.IncidentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por enviar notificações em tempo real via WebSocket.
 *
 * Fluxo:
 * 1. IncidentListener chama este serviço
 * 2. Converte Incident em IncidentResponse
 * 3. Envia para topic /topic/incidents
 * 4. STOMP distribui para todos os clientes inscritos
 * 5. Frontend React recebe em tempo real
 *
 * Logger rastreia conectividade e envio de mensagens
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    // ====================================
    // # Dependências
    // ====================================
    private final SimpMessagingTemplate messagingTemplate;
    private final IncidentMapper incidentMapper;

    // ====================================
    // # Constantes WebSocket
    // ====================================
    private static final String TOPIC = "/topic/incidents";
    private static final String ENDPOINT = "/ws/incidents";

    // ====================================
    // # notifyIncidentCreated - Notificar novo incidente
    // ====================================
    /**
     * Envia notificação de novo incidente para todos os clientes conectados.
     *
     * Fluxo com Logger:
     * 1. ✅ INFO - Iniciando envio
     * 2. ✅ DEBUG - Convertendo para DTO
     * 3. ✅ INFO - Enviando para topic
     * 4. ✅ DEBUG - Monitorando sessões ativas
     * 5. ✅ INFO - Concluído com sucesso
     *
     * @param incident Entidade que será enviada para frontend
     */
    public void notifyIncidentCreated(Incident incident) {
        log.info("=".repeat(80));
        log.info("🔔 [WEBSOCKET SEND] Preparando envio de notificação");
        log.info("   - Topic: {}", TOPIC);
        log.info("   - Endpoint: {}", ENDPOINT);
        log.info("   - Protocolo: STOMP");
        
        // ====================================
        // # Converter para Response DTO
        // ====================================
        log.debug("🔄 [WEBSOCKET SEND] Convertendo Incident → IncidentResponse");
        IncidentResponse response = incidentMapper.toResponse(incident);
        log.debug("✅ [WEBSOCKET SEND] Conversão concluída");
        log.debug("   - Response ID: {}", response.id());
        log.debug("   - Response Title: {}", response.title());
        
        // ====================================
        // # Enviar via STOMP/WebSocket
        // ====================================
        try {
            log.info("📤 [WEBSOCKET SEND] Enviando para clientes inscritos em {}", TOPIC);
            log.debug("   - Serializando em JSON");
            log.debug("   - Payload size: {} bytes", response.toString().length());
            
            messagingTemplate.convertAndSend(TOPIC, response);
            
            log.info("✅ [WEBSOCKET SEND] Notificação enviada com sucesso!");
            log.info("   - Todos os clientes inscritos receberam a mensagem");
            log.info("   - Frontend React deve atualizar em tempo real");
            
        } catch (Exception e) {
            log.error("❌ [WEBSOCKET SEND] Erro ao enviar notificação WebSocket", e);
            log.error("   - Erro: {}", e.getMessage());
            log.error("   - Causa: {}", e.getCause());
            log.error("   - Stack trace:", e);
            throw new RuntimeException("Erro ao enviar notificação via WebSocket", e);
        }
        
        log.info("=".repeat(80));
    }

    // ====================================
    // # notifyIncidentUpdated - Notificar atualização
    // ====================================
    /**
     * Envia notificação de atualização de incidente.
     *
     * @param incident Incidente atualizado
     */
    public void notifyIncidentUpdated(Incident incident) {
        log.info("🔄 [WEBSOCKET SEND] Notificando atualização de incidente");
        log.info("   - ID: {}", incident.getId());
        log.info("   - Novo Status: {}", incident.getStatus());
        log.info("   - Topic: {}", TOPIC);
        
        try {
            IncidentResponse response = incidentMapper.toResponse(incident);
            messagingTemplate.convertAndSend(TOPIC, response);
            log.info("✅ [WEBSOCKET SEND] Atualização notificada com sucesso!");
        } catch (Exception e) {
            log.error("❌ [WEBSOCKET SEND] Erro ao notificar atualização", e);
            throw new RuntimeException("Erro ao enviar notificação de atualização", e);
        }
    }

    // ====================================
    // # notifyIncidentDeleted - Notificar deleção
    // ====================================
    /**
     * Envia notificação de deleção de incidente.
     *
     * @param incidentId ID do incidente deletado
     */
    public void notifyIncidentDeleted(Long incidentId) {
        log.info("🗑️  [WEBSOCKET SEND] Notificando deleção de incidente");
        log.info("   - ID: {}", incidentId);
        log.info("   - Topic: {}/deleted", TOPIC);
        
        try {
            messagingTemplate.convertAndSend(TOPIC + "/deleted", incidentId);
            log.info("✅ [WEBSOCKET SEND] Deleção notificada com sucesso!");
        } catch (Exception e) {
            log.error("❌ [WEBSOCKET SEND] Erro ao notificar deleção", e);
        }
    }

    // ====================================
    // # Notas sobre WebSocket e STOMP
    // ====================================
    /*
     * FLUXO WEBSOCKET COMPLETO:
     * 
     * 1. CONECTAR
     *    - Frontend: stompClient.connect()
     *    - Backend: WebSocketConfig.registerStompEndpoints("/ws/incidents")
     *    - Handshake HTTP → Upgrade para WebSocket
     *
     * 2. INSCREVER
     *    - Frontend: stompClient.subscribe("/topic/incidents", onMessage)
     *    - Backend: Spring STOMP recebe subscrição
     *    - Mapeia cliente para tópico
     *
     * 3. ENVIAR
     *    - Backend: messagingTemplate.convertAndSend("/topic/incidents", msg)
     *    - Spring STOMP serializa em JSON
     *    - Envia para TODOS os clientes inscritos
     *    - Via WebSocket (conexão persistente TCP)
     *
     * 4. RECEBER
     *    - Frontend: onMessage(message)
     *    - Desserializa JSON
     *    - Atualiza estado React
     *    - Re-renderiza UI
     *
     * CLIENTES CONECTADOS:
     * - Se 1 cliente: recebe 1 mensagem
     * - Se 10 clientes: recebem 10 mensagens (broadcast)
     * - Cada cliente em sua própria sessão WebSocket
     *
     * PERSISTÊNCIA:
     * - Conexão WebSocket mantém sessão aberta
     * - Enquanto client estiver na página, recebe mensagens
     * - Se desconectar, para de receber
     * - Se reconectar, se inscreve novamente
     */
}
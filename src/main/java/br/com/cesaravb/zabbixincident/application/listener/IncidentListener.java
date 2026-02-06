package br.com.cesaravb.zabbixincident.application.listener;

import br.com.cesaravb.zabbixincident.domain.entity.Incident;
import br.com.cesaravb.zabbixincident.application.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Listener que consome mensagens da fila RabbitMQ.
 *
 * Fluxo:
 * 1. RabbitMQ publica mensagem na queue
 * 2. @RabbitListener detecta nova mensagem
 * 3. Jackson2JsonMessageConverter desserializa JSON → Incident
 * 4. Logger registra consumo
 * 5. WebSocketNotificationService envia para frontend
 * 6. Frontend recebe via WebSocket em tempo real
 *
 * Logger rastreia cada etapa do processamento assíncrono
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentListener {

    // ====================================
    // # Dependências
    // ====================================
    private final WebSocketNotificationService webSocketService;

    // ====================================
    // # Constante da queue
    // ====================================
    private static final String QUEUE_NAME = "zabbix.incident.queue";

    // ====================================
    // # consumeIncident - Consumir da fila
    // ====================================
    /**
     * Consome mensagens da fila RabbitMQ.
     *
     * Esta é uma operação ASSÍNCRONA:
     * - Não bloqueia o Controller
     * - Executa em thread separada
     * - Pode ter múltiplas instâncias consumindo
     *
     * Fluxo com Logger:
     * 1. ✅ INFO - Mensagem recebida da fila
     * 2. ✅ DEBUG - Dados da mensagem
     * 3. ✅ INFO - Iniciando processamento WebSocket
     * 4. ✅ DEBUG - Enviando para frontend
     * 5. ✅ INFO - Concluído com sucesso
     *
     * @param incident Entidade desserializada automaticamente do JSON
     */
    @RabbitListener(queues = QUEUE_NAME)
    public void consumeIncident(Incident incident) {
        log.info("=".repeat(80));
        log.info("📨 [CONSUME FROM RABBITMQ] Mensagem recebida da fila!");
        log.info("   - Queue: {}", QUEUE_NAME);
        log.info("   - Thread: {}", Thread.currentThread().getName());
        log.info("   - Timestamp: {}", System.currentTimeMillis());
        
        // ====================================
        // # Log dos dados do incidente
        // ====================================
        log.info("📋 [CONSUME FROM RABBITMQ] Dados do incidente:");
        log.info("   - ID: {}", incident.getId());
        log.info("   - Zabbix Event ID: {}", incident.getZabbixEventId());
        log.info("   - Título: {}", incident.getTitle());
        log.info("   - Status: {}", incident.getStatus());
        log.info("   - Severidade: {}", incident.getSeverity());
        log.info("   - Host: {}", incident.getHost());
        log.info("   - Host IP: {}", incident.getHostIp());
        log.debug("   - Descrição: {}", incident.getDescription());
        log.debug("   - Created At: {}", incident.getCreatedAt());
        
        // ====================================
        // # Enviar notificação via WebSocket
        // ====================================
        try {
            log.info("📡 [WEBSOCKET NOTIFICATION] Iniciando envio de notificação");
            log.debug("   - Tópico: /topic/incidents");
            log.debug("   - Conversor: Jackson2JsonMessageConverter");
            
            webSocketService.notifyIncidentCreated(incident);
            
            log.info("✅ [WEBSOCKET NOTIFICATION] Notificação enviada com sucesso!");
            log.info("   - Conectados e notificados via /topic/incidents");
            log.info("   - Tempo de processamento completo ✅");
            
        } catch (Exception e) {
            log.error("❌ [WEBSOCKET NOTIFICATION] Erro ao enviar notificação para WebSocket", e);
            log.error("   - Detalhes: {}", e.getMessage());
            log.error("   - Causa: {}", e.getCause());
            // Não relança exceção para não fazer retry automático
            // A mensagem já foi processada
        }
        
        log.info("=".repeat(80));
    }

    // ====================================
    // # Notas sobre comportamento
    // ====================================
    /*
     * IMPORTANTE:
     * 
     * 1. ASSINCRONISMO
     *    - Este método é executado em thread separada
     *    - Não bloqueia o endpoint HTTP
     *    - Zabbix recebe resposta imediatamente
     *
     * 2. FILA
     *    - Se houver 100 mensagens, 100 threads processam
     *    - Com prefetch=1, processa 1 por vez (mais controle)
     *    - Mensagem é deletada apenas se não houver exceção
     *
     * 3. ERRO HANDLING
     *    - Se lançar exceção, mensagem volta para fila
     *    - Tenta novamente (retry automático)
     *    - Se falhar N vezes, vai para Dead Letter Queue
     *
     * 4. MONITORAMENTO
     *    - Logs rastreiam cada etapa
     *    - Fácil debugar com timestamps e thread info
     *    - Pode monitorar via Docker: docker logs -f container
     */
}
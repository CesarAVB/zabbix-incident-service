package br.com.cesaravb.zabbixincident.application.listener;

import br.com.cesaravb.zabbixincident.application.service.WebSocketNotificationService;
import br.com.cesaravb.zabbixincident.domain.entity.Incident;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class IncidentListener {

    private final WebSocketNotificationService webSocketNotificationService;

    public IncidentListener(WebSocketNotificationService webSocketNotificationService) {
        this.webSocketNotificationService = webSocketNotificationService;
    }

    // ====================================
    // # handleIncidentMessage - Processa mensagens de incidentes da fila RabbitMQ
    // ====================================
    @RabbitListener(queues = "${app.rabbitmq.queue.incident}")
    public void handleIncidentMessage(Incident incident) {
        webSocketNotificationService.notifyIncidentCreated(incident);
    }
}
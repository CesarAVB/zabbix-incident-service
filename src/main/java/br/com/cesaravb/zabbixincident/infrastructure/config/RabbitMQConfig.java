package br.com.cesaravb.zabbixincident.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ para processamento assíncrono de incidentes.
 *
 * Fluxo:
 * 1. Service publica Incident na fila
 * 2. RabbitMQ serializa para JSON
 * 3. Listener consome a mensagem
 * 4. RabbitMQ desserializa de JSON para Incident
 * 5. Listener processa e envia via WebSocket
 */
@Configuration
public class RabbitMQConfig {

    // ====================================
    // # Constantes de configuração
    // ====================================
    public static final String INCIDENT_EXCHANGE = "zabbix.incident.exchange";
    public static final String INCIDENT_QUEUE = "zabbix.incident.queue";
    public static final String INCIDENT_ROUTING_KEY = "incident.created";

    // ====================================
    // # Exchange - Ponto de entrada para mensagens
    // ====================================
    /**
     * Define o exchange que receberá as mensagens.
     *
     * Topic exchange: Roteia baseado em padrões de routing key
     * As mensagens vão para a queue se o padrão bater (ex: incident.*)
     */
    @Bean
    public TopicExchange incidentExchange() {
        return new TopicExchange(INCIDENT_EXCHANGE, true, false);
    }

    // ====================================
    // # Queue - Onde as mensagens ficam armazenadas
    // ====================================
    /**
     * Define a fila que armazenará as mensagens.
     *
     * Configuração:
     * - durable: true → Persiste mensagens se RabbitMQ cair
     * - exclusive: false → Pode ser acessada por múltiplos consumers
     * - autoDelete: false → Não deleta quando não tem consumers
     */
    @Bean
    public Queue incidentQueue() {
        return new Queue(INCIDENT_QUEUE, true, false, false);
    }

    // ====================================
    // # Binding - Conecta Exchange → Queue
    // ====================================
    /**
     * Cria o binding entre exchange e queue.
     *
     * Fluxo:
     * 1. Mensagem chega no exchange com routing key "incident.created"
     * 2. Binding verifica se routing key bate
     * 3. Se bater, mensagem vai para a queue
     * 4. Listener consome da queue
     */
    @Bean
    public Binding incidentBinding(TopicExchange incidentExchange, Queue incidentQueue) {
        return BindingBuilder.bind(incidentQueue).to(incidentExchange).with(INCIDENT_ROUTING_KEY);
    }

    // ====================================
    // # MessageConverter - Serializar/Desserializar
    // ====================================
    /**
     * Configura como as mensagens são convertidas.
     *
     * Jackson2JsonMessageConverter:
     * - Entrada: Objeto Java → JSON String
     * - Saída: JSON String → Objeto Java
     *
     * Sem isso, RabbitMQ não consegue processar objetos!
     * Erro comum: "SimpleMessageConverter only supports String, byte[] and Serializable"
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ====================================
    // # RabbitTemplate - Enviar mensagens
    // ====================================
    /**
     * Template para enviar mensagens ao RabbitMQ.
     *
     * Configuração:
     * - connectionFactory: Conexão com RabbitMQ
     * - messageConverter: Como converter objetos
     *
     * Uso no Service:
     * rabbitTemplate.convertAndSend(exchange, routingKey, objeto);
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
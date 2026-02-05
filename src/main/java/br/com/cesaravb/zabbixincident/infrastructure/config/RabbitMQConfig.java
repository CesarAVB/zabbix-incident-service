package br.com.cesaravb.zabbixincident.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange.incident}")
    private String incidentExchange;

    @Value("${app.rabbitmq.queue.incident}")
    private String incidentQueue;

    @Value("${app.rabbitmq.routing-key.incident}")
    private String incidentRoutingKey;

    // ====================================
    // # incidentExchange - Declara o exchange para eventos de incidentes
    // ====================================
    @Bean
    public TopicExchange incidentExchange() {
        return new TopicExchange(incidentExchange, true, false);
    }

    // ====================================
    // # incidentQueue - Declara a fila para processamento de incidentes
    // ====================================
    @Bean
    public Queue incidentQueue() {
        return new Queue(incidentQueue, true);
    }

    // ====================================
    // # incidentBinding - Vincula a fila ao exchange com routing key
    // ====================================
    @Bean
    public Binding incidentBinding(Queue incidentQueue, TopicExchange incidentExchange) {
        return BindingBuilder.bind(incidentQueue).to(incidentExchange).with(incidentRoutingKey);
    }
}
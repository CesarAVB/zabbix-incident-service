package br.com.cesaravb.zabbixincident.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuração do WebSocket para comunicação em tempo real.
 *
 * Fluxo completo:
 * 1. Cliente (frontend) conecta em /ws/incidents via SockJS
 * 2. Negocia protocolo STOMP
 * 3. Subscribe em /topic/incidents
 * 4. Recebe mensagens em tempo real quando publicadas
 *
 * Configurações:
 * • endpoint: URL onde o WebSocket escuta (/ws/incidents)
 * • allowed-origins: Domínios permitidos a conectar
 * • broker-prefix: Prefixo para broadcast (/topic)
 * • app-prefix: Prefixo para handlers (/app)
 *
 * Ambiente Local:
 * • Origins: http://localhost:4200 (Angular), http://localhost:3000 (React)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // ====================================
    // # allowedOrigins - Domínios permitidos a conectar
    // ====================================
    @Value("${app.websocket.allowed-origins}")
    private String allowedOrigins;

    // ====================================
    // # endpoint - URL do WebSocket
    // ====================================
    @Value("${app.websocket.endpoint}")
    private String endpoint;

    // ====================================
    // # brokerPrefix - Prefixo para broadcast
    // ====================================
    @Value("${app.websocket.broker-prefix}")
    private String brokerPrefix;

    // ====================================
    // # appPrefix - Prefixo para handlers
    // ====================================
    @Value("${app.websocket.app-prefix}")
    private String appPrefix;

    // ====================================
    // # configureMessageBroker - Configurar broker de mensagens
    // ====================================
    /**
     * Configura o broker de mensagens STOMP.
     *
     * Broker Simples:
     * • enableSimpleBroker(brokerPrefix): Ativa broker em memória
     * • Prefixo /topic: Para broadcast (todos recebem)
     *
     * Application Prefix:
     * • setApplicationDestinationPrefixes(appPrefix): Prefixo para handlers
     * • Prefixo /app: Para rotas específicas da aplicação
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker(brokerPrefix);
        config.setApplicationDestinationPrefixes(appPrefix);
    }

    // ====================================
    // # registerStompEndpoints - Registrar endpoints STOMP
    // ====================================
    /**
     * Registra os endpoints onde o WebSocket escuta.
     *
     * Endpoint: /ws/incidents
     * • SockJS habilitado para fallback (se WebSocket não funcionar)
     * • CORS habilitado para allowed-origins
     *
     * Fluxo de conexão:
     * 1. Cliente conecta em 
     * 2. Negocia protocolo (WebSocket ou SockJS fallback)
     * 3. Após sucesso, pode subscrever a tópicos
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ====================================
        // # allowedOrigins - Array de domínios permitidos
        // ====================================
        String[] origins = allowedOrigins.split(",");
        registry.addEndpoint(endpoint).setAllowedOrigins(origins).withSockJS();
    }
}
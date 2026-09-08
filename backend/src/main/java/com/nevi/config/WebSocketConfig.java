package com.nevi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

// Configura WebSocket con el protocolo STOMP para el chat en tiempo real.
// STOMP (Simple Text Oriented Messaging Protocol) funciona sobre WebSocket
// y permite suscribirse a "topics" — como canales de chat por grupo.
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint de conexión WebSocket.
        // El frontend se conecta a: ws://localhost:8080/ws
        // SockJS es un fallback para navegadores sin soporte nativo de WebSocket.
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefijo para los topics de suscripción.
        // Los clientes se suscriben a: /topic/grupo/{grupoId}
        registry.enableSimpleBroker("/topic");

        // Prefijo para los mensajes enviados desde el cliente al servidor.
        // El cliente envía mensajes a: /app/chat/{grupoId}
        registry.setApplicationDestinationPrefixes("/app");
    }
}

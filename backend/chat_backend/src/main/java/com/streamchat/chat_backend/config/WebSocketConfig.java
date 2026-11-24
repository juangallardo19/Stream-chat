package com.streamchat.chat_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.chat_backend.domain.GroupCatalog;
import com.streamchat.chat_backend.domain.GroupSessions;
import com.streamchat.chat_backend.websocket.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
class WebSocketConfig implements WebSocketConfigurer {

    private static final String RUTA_CHAT = "/ws/chat";

    private final ChatWebSocketHandler handler;
    private final String origenPermitido;

    WebSocketConfig(ChatWebSocketHandler handler, @Value("${app.cors.origin}") String origenPermitido) {
        this.handler = handler;
        this.origenPermitido = origenPermitido;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registro) {
        registro.addHandler(handler, RUTA_CHAT).setAllowedOrigins(origenPermitido);
    }

    @Configuration
    static class HandlerConfig {

        @Bean
        ChatWebSocketHandler chatWebSocketHandler(GroupSessions sesiones, GroupCatalog catalogo,
                                                  ObjectMapper objectMapper) {
            return new ChatWebSocketHandler(sesiones, catalogo, objectMapper);
        }
    }
}

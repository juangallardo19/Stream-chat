package com.streamchat.status_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.status_backend.domain.GroupCatalog;
import com.streamchat.status_backend.domain.GroupSessions;
import com.streamchat.status_backend.websocket.StatusWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
class WebSocketConfig implements WebSocketConfigurer {

    private static final String RUTA_ESTADO = "/ws/status";

    private final StatusWebSocketHandler handler;
    private final String origenPermitido;

    WebSocketConfig(StatusWebSocketHandler handler, @Value("${app.cors.origin}") String origenPermitido) {
        this.handler = handler;
        this.origenPermitido = origenPermitido;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registro) {
        registro.addHandler(handler, RUTA_ESTADO).setAllowedOrigins(origenPermitido);
    }

    @Configuration
    static class HandlerConfig {

        @Bean
        StatusWebSocketHandler statusWebSocketHandler(GroupSessions sesiones, GroupCatalog catalogo,
                                                  ObjectMapper objectMapper) {
            return new StatusWebSocketHandler(sesiones, catalogo, objectMapper);
        }
    }
}

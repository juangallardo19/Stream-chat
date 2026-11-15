package com.streamchat.status_backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.status_backend.domain.StatusSubscriber;
import com.streamchat.status_backend.domain.StatusUpdate;
import com.streamchat.status_backend.mapper.StatusUpdateMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

final class WebSocketStatusSubscriber implements StatusSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketStatusSubscriber.class);

    private final WebSocketSession sesion;
    private final ObjectMapper objectMapper;

    WebSocketStatusSubscriber(WebSocketSession sesion, ObjectMapper objectMapper) {
        this.sesion = sesion;
        this.objectMapper = objectMapper;
    }

    @Override
    public void recibir(StatusUpdate actualizacion) {
        if (!sesion.isOpen()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(StatusUpdateMapper.aPayload(actualizacion));
            sesion.sendMessage(new TextMessage(json));
        } catch (IOException | RuntimeException error) {
            LOG.warn("No se pudo entregar el estado de {} a la sesión {}: {}", actualizacion.user().value(),
                    sesion.getId(), error.getMessage());
        }
    }
}

package com.streamchat.chat_backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.chat_backend.domain.ChatMessage;
import com.streamchat.chat_backend.domain.ChatSubscriber;
import com.streamchat.chat_backend.mapper.ChatMessageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

final class WebSocketChatSubscriber implements ChatSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketChatSubscriber.class);

    private final WebSocketSession sesion;
    private final ObjectMapper objectMapper;

    WebSocketChatSubscriber(WebSocketSession sesion, ObjectMapper objectMapper) {
        this.sesion = sesion;
        this.objectMapper = objectMapper;
    }

    @Override
    public void recibir(ChatMessage mensaje) {
        if (!sesion.isOpen()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(ChatMessageMapper.aPayload(mensaje));
            sesion.sendMessage(new TextMessage(json));
        } catch (IOException | RuntimeException error) {
            LOG.warn("No se pudo entregar el mensaje {} a la sesión {}: {}", mensaje.messageId(), sesion.getId(),
                    error.getMessage());
        }
    }
}

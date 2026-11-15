package com.streamchat.chat_backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.chat_backend.domain.ChatMessage;
import com.streamchat.chat_backend.domain.ChatSubscriber;
import org.springframework.web.socket.WebSocketSession;

final class WebSocketChatSubscriber implements ChatSubscriber {

    private final WebSocketSession sesion;
    private final ObjectMapper objectMapper;

    WebSocketChatSubscriber(WebSocketSession sesion, ObjectMapper objectMapper) {
        this.sesion = sesion;
        this.objectMapper = objectMapper;
    }

    @Override
    public void recibir(ChatMessage mensaje) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}

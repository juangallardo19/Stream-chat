package com.streamchat.chat_backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.chat_backend.domain.GroupCatalog;
import com.streamchat.chat_backend.domain.GroupSessions;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final GroupSessions sesiones;
    private final GroupCatalog catalogo;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(GroupSessions sesiones, GroupCatalog catalogo, ObjectMapper objectMapper) {
        this.sesiones = sesiones;
        this.catalogo = catalogo;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession sesion) throws Exception {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession sesion, CloseStatus estado) throws Exception {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}

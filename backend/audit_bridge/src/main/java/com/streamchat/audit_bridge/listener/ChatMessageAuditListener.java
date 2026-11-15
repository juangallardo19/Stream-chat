package com.streamchat.audit_bridge.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.audit_bridge.service.AuditBridgeService;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

@Component
class ChatMessageAuditListener {

    private final AuditBridgeService servicio;
    private final ObjectMapper objectMapper;

    ChatMessageAuditListener(AuditBridgeService servicio, ObjectMapper objectMapper) {
        this.servicio = servicio;
        this.objectMapper = objectMapper;
    }

    void recibir(Message mensajeAmqp) {
    }
}

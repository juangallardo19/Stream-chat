package com.streamchat.audit_bridge.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.audit_bridge.domain.AuditRecord;
import com.streamchat.audit_bridge.dto.ChatMessagePayload;
import com.streamchat.audit_bridge.mapper.AuditRecordMapper;
import com.streamchat.audit_bridge.service.AuditBridgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
class ChatMessageAuditListener {

    private static final Logger LOG = LoggerFactory.getLogger(ChatMessageAuditListener.class);

    private final AuditBridgeService servicio;
    private final ObjectMapper objectMapper;

    ChatMessageAuditListener(AuditBridgeService servicio, ObjectMapper objectMapper) {
        this.servicio = servicio;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "audit_bridge.messages", concurrency = "1")
    void recibir(Message mensajeAmqp) {
        servicio.transferir(convertir(mensajeAmqp));
    }

    private AuditRecord convertir(Message mensajeAmqp) {
        try {
            ChatMessagePayload payload = objectMapper.readValue(mensajeAmqp.getBody(), ChatMessagePayload.class);
            return AuditRecordMapper.aDominio(payload);
        } catch (IOException | RuntimeException error) {
            LOG.warn("Mensaje descartado de la auditoría por estar mal formado: {}", error.getMessage());
            throw new AmqpRejectAndDontRequeueException("Mensaje de chat mal formado", error);
        }
    }
}

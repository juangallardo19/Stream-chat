package com.streamchat.analytics_service.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.analytics_service.domain.AuditedMessage;
import com.streamchat.analytics_service.dto.ChatMessagePayload;
import com.streamchat.analytics_service.mapper.AuditedMessageMapper;
import com.streamchat.analytics_service.service.ArchiveMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class AuditLogListener {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogListener.class);

    private final ArchiveMessageService servicio;
    private final ObjectMapper objectMapper;

    AuditLogListener(ArchiveMessageService servicio, ObjectMapper objectMapper) {
        this.servicio = servicio;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "message_audit_log", groupId = "analytics-group")
    void recibir(String json) {
        convertir(json).ifPresent(servicio::archivar);
    }

    private Optional<AuditedMessage> convertir(String json) {
        try {
            ChatMessagePayload payload = objectMapper.readValue(json, ChatMessagePayload.class);
            return Optional.of(AuditedMessageMapper.aDominio(payload));
        } catch (JsonProcessingException | RuntimeException error) {
            LOG.warn("Registro de auditoría saltado por estar mal formado: {}", error.getMessage());
            return Optional.empty();
        }
    }
}

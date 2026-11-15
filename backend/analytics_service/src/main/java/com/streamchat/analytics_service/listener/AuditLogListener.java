package com.streamchat.analytics_service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.analytics_service.service.ArchiveMessageService;
import org.springframework.stereotype.Component;

@Component
class AuditLogListener {

    private final ArchiveMessageService servicio;
    private final ObjectMapper objectMapper;

    AuditLogListener(ArchiveMessageService servicio, ObjectMapper objectMapper) {
        this.servicio = servicio;
        this.objectMapper = objectMapper;
    }

    void recibir(String json) {
    }
}

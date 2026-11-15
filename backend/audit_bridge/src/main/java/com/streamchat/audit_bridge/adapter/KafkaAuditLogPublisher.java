package com.streamchat.audit_bridge.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.audit_bridge.domain.AuditRecord;
import com.streamchat.audit_bridge.port.AuditLogPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class KafkaAuditLogPublisher implements AuditLogPublisher {

    static final String TOPIC = "message_audit_log";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    KafkaAuditLogPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publicar(AuditRecord registro) {
    }
}

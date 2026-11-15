package com.streamchat.audit_bridge.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.audit_bridge.domain.AuditRecord;
import com.streamchat.audit_bridge.mapper.AuditRecordMapper;
import com.streamchat.audit_bridge.port.AuditLogPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
class KafkaAuditLogPublisher implements AuditLogPublisher {

    static final String TOPIC = "message_audit_log";
    private static final long ESPERA_CONFIRMACION_SEGUNDOS = 15;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    KafkaAuditLogPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publicar(AuditRecord registro) {
        Objects.requireNonNull(registro, "El registro de auditoría es obligatorio");
        String json = aJson(registro);
        try {
            kafkaTemplate.send(TOPIC, registro.claveParticion(), json).get(ESPERA_CONFIRMACION_SEGUNDOS, TimeUnit.SECONDS);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw noConfirmado(registro, error);
        } catch (ExecutionException | TimeoutException error) {
            throw noConfirmado(registro, error);
        }
    }

    private String aJson(AuditRecord registro) {
        try {
            return objectMapper.writeValueAsString(AuditRecordMapper.aPayload(registro));
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("No se pudo convertir a JSON el mensaje " + registro.messageId(), error);
        }
    }

    private static IllegalStateException noConfirmado(AuditRecord registro, Exception causa) {
        return new IllegalStateException("Kafka no confirmó el mensaje " + registro.messageId(), causa);
    }
}

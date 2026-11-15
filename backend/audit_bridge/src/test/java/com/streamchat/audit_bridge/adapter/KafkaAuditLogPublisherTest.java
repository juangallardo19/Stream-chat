package com.streamchat.audit_bridge.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.streamchat.audit_bridge.domain.AuditRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaAuditLogPublisher")
class KafkaAuditLogPublisherTest {

    private static final String JSON_ESPERADO = "{\"messageId\":\"3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b\",\"user\":\"Ana\","
            + "\"group\":\"GrupoA\",\"text\":\"¡Hola!\",\"sentAt\":\"2026-11-13T16:30:10.123Z\"}";

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @InjectMocks
    private KafkaAuditLogPublisher publicador;

    private final AuditRecord registro = new AuditRecord(
            UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b"),
            "Ana",
            "GrupoA",
            "¡Hola!",
            Instant.parse("2026-11-13T16:30:10.123Z"));

    @SuppressWarnings("unchecked")
    private static CompletableFuture<SendResult<String, String>> confirmado() {
        return CompletableFuture.completedFuture(mock(SendResult.class));
    }

    @Nested
    @DisplayName("Cuando Kafka confirma")
    class KafkaConfirma {

        @Test
        @DisplayName("publica en message_audit_log con la clave group y el JSON del contrato")
        void publicaConTopicClaveYJson() {
            when(kafkaTemplate.send("message_audit_log", "GrupoA", JSON_ESPERADO)).thenReturn(confirmado());

            publicador.publicar(registro);

            verify(kafkaTemplate, times(1)).send("message_audit_log", "GrupoA", JSON_ESPERADO);
        }
    }

    @Nested
    @DisplayName("Cuando Kafka falla")
    class KafkaFalla {

        @Test
        @DisplayName("lanza IllegalStateException para que RabbitMQ no reciba el ack")
        void propagaElFallo() {
            when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker caído")));

            IllegalStateException error = assertThrows(IllegalStateException.class, () -> publicador.publicar(registro));

            assertEquals("Kafka no confirmó el mensaje 3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b", error.getMessage());
        }

        @Test
        @DisplayName("rechaza un registro nulo sin tocar Kafka")
        void rechazaNulo() {
            NullPointerException error = assertThrows(NullPointerException.class, () -> publicador.publicar(null));

            assertEquals("El registro de auditoría es obligatorio", error.getMessage());
            verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
        }
    }
}

package com.streamchat.audit_bridge.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.streamchat.audit_bridge.domain.AuditRecord;
import com.streamchat.audit_bridge.service.AuditBridgeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageAuditListener")
class ChatMessageAuditListenerTest {

    private static final String JSON_VALIDO = "{\"messageId\":\"3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b\",\"user\":\"Ana\","
            + "\"group\":\"GrupoA\",\"text\":\"¡Hola!\",\"sentAt\":\"2026-11-13T16:30:10.123Z\"}";

    @Mock
    private AuditBridgeService servicio;

    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @InjectMocks
    private ChatMessageAuditListener listener;

    private static Message mensajeAmqp(String cuerpo) {
        return new Message(cuerpo.getBytes(StandardCharsets.UTF_8), new MessageProperties());
    }

    @Nested
    @DisplayName("Con un mensaje bien formado")
    class MensajeValido {

        @Test
        @DisplayName("lo convierte en registro y lo transfiere una vez")
        void transfiereAlServicio() {
            AuditRecord esperado = new AuditRecord(UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b"),
                    "Ana", "GrupoA", "¡Hola!", Instant.parse("2026-11-13T16:30:10.123Z"));

            listener.recibir(mensajeAmqp(JSON_VALIDO));

            verify(servicio, times(1)).transferir(esperado);
        }

        @Test
        @DisplayName("si Kafka falla, propaga el error y el mensaje vuelve a la cola")
        void propagaFalloDeKafka() {
            doThrow(new IllegalStateException("Kafka no confirmó")).when(servicio).transferir(any());

            IllegalStateException error = assertThrows(IllegalStateException.class,
                    () -> listener.recibir(mensajeAmqp(JSON_VALIDO)));

            assertEquals("Kafka no confirmó", error.getMessage());
        }
    }

    @Nested
    @DisplayName("Con un mensaje mal formado se descarta sin volver a la cola")
    class MensajeInvalido {

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "esto no es JSON",
                "{\"user\":\"Ana\",\"group\":\"GrupoA\",\"text\":\"¡Hola!\",\"sentAt\":\"2026-11-13T16:30:10.123Z\"}",
                "{\"messageId\":\"3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b\",\"user\":\"Ana\",\"group\":\"Grupo.A\",\"text\":\"¡Hola!\",\"sentAt\":\"2026-11-13T16:30:10.123Z\"}",
                "{\"messageId\":\"3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b\",\"user\":\"Ana\",\"group\":\"GrupoA\",\"text\":\" \",\"sentAt\":\"2026-11-13T16:30:10.123Z\"}"
        })
        @DisplayName("lanza AmqpRejectAndDontRequeueException y no transfiere nada")
        void descartaSinReencolar(String cuerpo) {
            Message mensaje = mensajeAmqp(cuerpo);

            assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.recibir(mensaje));

            verify(servicio, never()).transferir(any());
        }
    }
}

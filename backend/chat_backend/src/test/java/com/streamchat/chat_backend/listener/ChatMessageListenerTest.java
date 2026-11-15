package com.streamchat.chat_backend.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.streamchat.chat_backend.domain.ChatMessage;
import com.streamchat.chat_backend.domain.GroupId;
import com.streamchat.chat_backend.domain.MessageText;
import com.streamchat.chat_backend.domain.UserId;
import com.streamchat.chat_backend.service.DeliverMessageService;
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
@DisplayName("ChatMessageListener")
class ChatMessageListenerTest {

    private static final String JSON_VALIDO = "{\"messageId\":\"3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b\",\"user\":\"Ana\","
            + "\"group\":\"GrupoA\",\"text\":\"¡Hola!\",\"sentAt\":\"2026-11-13T16:30:10.123Z\"}";

    @Mock
    private DeliverMessageService servicio;

    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @InjectMocks
    private ChatMessageListener listener;

    private static Message mensajeAmqp(String cuerpo) {
        return new Message(cuerpo.getBytes(StandardCharsets.UTF_8), new MessageProperties());
    }

    @Nested
    @DisplayName("Con un mensaje bien formado")
    class MensajeValido {

        @Test
        @DisplayName("lo convierte al dominio y lo entrega al servicio una vez")
        void entregaAlServicio() {
            ChatMessage esperado = new ChatMessage(UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b"),
                    new UserId("Ana"), new GroupId("GrupoA"), new MessageText("¡Hola!"),
                    Instant.parse("2026-11-13T16:30:10.123Z"));

            listener.recibir(mensajeAmqp(JSON_VALIDO));

            verify(servicio, times(1)).entregar(esperado);
        }

        @Test
        @DisplayName("si el servicio falla, propaga el error para que RabbitMQ no confirme el mensaje")
        void propagaFalloDelServicio() {
            doThrow(new IllegalStateException("Fallo al difundir")).when(servicio).entregar(any());

            IllegalStateException error = assertThrows(IllegalStateException.class,
                    () -> listener.recibir(mensajeAmqp(JSON_VALIDO)));

            assertEquals("Fallo al difundir", error.getMessage());
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
        @DisplayName("lanza AmqpRejectAndDontRequeueException y no entrega nada")
        void descartaSinReencolar(String cuerpo) {
            Message mensaje = mensajeAmqp(cuerpo);

            assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.recibir(mensaje));

            verify(servicio, never()).entregar(any());
        }
    }
}

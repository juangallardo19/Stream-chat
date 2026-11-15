package com.streamchat.typing_processor.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.streamchat.typing_processor.domain.GroupId;
import com.streamchat.typing_processor.domain.StatusChange;
import com.streamchat.typing_processor.domain.TypingStatus;
import com.streamchat.typing_processor.domain.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RabbitStatusPublisher")
class RabbitStatusPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder().build();

    @InjectMocks
    private RabbitStatusPublisher publicador;

    @Captor
    private ArgumentCaptor<Message> mensajeCaptor;

    private final StatusChange cambio = new StatusChange(new UserId("Ana"), new GroupId("GrupoA"), TypingStatus.TYPING);

    @Nested
    @DisplayName("Al publicar una transición")
    class Publicar {

        @Test
        @DisplayName("la envía una vez al fanout status_updates sin routing key")
        void enviaAlFanout() {
            publicador.publicar(cambio);

            verify(rabbitTemplate, times(1)).send(eq("status_updates"), eq(""), any(Message.class));
        }

        @Test
        @DisplayName("el cuerpo es el JSON del contrato, no persistente y con tipo application/json")
        void cuerpoYPropiedades() {
            publicador.publicar(cambio);

            verify(rabbitTemplate).send(eq("status_updates"), eq(""), mensajeCaptor.capture());
            Message enviado = mensajeCaptor.getValue();
            assertAll(
                    () -> assertEquals("{\"user\":\"Ana\",\"group\":\"GrupoA\",\"status\":\"TYPING\"}",
                            new String(enviado.getBody(), StandardCharsets.UTF_8)),
                    () -> assertEquals(MessageDeliveryMode.NON_PERSISTENT,
                            enviado.getMessageProperties().getDeliveryMode()),
                    () -> assertEquals(MessageProperties.CONTENT_TYPE_JSON,
                            enviado.getMessageProperties().getContentType()));
        }
    }

    @Nested
    @DisplayName("Cuando RabbitMQ falla")
    class RabbitFalla {

        @Test
        @DisplayName("no lanza error: en el flujo de estado se acepta perder una transición")
        void aceptaLaPerdida() {
            doThrow(new AmqpConnectException(new ConnectException("RabbitMQ caído")))
                    .when(rabbitTemplate).send(anyString(), anyString(), any(Message.class));

            assertDoesNotThrow(() -> publicador.publicar(cambio));
        }

        @Test
        @DisplayName("rechaza una transición nula sin tocar RabbitMQ")
        void rechazaNula() {
            NullPointerException error = assertThrows(NullPointerException.class, () -> publicador.publicar(null));

            assertEquals("La transición de estado es obligatoria", error.getMessage());
            verify(rabbitTemplate, never()).send(anyString(), anyString(), any(Message.class));
        }
    }
}

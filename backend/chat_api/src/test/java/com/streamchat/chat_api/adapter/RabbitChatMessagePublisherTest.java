package com.streamchat.chat_api.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.streamchat.chat_api.domain.ChatMessage;
import com.streamchat.chat_api.domain.GroupId;
import com.streamchat.chat_api.domain.MessageText;
import com.streamchat.chat_api.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RabbitChatMessagePublisher")
class RabbitChatMessagePublisherTest {

    private static final Instant INSTANTE = Instant.parse("2026-11-13T16:30:10.123Z");

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @InjectMocks
    private RabbitChatMessagePublisher publisher;

    @Captor
    private ArgumentCaptor<Message> mensajeAmqpCaptor;

    private final ChatMessage mensaje = ChatMessage.registrarMensaje(
            new UserId("Ana"), new GroupId("GrupoA"), new MessageText("¡Hola!"),
            Clock.fixed(INSTANTE, ZoneOffset.UTC));

    @BeforeEach
    void ejecutarCanalConConfirmaciones() {
        lenient().when(rabbitTemplate.invoke(any(RabbitOperations.OperationsCallback.class)))
                .thenAnswer(invocacion -> {
                    RabbitOperations.OperationsCallback<?> callback = invocacion.getArgument(0);
                    return callback.doInRabbit(rabbitTemplate);
                });
    }

    @Nested
    @DisplayName("Destino en RabbitMQ")
    class Destino {

        @Test
        @DisplayName("publica en el exchange chat_messages con la routing key del grupo")
        void publicaEnExchangeYRoutingKey() {
            publisher.publicar(mensaje);

            verify(rabbitTemplate).send(eq("chat_messages"), eq("chat.grupoa"), any(Message.class));
        }
    }

    @Nested
    @DisplayName("Propiedades del mensaje AMQP")
    class Propiedades {

        @Test
        @DisplayName("es persistente, JSON en UTF-8 y lleva el messageId del mensaje")
        void propiedadesDeDurabilidadYFormato() {
            publisher.publicar(mensaje);

            verify(rabbitTemplate).send(anyString(), anyString(), mensajeAmqpCaptor.capture());
            MessageProperties propiedades = mensajeAmqpCaptor.getValue().getMessageProperties();
            assertAll(
                    () -> assertEquals(MessageDeliveryMode.PERSISTENT, propiedades.getDeliveryMode()),
                    () -> assertEquals(MessageProperties.CONTENT_TYPE_JSON, propiedades.getContentType()),
                    () -> assertEquals("UTF-8", propiedades.getContentEncoding()),
                    () -> assertEquals(mensaje.messageId().toString(), propiedades.getMessageId()));
        }

        @Test
        @DisplayName("no agrega la cabecera de tipo Java __TypeId__")
        void sinCabeceraDeTipoJava() {
            publisher.publicar(mensaje);

            verify(rabbitTemplate).send(anyString(), anyString(), mensajeAmqpCaptor.capture());
            assertFalse(mensajeAmqpCaptor.getValue().getMessageProperties().getHeaders().containsKey("__TypeId__"));
        }
    }

    @Nested
    @DisplayName("Cuerpo JSON")
    class CuerpoJson {

        @Test
        @DisplayName("sigue el contrato de la sección 5.5: messageId, user, group, text y sentAt")
        void cuerpoSegunContrato() throws Exception {
            publisher.publicar(mensaje);

            verify(rabbitTemplate).send(anyString(), anyString(), mensajeAmqpCaptor.capture());
            JsonNode json = new ObjectMapper().readTree(mensajeAmqpCaptor.getValue().getBody());
            assertAll(
                    () -> assertEquals(mensaje.messageId().toString(), json.get("messageId").asText()),
                    () -> assertEquals("Ana", json.get("user").asText()),
                    () -> assertEquals("GrupoA", json.get("group").asText()),
                    () -> assertEquals("¡Hola!", json.get("text").asText()),
                    () -> assertEquals("2026-11-13T16:30:10.123Z", json.get("sentAt").asText()),
                    () -> assertEquals(List.of("messageId", "user", "group", "text", "sentAt"),
                            nombresDeCampos(json)));
        }

        private List<String> nombresDeCampos(JsonNode json) {
            return json.properties().stream().map(Map.Entry::getKey).toList();
        }
    }

    @Nested
    @DisplayName("Confirmación del broker (publisher confirms)")
    class Confirmacion {

        @Test
        @DisplayName("espera la confirmación de RabbitMQ después de enviar")
        void esperaConfirmacionDespuesDeEnviar() {
            publisher.publicar(mensaje);

            InOrder orden = inOrder(rabbitTemplate);
            orden.verify(rabbitTemplate).send(anyString(), anyString(), any(Message.class));
            orden.verify(rabbitTemplate).waitForConfirmsOrDie(5_000L);
        }

        @Test
        @DisplayName("si RabbitMQ no confirma a tiempo, propaga el error")
        void propagaFaltaDeConfirmacion() {
            doThrow(new AmqpTimeoutException("RabbitMQ no confirmó el mensaje"))
                    .when(rabbitTemplate).waitForConfirmsOrDie(anyLong());

            AmqpTimeoutException error = assertThrows(AmqpTimeoutException.class, () -> publisher.publicar(mensaje));

            assertEquals("RabbitMQ no confirmó el mensaje", error.getMessage());
        }
    }

    @Nested
    @DisplayName("Fallo al convertir a JSON")
    class FalloJson {

        @Test
        @DisplayName("lanza IllegalStateException con mensaje claro y no envía nada")
        void noEnviaSiFallaLaConversion() throws JsonProcessingException {
            doThrow(new JsonMappingException(null, "fallo simulado")).when(objectMapper).writeValueAsBytes(any());

            IllegalStateException error = assertThrows(IllegalStateException.class, () -> publisher.publicar(mensaje));

            assertEquals("No se pudo convertir a JSON el mensaje " + mensaje.messageId(), error.getMessage());
            verify(rabbitTemplate, never()).send(anyString(), anyString(), any(Message.class));
        }
    }
}

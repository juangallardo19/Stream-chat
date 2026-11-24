package com.streamchat.chat_backend.mapper;

import com.streamchat.chat_backend.domain.ChatMessage;
import com.streamchat.chat_backend.domain.GroupId;
import com.streamchat.chat_backend.domain.MessageText;
import com.streamchat.chat_backend.domain.UserId;
import com.streamchat.chat_backend.dto.ChatMessagePayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ChatMessageMapper")
class ChatMessageMapperTest {

    private static final UUID IDENTIFICADOR = UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b");
    private static final Instant INSTANTE = Instant.parse("2026-11-13T16:30:10.123Z");

    private final ChatMessage mensaje = new ChatMessage(IDENTIFICADOR, new UserId("Ana"), new GroupId("GrupoA"),
            new MessageText("¡Hola!"), INSTANTE);
    private final ChatMessagePayload payload = new ChatMessagePayload(IDENTIFICADOR, "Ana", "GrupoA", "¡Hola!", INSTANTE);

    @Nested
    @DisplayName("aDominio()")
    class ADominio {

        @Test
        @DisplayName("convierte el JSON recibido de RabbitMQ en un mensaje del dominio")
        void convierteADominio() {
            ChatMessage resultado = ChatMessageMapper.aDominio(payload);

            assertEquals(mensaje, resultado);
        }

        @Test
        @DisplayName("rechaza un grupo con formato inválido aunque venga de la cola")
        void rechazaGrupoInvalido() {
            ChatMessagePayload invalido = new ChatMessagePayload(IDENTIFICADOR, "Ana", "Grupo.A", "¡Hola!", INSTANTE);

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> ChatMessageMapper.aDominio(invalido));

            assertEquals("El grupo solo admite letras, números, guion y guion bajo", error.getMessage());
        }

        @Test
        @DisplayName("rechaza un payload nulo")
        void rechazaPayloadNulo() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> ChatMessageMapper.aDominio(null));

            assertEquals("El payload es obligatorio", error.getMessage());
        }
    }

    @Nested
    @DisplayName("aPayload()")
    class APayload {

        @Test
        @DisplayName("convierte el mensaje del dominio en el JSON que se envía al navegador")
        void convierteAPayload() {
            ChatMessagePayload resultado = ChatMessageMapper.aPayload(mensaje);

            assertEquals(payload, resultado);
        }

        @Test
        @DisplayName("rechaza un mensaje nulo")
        void rechazaMensajeNulo() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> ChatMessageMapper.aPayload(null));

            assertEquals("El mensaje es obligatorio", error.getMessage());
        }
    }
}

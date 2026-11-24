package com.streamchat.chat_backend.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ChatMessage")
class ChatMessageTest {

    private static final UUID IDENTIFICADOR = UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b");
    private static final Instant INSTANTE = Instant.parse("2026-11-13T16:30:10.123Z");

    private final UserId ana = new UserId("Ana");
    private final GroupId grupoA = new GroupId("GrupoA");
    private final MessageText hola = new MessageText("¡Hola!");

    @Test
    @DisplayName("conserva los cinco datos del mensaje recibido de RabbitMQ")
    void conservaLosDatos() {
        ChatMessage mensaje = new ChatMessage(IDENTIFICADOR, ana, grupoA, hola, INSTANTE);

        assertAll(
                () -> assertEquals(IDENTIFICADOR, mensaje.messageId()),
                () -> assertEquals(ana, mensaje.user()),
                () -> assertEquals(grupoA, mensaje.group()),
                () -> assertEquals(hola, mensaje.text()),
                () -> assertEquals(INSTANTE, mensaje.sentAt()));
    }

    @Nested
    @DisplayName("Rechaza datos faltantes")
    class DatosFaltantes {

        @Test
        @DisplayName("sin identificador")
        void rechazaSinIdentificador() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> new ChatMessage(null, ana, grupoA, hola, INSTANTE));

            assertEquals("El identificador del mensaje es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("sin usuario")
        void rechazaSinUsuario() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> new ChatMessage(IDENTIFICADOR, null, grupoA, hola, INSTANTE));

            assertEquals("El usuario es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("sin grupo")
        void rechazaSinGrupo() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> new ChatMessage(IDENTIFICADOR, ana, null, hola, INSTANTE));

            assertEquals("El grupo es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("sin texto")
        void rechazaSinTexto() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> new ChatMessage(IDENTIFICADOR, ana, grupoA, null, INSTANTE));

            assertEquals("El texto del mensaje es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("sin hora de envío")
        void rechazaSinHora() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> new ChatMessage(IDENTIFICADOR, ana, grupoA, hola, null));

            assertEquals("La hora de envío es obligatoria", error.getMessage());
        }
    }
}

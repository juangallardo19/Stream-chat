package com.streamchat.chat_api.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ChatMessage")
class ChatMessageTest {

    private static final Instant INSTANTE = Instant.parse("2026-11-13T16:30:10.123Z");
    private static final Clock RELOJ = Clock.fixed(INSTANTE, ZoneOffset.UTC);

    private final UserId ana = new UserId("Ana");
    private final GroupId grupoA = new GroupId("GrupoA");
    private final MessageText hola = new MessageText("¡Hola!");

    @Nested
    @DisplayName("registrarMensaje()")
    class RegistrarMensaje {

        @Test
        @DisplayName("toma la hora del reloj inyectado, en UTC")
        void tomaLaHoraDelReloj() {
            ChatMessage mensaje = ChatMessage.registrarMensaje(ana, grupoA, hola, RELOJ);

            assertEquals(INSTANTE, mensaje.sentAt());
        }

        @Test
        @DisplayName("recorta la hora a milisegundos aunque el reloj tenga nanosegundos")
        void recortaAMilisegundos() {
            Clock relojConNanos = Clock.fixed(Instant.parse("2026-11-13T16:30:10.123456789Z"), ZoneOffset.UTC);

            ChatMessage mensaje = ChatMessage.registrarMensaje(ana, grupoA, hola, relojConNanos);

            assertEquals(Instant.parse("2026-11-13T16:30:10.123Z"), mensaje.sentAt());
        }

        @Test
        @DisplayName("conserva el usuario, el grupo y el texto")
        void conservaLosDatos() {
            ChatMessage mensaje = ChatMessage.registrarMensaje(ana, grupoA, hola, RELOJ);

            assertAll(
                    () -> assertEquals(ana, mensaje.user()),
                    () -> assertEquals(grupoA, mensaje.group()),
                    () -> assertEquals(hola, mensaje.text()));
        }

        @Test
        @DisplayName("genera un identificador distinto para cada mensaje")
        void generaIdentificadorUnico() {
            ChatMessage primero = ChatMessage.registrarMensaje(ana, grupoA, hola, RELOJ);
            ChatMessage segundo = ChatMessage.registrarMensaje(ana, grupoA, hola, RELOJ);

            assertNotNull(primero.messageId());
            assertNotEquals(primero.messageId(), segundo.messageId());
        }

        @Test
        @DisplayName("expone la routing key de su grupo")
        void exponeRoutingKeyDelGrupo() {
            ChatMessage mensaje = ChatMessage.registrarMensaje(ana, grupoA, hola, RELOJ);

            String routingKey = mensaje.routingKey();

            assertEquals("chat.grupoa", routingKey);
        }
    }

    @Nested
    @DisplayName("Rechaza datos faltantes")
    class DatosFaltantes {

        @Test
        @DisplayName("sin usuario")
        void rechazaSinUsuario() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> ChatMessage.registrarMensaje(null, grupoA, hola, RELOJ));

            assertEquals("El usuario es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("sin grupo")
        void rechazaSinGrupo() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> ChatMessage.registrarMensaje(ana, null, hola, RELOJ));

            assertEquals("El grupo es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("sin texto")
        void rechazaSinTexto() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> ChatMessage.registrarMensaje(ana, grupoA, null, RELOJ));

            assertEquals("El texto del mensaje es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("sin reloj")
        void rechazaSinReloj() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> ChatMessage.registrarMensaje(ana, grupoA, hola, null));

            assertEquals("El reloj es obligatorio", error.getMessage());
        }
    }
}

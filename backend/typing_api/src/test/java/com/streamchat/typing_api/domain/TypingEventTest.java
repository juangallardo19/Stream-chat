package com.streamchat.typing_api.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypingEvent")
class TypingEventTest {

    private static final Instant INSTANTE = Instant.parse("2026-11-13T16:30:01.050Z");
    private static final Clock RELOJ = Clock.fixed(INSTANTE, ZoneOffset.UTC);

    @Nested
    @DisplayName("registrarTecleo()")
    class RegistrarTecleo {

        @Test
        @DisplayName("crea el evento con usuario, grupo y la hora del reloj")
        void creaEventoCompleto() {
            UserId usuario = new UserId("Ana");
            GroupId grupo = new GroupId("GrupoA");

            TypingEvent evento = TypingEvent.registrarTecleo(usuario, grupo, RELOJ);

            assertAll(
                    () -> assertEquals(usuario, evento.user()),
                    () -> assertEquals(grupo, evento.group()),
                    () -> assertEquals(INSTANTE, evento.occurredAt()));
        }

        @Test
        @DisplayName("recorta la hora a milisegundos")
        void recortaAMilisegundos() {
            Clock relojConNanos = Clock.fixed(Instant.parse("2026-11-13T16:30:01.050999999Z"), ZoneOffset.UTC);

            TypingEvent evento = TypingEvent.registrarTecleo(new UserId("Ana"), new GroupId("GrupoA"), relojConNanos);

            assertEquals(INSTANTE, evento.occurredAt());
        }

        @Test
        @DisplayName("rechaza un reloj nulo")
        void rechazaRelojNulo() {
            UserId usuario = new UserId("Ana");
            GroupId grupo = new GroupId("GrupoA");

            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> TypingEvent.registrarTecleo(usuario, grupo, null));

            assertEquals("El reloj es obligatorio", error.getMessage());
        }
    }

    @Nested
    @DisplayName("Al construirlo directamente")
    class Construccion {

        @Test
        @DisplayName("rechaza un evento sin usuario")
        void rechazaSinUsuario() {
            GroupId grupo = new GroupId("GrupoA");

            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> new TypingEvent(null, grupo, INSTANTE));

            assertEquals("El usuario es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("rechaza un evento sin grupo")
        void rechazaSinGrupo() {
            UserId usuario = new UserId("Ana");

            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> new TypingEvent(usuario, null, INSTANTE));

            assertEquals("El grupo es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("rechaza un evento sin hora")
        void rechazaSinHora() {
            UserId usuario = new UserId("Ana");
            GroupId grupo = new GroupId("GrupoA");

            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> new TypingEvent(usuario, grupo, null));

            assertEquals("La hora del evento es obligatoria", error.getMessage());
        }

        @Test
        @DisplayName("recorta a milisegundos también al construirlo directamente")
        void recortaAlConstruir() {
            Instant conNanos = Instant.parse("2026-11-13T16:30:01.050999999Z");

            TypingEvent evento = new TypingEvent(new UserId("Ana"), new GroupId("GrupoA"), conNanos);

            assertEquals(INSTANTE, evento.occurredAt());
        }
    }

    @Nested
    @DisplayName("claveParticion()")
    class ClaveParticion {

        @Test
        @DisplayName("es el nombre del grupo, para que los eventos de un grupo vayan a la misma partición")
        void esElGrupo() {
            TypingEvent evento = new TypingEvent(new UserId("Ana"), new GroupId("GrupoA"), INSTANTE);

            String clave = evento.claveParticion();

            assertEquals("GrupoA", clave);
        }
    }
}

package com.streamchat.typing_processor.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Eventos del dominio")
class EventsTest {

    private static final UserId ANA = new UserId("Ana");
    private static final GroupId GRUPO_A = new GroupId("GrupoA");

    @Nested
    @DisplayName("StatusChange")
    class Cambio {

        @Test
        @DisplayName("rechaza una transición sin estado")
        void rechazaSinEstado() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> new StatusChange(ANA, GRUPO_A, null));

            assertEquals("El estado es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("rechaza una transición sin usuario")
        void rechazaSinUsuario() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> new StatusChange(null, GRUPO_A, TypingStatus.TYPING));

            assertEquals("El usuario es obligatorio", error.getMessage());
        }
    }

    @Nested
    @DisplayName("TypingEvent")
    class Evento {

        @Test
        @DisplayName("rechaza un evento sin hora")
        void rechazaSinHora() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> new TypingEvent(ANA, GRUPO_A, null));

            assertEquals("La hora del evento es obligatoria", error.getMessage());
        }

        @Test
        @DisplayName("rechaza un evento sin grupo")
        void rechazaSinGrupo() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> new TypingEvent(ANA, null, Instant.parse("2026-11-13T16:30:00Z")));

            assertEquals("El grupo es obligatorio", error.getMessage());
        }
    }
}

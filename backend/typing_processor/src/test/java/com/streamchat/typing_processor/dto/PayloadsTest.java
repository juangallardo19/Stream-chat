package com.streamchat.typing_processor.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Payloads entre servicios")
class PayloadsTest {

    private static final Instant INSTANTE = Instant.parse("2026-11-13T16:30:01.050Z");

    @Nested
    @DisplayName("TypingEventPayload")
    class Entrada {

        @ParameterizedTest(name = "rechaza usuario [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {"  "})
        @DisplayName("rechaza un usuario vacío")
        void rechazaUsuarioVacio(String usuario) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> new TypingEventPayload(usuario, "GrupoA", INSTANTE));

            assertEquals("El usuario es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("rechaza un evento sin hora")
        void rechazaSinHora() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> new TypingEventPayload("Ana", "GrupoA", null));

            assertEquals("La hora del evento es obligatoria", error.getMessage());
        }
    }

    @Nested
    @DisplayName("StatusUpdatePayload")
    class Salida {

        @ParameterizedTest(name = "rechaza grupo [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {"  "})
        @DisplayName("rechaza un grupo vacío")
        void rechazaGrupoVacio(String grupo) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> new StatusUpdatePayload("Ana", grupo, "TYPING"));

            assertEquals("El grupo es obligatorio", error.getMessage());
        }

        @ParameterizedTest(name = "rechaza estado [{0}]")
        @ValueSource(strings = {"WRITING", "typing", ""})
        @DisplayName("solo admite TYPING o STOPPED")
        void soloDosEstados(String estado) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> new StatusUpdatePayload("Ana", "GrupoA", estado));

            assertEquals("El estado solo puede ser TYPING o STOPPED", error.getMessage());
        }
    }
}

package com.streamchat.typing_api.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypingEventPayload")
class TypingEventPayloadTest {

    private static final Instant INSTANTE = Instant.parse("2026-11-13T16:30:01.050Z");

    @ParameterizedTest(name = "rechaza usuario [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    @DisplayName("rechaza un usuario vacío")
    void rechazaUsuarioVacio(String usuario) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new TypingEventPayload(usuario, "GrupoA", INSTANTE));

        assertEquals("El usuario es obligatorio", error.getMessage());
    }

    @ParameterizedTest(name = "rechaza grupo [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    @DisplayName("rechaza un grupo vacío")
    void rechazaGrupoVacio(String grupo) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new TypingEventPayload("Ana", grupo, INSTANTE));

        assertEquals("El grupo es obligatorio", error.getMessage());
    }

    @Test
    @DisplayName("rechaza un payload sin hora")
    void rechazaSinHora() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> new TypingEventPayload("Ana", "GrupoA", null));

        assertEquals("La hora del evento es obligatoria", error.getMessage());
    }
}

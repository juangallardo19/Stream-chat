package com.streamchat.status_backend.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("StatusUpdatePayload")
class StatusUpdatePayloadTest {

    @ParameterizedTest(name = "rechaza usuario [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    @DisplayName("rechaza un usuario vacío")
    void rechazaUsuarioVacio(String usuario) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new StatusUpdatePayload(usuario, "GrupoA", "TYPING"));

        assertEquals("El usuario es obligatorio", error.getMessage());
    }

    @Test
    @DisplayName("rechaza un grupo vacío")
    void rechazaGrupoVacio() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new StatusUpdatePayload("Ana", " ", "TYPING"));

        assertEquals("El grupo es obligatorio", error.getMessage());
    }

    @ParameterizedTest(name = "rechaza estado [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"WRITING", "typing"})
    @DisplayName("solo admite TYPING o STOPPED")
    void soloDosEstados(String estado) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new StatusUpdatePayload("Ana", "GrupoA", estado));

        assertEquals("El estado solo puede ser TYPING o STOPPED", error.getMessage());
    }
}

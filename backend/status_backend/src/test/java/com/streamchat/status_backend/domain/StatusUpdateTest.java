package com.streamchat.status_backend.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("StatusUpdate")
class StatusUpdateTest {

    private static final UserId ANA = new UserId("Ana");
    private static final GroupId GRUPO_A = new GroupId("GrupoA");

    @Test
    @DisplayName("guarda usuario, grupo y estado")
    void guardaLosDatos() {
        StatusUpdate actualizacion = new StatusUpdate(ANA, GRUPO_A, TypingStatus.TYPING);

        assertEquals(TypingStatus.TYPING, actualizacion.status());
    }

    @Test
    @DisplayName("rechaza una actualización sin usuario")
    void rechazaSinUsuario() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> new StatusUpdate(null, GRUPO_A, TypingStatus.TYPING));

        assertEquals("El usuario es obligatorio", error.getMessage());
    }

    @Test
    @DisplayName("rechaza una actualización sin grupo")
    void rechazaSinGrupo() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> new StatusUpdate(ANA, null, TypingStatus.TYPING));

        assertEquals("El grupo es obligatorio", error.getMessage());
    }

    @Test
    @DisplayName("rechaza una actualización sin estado")
    void rechazaSinEstado() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> new StatusUpdate(ANA, GRUPO_A, null));

        assertEquals("El estado es obligatorio", error.getMessage());
    }
}

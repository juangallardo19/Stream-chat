package com.streamchat.chat_backend.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ChatMessagePayload")
class ChatMessagePayloadTest {

    private static final UUID IDENTIFICADOR = UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b");
    private static final Instant INSTANTE = Instant.parse("2026-11-13T16:30:10.123Z");

    @Test
    @DisplayName("rechaza un payload sin messageId")
    void rechazaSinIdentificador() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> new ChatMessagePayload(null, "Ana", "GrupoA", "¡Hola!", INSTANTE));

        assertEquals("El identificador del mensaje es obligatorio", error.getMessage());
    }

    @Test
    @DisplayName("rechaza un payload sin usuario")
    void rechazaSinUsuario() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> new ChatMessagePayload(IDENTIFICADOR, null, "GrupoA", "¡Hola!", INSTANTE));

        assertEquals("El usuario es obligatorio", error.getMessage());
    }

    @Test
    @DisplayName("rechaza un payload sin grupo")
    void rechazaSinGrupo() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> new ChatMessagePayload(IDENTIFICADOR, "Ana", null, "¡Hola!", INSTANTE));

        assertEquals("El grupo es obligatorio", error.getMessage());
    }

    @Test
    @DisplayName("rechaza un payload sin texto")
    void rechazaSinTexto() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> new ChatMessagePayload(IDENTIFICADOR, "Ana", "GrupoA", null, INSTANTE));

        assertEquals("El texto del mensaje es obligatorio", error.getMessage());
    }

    @Test
    @DisplayName("rechaza un payload sin hora de envío")
    void rechazaSinHora() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> new ChatMessagePayload(IDENTIFICADOR, "Ana", "GrupoA", "¡Hola!", null));

        assertEquals("La hora de envío es obligatoria", error.getMessage());
    }
}

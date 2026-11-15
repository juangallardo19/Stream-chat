package com.streamchat.chat_api.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SendMessageResponse")
class SendMessageResponseTest {

    private static final UUID IDENTIFICADOR = UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b");
    private static final Instant INSTANTE = Instant.parse("2026-11-13T16:30:10.123Z");

    @Test
    @DisplayName("conserva el identificador y la hora del mensaje")
    void conservaLosDatos() {
        SendMessageResponse respuesta = new SendMessageResponse(IDENTIFICADOR, INSTANTE);

        assertEquals(IDENTIFICADOR, respuesta.messageId());
        assertEquals(INSTANTE, respuesta.sentAt());
    }

    @Test
    @DisplayName("rechaza una respuesta sin identificador")
    void rechazaSinIdentificador() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> new SendMessageResponse(null, INSTANTE));

        assertEquals("El identificador del mensaje es obligatorio", error.getMessage());
    }

    @Test
    @DisplayName("rechaza una respuesta sin hora de envío")
    void rechazaSinHora() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> new SendMessageResponse(IDENTIFICADOR, null));

        assertEquals("La hora de envío es obligatoria", error.getMessage());
    }
}

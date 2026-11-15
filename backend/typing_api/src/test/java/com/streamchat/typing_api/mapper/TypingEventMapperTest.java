package com.streamchat.typing_api.mapper;

import com.streamchat.typing_api.domain.GroupId;
import com.streamchat.typing_api.domain.TypingEvent;
import com.streamchat.typing_api.domain.UserId;
import com.streamchat.typing_api.dto.TypingEventPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypingEventMapper")
class TypingEventMapperTest {

    @Test
    @DisplayName("convierte el evento de dominio en el payload con textos planos")
    void convierteAPayload() {
        Instant instante = Instant.parse("2026-11-13T16:30:01.050Z");
        TypingEvent evento = new TypingEvent(new UserId("Ana"), new GroupId("GrupoA"), instante);

        TypingEventPayload payload = TypingEventMapper.aPayload(evento);

        assertEquals(new TypingEventPayload("Ana", "GrupoA", instante), payload);
    }

    @Test
    @DisplayName("rechaza un evento nulo")
    void rechazaNulo() {
        NullPointerException error = assertThrows(NullPointerException.class, () -> TypingEventMapper.aPayload(null));

        assertEquals("El evento de tecleo es obligatorio", error.getMessage());
    }
}

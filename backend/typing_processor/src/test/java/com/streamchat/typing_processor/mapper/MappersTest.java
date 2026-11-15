package com.streamchat.typing_processor.mapper;

import com.streamchat.typing_processor.domain.GroupId;
import com.streamchat.typing_processor.domain.StatusChange;
import com.streamchat.typing_processor.domain.TypingEvent;
import com.streamchat.typing_processor.domain.TypingStatus;
import com.streamchat.typing_processor.domain.UserId;
import com.streamchat.typing_processor.dto.StatusUpdatePayload;
import com.streamchat.typing_processor.dto.TypingEventPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Mappers")
class MappersTest {

    @Test
    @DisplayName("TypingEventMapper convierte el JSON recibido en un evento de dominio")
    void eventoADominio() {
        Instant instante = Instant.parse("2026-11-13T16:30:01.050Z");
        TypingEventPayload payload = new TypingEventPayload("Ana", "GrupoA", instante);

        TypingEvent evento = TypingEventMapper.aDominio(payload);

        assertEquals(new TypingEvent(new UserId("Ana"), new GroupId("GrupoA"), instante), evento);
    }

    @Test
    @DisplayName("TypingEventMapper rechaza un grupo con formato inválido")
    void eventoConGrupoInvalido() {
        TypingEventPayload payload = new TypingEventPayload("Ana", "Grupo.A", Instant.parse("2026-11-13T16:30:01Z"));

        assertThrows(IllegalArgumentException.class, () -> TypingEventMapper.aDominio(payload));
    }

    @Test
    @DisplayName("StatusChangeMapper convierte la transición en el payload del contrato")
    void cambioAPayload() {
        StatusChange cambio = new StatusChange(new UserId("Ana"), new GroupId("GrupoA"), TypingStatus.TYPING);

        StatusUpdatePayload payload = StatusChangeMapper.aPayload(cambio);

        assertEquals(new StatusUpdatePayload("Ana", "GrupoA", "TYPING"), payload);
    }

    @Test
    @DisplayName("los mappers rechazan un valor nulo")
    void rechazanNulo() {
        NullPointerException entrada = assertThrows(NullPointerException.class, () -> TypingEventMapper.aDominio(null));
        NullPointerException salida = assertThrows(NullPointerException.class, () -> StatusChangeMapper.aPayload(null));

        assertEquals("El payload es obligatorio", entrada.getMessage());
        assertEquals("La transición de estado es obligatoria", salida.getMessage());
    }
}

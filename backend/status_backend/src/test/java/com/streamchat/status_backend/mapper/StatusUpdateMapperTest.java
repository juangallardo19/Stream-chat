package com.streamchat.status_backend.mapper;

import com.streamchat.status_backend.domain.GroupId;
import com.streamchat.status_backend.domain.StatusUpdate;
import com.streamchat.status_backend.domain.TypingStatus;
import com.streamchat.status_backend.domain.UserId;
import com.streamchat.status_backend.dto.StatusUpdatePayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("StatusUpdateMapper")
class StatusUpdateMapperTest {

    private final StatusUpdate dominio = new StatusUpdate(new UserId("Ana"), new GroupId("GrupoA"), TypingStatus.STOPPED);
    private final StatusUpdatePayload payload = new StatusUpdatePayload("Ana", "GrupoA", "STOPPED");

    @Test
    @DisplayName("convierte el JSON recibido en el objeto de dominio")
    void aDominio() {
        StatusUpdate resultado = StatusUpdateMapper.aDominio(payload);

        assertEquals(dominio, resultado);
    }

    @Test
    @DisplayName("convierte el objeto de dominio en el JSON para el navegador")
    void aPayload() {
        StatusUpdatePayload resultado = StatusUpdateMapper.aPayload(dominio);

        assertEquals(payload, resultado);
    }

    @Test
    @DisplayName("rechaza valores nulos")
    void rechazaNulos() {
        NullPointerException entrada = assertThrows(NullPointerException.class, () -> StatusUpdateMapper.aDominio(null));
        NullPointerException salida = assertThrows(NullPointerException.class, () -> StatusUpdateMapper.aPayload(null));

        assertEquals("El payload es obligatorio", entrada.getMessage());
        assertEquals("La actualización de estado es obligatoria", salida.getMessage());
    }
}

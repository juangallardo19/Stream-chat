package com.streamchat.analytics_service.mapper;

import com.streamchat.analytics_service.domain.AuditedMessage;
import com.streamchat.analytics_service.dto.ChatMessagePayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AuditedMessageMapper")
class AuditedMessageMapperTest {

    private static final UUID ID = UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b");
    private static final Instant ENVIADO = Instant.parse("2026-11-13T16:30:10.123Z");

    @Test
    @DisplayName("convierte el payload de Kafka en un mensaje auditado")
    void payloadADominio() {
        ChatMessagePayload payload = new ChatMessagePayload(ID, "Ana", "GrupoA", "¡Hola!", ENVIADO);

        AuditedMessage mensaje = AuditedMessageMapper.aDominio(payload);

        assertEquals(new AuditedMessage(ID, "Ana", "GrupoA", "¡Hola!", ENVIADO), mensaje);
    }

    @Test
    @DisplayName("rechaza un payload nulo")
    void rechazaNulo() {
        NullPointerException error = assertThrows(NullPointerException.class, () -> AuditedMessageMapper.aDominio(null));

        assertEquals("El payload es obligatorio", error.getMessage());
    }
}

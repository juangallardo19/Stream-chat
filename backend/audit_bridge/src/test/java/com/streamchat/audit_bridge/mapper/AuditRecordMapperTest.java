package com.streamchat.audit_bridge.mapper;

import com.streamchat.audit_bridge.domain.AuditRecord;
import com.streamchat.audit_bridge.dto.ChatMessagePayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AuditRecordMapper")
class AuditRecordMapperTest {

    private static final UUID ID = UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b");
    private static final Instant ENVIADO = Instant.parse("2026-11-13T16:30:10.123Z");

    @Test
    @DisplayName("convierte el payload en un registro de auditoría")
    void payloadADominio() {
        ChatMessagePayload payload = new ChatMessagePayload(ID, "Ana", "GrupoA", "¡Hola!", ENVIADO);

        AuditRecord registro = AuditRecordMapper.aDominio(payload);

        assertEquals(new AuditRecord(ID, "Ana", "GrupoA", "¡Hola!", ENVIADO), registro);
    }

    @Test
    @DisplayName("convierte el registro en el payload que viaja a Kafka")
    void dominioAPayload() {
        AuditRecord registro = new AuditRecord(ID, "Ana", "GrupoA", "¡Hola!", ENVIADO);

        ChatMessagePayload payload = AuditRecordMapper.aPayload(registro);

        assertEquals(new ChatMessagePayload(ID, "Ana", "GrupoA", "¡Hola!", ENVIADO), payload);
    }

    @Test
    @DisplayName("rechaza un payload nulo")
    void rechazaPayloadNulo() {
        NullPointerException error = assertThrows(NullPointerException.class, () -> AuditRecordMapper.aDominio(null));

        assertEquals("El payload es obligatorio", error.getMessage());
    }

    @Test
    @DisplayName("rechaza un registro nulo")
    void rechazaRegistroNulo() {
        NullPointerException error = assertThrows(NullPointerException.class, () -> AuditRecordMapper.aPayload(null));

        assertEquals("El registro de auditoría es obligatorio", error.getMessage());
    }
}

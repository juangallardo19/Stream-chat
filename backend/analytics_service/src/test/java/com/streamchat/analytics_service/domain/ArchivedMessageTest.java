package com.streamchat.analytics_service.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ArchivedMessage")
class ArchivedMessageTest {

    private static final UUID ID = UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b");
    private static final Instant ENVIADO = Instant.parse("2026-11-13T16:30:10.123Z");
    private static final Instant ARCHIVADO = Instant.parse("2026-11-13T16:30:11.456Z");
    private static final AuditedMessage AUDITADO = new AuditedMessage(ID, "Ana", "GrupoA", "¡Hola!", ENVIADO);

    @Test
    @DisplayName("copia los datos del mensaje auditado y la hora de archivo")
    void copiaLosDatos() {
        ArchivedMessage archivado = ArchivedMessage.archivar(AUDITADO, ARCHIVADO);

        assertEquals(ID, archivado.getMessageId());
        assertEquals("GrupoA", archivado.getGroupId());
        assertEquals("Ana", archivado.getUserId());
        assertEquals("¡Hola!", archivado.getText());
        assertEquals(ENVIADO, archivado.getSentAt());
        assertEquals(ARCHIVADO, archivado.getArchivedAt());
    }

    @Test
    @DisplayName("recorta la hora de archivo a milisegundos")
    void recortaHoraDeArchivo() {
        ArchivedMessage archivado = ArchivedMessage.archivar(AUDITADO, Instant.parse("2026-11-13T16:30:11.456789Z"));

        assertEquals(ARCHIVADO, archivado.getArchivedAt());
    }

    @Test
    @DisplayName("rechaza un mensaje auditado nulo")
    void rechazaMensajeNulo() {
        NullPointerException error = assertThrows(NullPointerException.class, () -> ArchivedMessage.archivar(null, ARCHIVADO));

        assertEquals("El mensaje auditado es obligatorio", error.getMessage());
    }

    @Test
    @DisplayName("rechaza una hora de archivo nula")
    void rechazaHoraNula() {
        NullPointerException error = assertThrows(NullPointerException.class, () -> ArchivedMessage.archivar(AUDITADO, null));

        assertEquals("La hora de archivo es obligatoria", error.getMessage());
    }
}

package com.streamchat.audit_bridge.service;

import com.streamchat.audit_bridge.domain.AuditRecord;
import com.streamchat.audit_bridge.port.AuditLogPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditBridgeService")
class AuditBridgeServiceTest {

    @Mock
    private AuditLogPublisher publicador;

    @InjectMocks
    private AuditBridgeService servicio;

    private final AuditRecord registro = new AuditRecord(
            UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b"),
            "Ana",
            "GrupoA",
            "¡Hola!",
            Instant.parse("2026-11-13T16:30:10.123Z"));

    @Nested
    @DisplayName("Con un registro válido")
    class RegistroValido {

        @Test
        @DisplayName("lo publica en el log de auditoría exactamente una vez")
        void publicaUnaVez() {
            servicio.transferir(registro);

            verify(publicador, times(1)).publicar(registro);
        }
    }

    @Nested
    @DisplayName("Con errores")
    class Errores {

        @Test
        @DisplayName("rechaza un registro nulo y no publica nada")
        void rechazaNulo() {
            NullPointerException error = assertThrows(NullPointerException.class, () -> servicio.transferir(null));

            assertEquals("El registro de auditoría es obligatorio", error.getMessage());
            verify(publicador, never()).publicar(any());
        }

        @Test
        @DisplayName("si Kafka falla, propaga el error para que RabbitMQ no reciba el ack")
        void propagaErrorDeKafka() {
            doThrow(new IllegalStateException("Kafka no confirmó")).when(publicador).publicar(registro);

            IllegalStateException error = assertThrows(IllegalStateException.class, () -> servicio.transferir(registro));

            assertEquals("Kafka no confirmó", error.getMessage());
        }
    }
}

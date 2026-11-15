package com.streamchat.analytics_service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.streamchat.analytics_service.domain.AuditedMessage;
import com.streamchat.analytics_service.service.ArchiveMessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogListener")
class AuditLogListenerTest {

    private static final String JSON_VALIDO = "{\"messageId\":\"3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b\",\"user\":\"Ana\","
            + "\"group\":\"GrupoA\",\"text\":\"¡Hola!\",\"sentAt\":\"2026-11-13T16:30:10.123Z\"}";

    @Mock
    private ArchiveMessageService servicio;

    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @InjectMocks
    private AuditLogListener listener;

    @Nested
    @DisplayName("Con un registro bien formado")
    class RegistroValido {

        @Test
        @DisplayName("lo convierte y lo archiva una vez")
        void archivaUnaVez() {
            AuditedMessage esperado = new AuditedMessage(UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b"),
                    "Ana", "GrupoA", "¡Hola!", Instant.parse("2026-11-13T16:30:10.123Z"));

            listener.recibir(JSON_VALIDO);

            verify(servicio, times(1)).archivar(esperado);
        }

        @Test
        @DisplayName("si la base de datos falla, propaga el error para que Kafka no avance el offset")
        void propagaFalloDeBaseDeDatos() {
            doThrow(new IllegalStateException("Base de datos caída")).when(servicio).archivar(any());

            IllegalStateException error = assertThrows(IllegalStateException.class, () -> listener.recibir(JSON_VALIDO));

            assertEquals("Base de datos caída", error.getMessage());
        }
    }

    @Nested
    @DisplayName("Con un registro mal formado")
    class RegistroInvalido {

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "esto no es JSON",
                "{\"user\":\"Ana\",\"group\":\"GrupoA\",\"text\":\"¡Hola!\",\"sentAt\":\"2026-11-13T16:30:10.123Z\"}",
                "{\"messageId\":\"3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b\",\"user\":\"Ana\",\"group\":\"Grupo.A\",\"text\":\"¡Hola!\",\"sentAt\":\"2026-11-13T16:30:10.123Z\"}"
        })
        @DisplayName("lo salta sin lanzar error, para no trancar la partición, y no archiva nada")
        void loSalta(String json) {
            assertDoesNotThrow(() -> listener.recibir(json));

            verify(servicio, never()).archivar(any());
        }
    }
}

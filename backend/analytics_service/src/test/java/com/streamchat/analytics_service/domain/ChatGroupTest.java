package com.streamchat.analytics_service.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ChatGroup")
class ChatGroupTest {

    private static final Instant ARCHIVADO = Instant.parse("2026-11-13T16:31:00.000Z");

    private static ArchivedMessage mensaje(String grupo, String enviado) {
        AuditedMessage auditado = new AuditedMessage(UUID.randomUUID(), "Ana", grupo, "¡Hola!", Instant.parse(enviado));
        return ArchivedMessage.archivar(auditado, ARCHIVADO);
    }

    @Nested
    @DisplayName("Al crearse")
    class AlCrearse {

        @Test
        @DisplayName("empieza con cero mensajes y sin fechas")
        void empiezaVacio() {
            ChatGroup grupo = ChatGroup.nuevo("GrupoA");

            assertEquals("GrupoA", grupo.getGroupId());
            assertEquals(0, grupo.getTotalMessages());
            assertEquals(Optional.empty(), grupo.getFirstMessageAt());
            assertEquals(Optional.empty(), grupo.getLastMessageAt());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("rechaza un identificador vacío")
        void rechazaIdentificadorVacio(String identificador) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> ChatGroup.nuevo(identificador));

            assertEquals("El grupo es obligatorio", error.getMessage());
        }
    }

    @Nested
    @DisplayName("registrarMensaje")
    class RegistrarMensaje {

        @Test
        @DisplayName("el primer mensaje deja el total en 1 y fija las dos fechas")
        void primerMensaje() {
            ChatGroup grupo = ChatGroup.nuevo("GrupoA");

            grupo.registrarMensaje(mensaje("GrupoA", "2026-11-13T16:30:10.123Z"));

            assertEquals(1, grupo.getTotalMessages());
            assertEquals(Optional.of(Instant.parse("2026-11-13T16:30:10.123Z")), grupo.getFirstMessageAt());
            assertEquals(Optional.of(Instant.parse("2026-11-13T16:30:10.123Z")), grupo.getLastMessageAt());
        }

        @Test
        @DisplayName("varios mensajes acumulan el total y conservan la fecha más antigua y la más reciente")
        void variosMensajes() {
            ChatGroup grupo = ChatGroup.nuevo("GrupoA");

            grupo.registrarMensaje(mensaje("GrupoA", "2026-11-13T16:30:20.000Z"));
            grupo.registrarMensaje(mensaje("GrupoA", "2026-11-13T16:30:10.000Z"));
            grupo.registrarMensaje(mensaje("GrupoA", "2026-11-13T16:30:30.000Z"));

            assertEquals(3, grupo.getTotalMessages());
            assertEquals(Optional.of(Instant.parse("2026-11-13T16:30:10.000Z")), grupo.getFirstMessageAt());
            assertEquals(Optional.of(Instant.parse("2026-11-13T16:30:30.000Z")), grupo.getLastMessageAt());
        }

        @Test
        @DisplayName("rechaza un mensaje nulo")
        void rechazaNulo() {
            ChatGroup grupo = ChatGroup.nuevo("GrupoA");

            NullPointerException error = assertThrows(NullPointerException.class, () -> grupo.registrarMensaje(null));

            assertEquals("El mensaje es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("rechaza un mensaje de otro grupo y no cambia el total")
        void rechazaOtroGrupo() {
            ChatGroup grupo = ChatGroup.nuevo("GrupoA");
            ArchivedMessage deOtroGrupo = mensaje("GrupoB", "2026-11-13T16:30:10.000Z");

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> grupo.registrarMensaje(deOtroGrupo));

            assertEquals("El mensaje pertenece al grupo GrupoB y no a GrupoA", error.getMessage());
            assertEquals(0, grupo.getTotalMessages());
        }
    }
}

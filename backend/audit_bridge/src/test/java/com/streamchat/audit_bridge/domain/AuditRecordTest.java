package com.streamchat.audit_bridge.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AuditRecord")
class AuditRecordTest {

    private static final UUID ID = UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b");
    private static final Instant ENVIADO = Instant.parse("2026-11-13T16:30:10.123Z");

    @Nested
    @DisplayName("Con datos válidos")
    class Valido {

        @Test
        @DisplayName("conserva todos los campos del mensaje")
        void conservaCampos() {
            AuditRecord registro = new AuditRecord(ID, "Ana", "GrupoA", "¡Hola!", ENVIADO);

            assertEquals(ID, registro.messageId());
            assertEquals("Ana", registro.user());
            assertEquals("GrupoA", registro.group());
            assertEquals("¡Hola!", registro.text());
            assertEquals(ENVIADO, registro.sentAt());
        }

        @Test
        @DisplayName("usa el grupo como clave de partición de Kafka")
        void claveEsElGrupo() {
            AuditRecord registro = new AuditRecord(ID, "Ana", "GrupoA", "¡Hola!", ENVIADO);

            String clave = registro.claveParticion();

            assertEquals("GrupoA", clave);
        }

        @Test
        @DisplayName("recorta la hora de envío a milisegundos")
        void recortaAMilisegundos() {
            Instant conNanos = Instant.parse("2026-11-13T16:30:10.123456789Z");

            AuditRecord registro = new AuditRecord(ID, "Ana", "GrupoA", "¡Hola!", conNanos);

            assertEquals(ENVIADO, registro.sentAt());
        }
    }

    @Nested
    @DisplayName("Con datos inválidos")
    class Invalido {

        @Test
        @DisplayName("rechaza un identificador de mensaje nulo")
        void rechazaIdNulo() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> new AuditRecord(null, "Ana", "GrupoA", "¡Hola!", ENVIADO));

            assertEquals("El identificador del mensaje es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("rechaza una hora de envío nula")
        void rechazaHoraNula() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> new AuditRecord(ID, "Ana", "GrupoA", "¡Hola!", null));

            assertEquals("La hora de envío es obligatoria", error.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("rechaza un usuario vacío")
        void rechazaUsuarioVacio(String usuario) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> new AuditRecord(ID, usuario, "GrupoA", "¡Hola!", ENVIADO));

            assertEquals("El usuario es obligatorio", error.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"grupo.a", "grupo#", "grupo*", "con espacio"})
        @DisplayName("rechaza un grupo con caracteres prohibidos")
        void rechazaGrupoInvalido(String grupo) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> new AuditRecord(ID, "Ana", grupo, "¡Hola!", ENVIADO));

            assertEquals("El grupo solo admite letras, números, guion y guion bajo", error.getMessage());
        }

        @Test
        @DisplayName("rechaza un usuario de más de 40 caracteres")
        void rechazaUsuarioLargo() {
            String largo = "a".repeat(41);

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> new AuditRecord(ID, largo, "GrupoA", "¡Hola!", ENVIADO));

            assertEquals("El usuario admite como máximo 40 caracteres", error.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("rechaza un texto vacío")
        void rechazaTextoVacio(String texto) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> new AuditRecord(ID, "Ana", "GrupoA", texto, ENVIADO));

            assertEquals("El texto del mensaje es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("rechaza un texto de más de 1000 caracteres")
        void rechazaTextoLargo() {
            String largo = "a".repeat(1001);

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> new AuditRecord(ID, "Ana", "GrupoA", largo, ENVIADO));

            assertEquals("El texto del mensaje admite como máximo 1000 caracteres", error.getMessage());
        }
    }
}

package com.streamchat.typing_processor.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ChatGroup (agregado en memoria)")
class ChatGroupTest {

    private static final Instant INICIO = Instant.parse("2026-11-13T16:30:00Z");
    private static final GroupId GRUPO_A = new GroupId("GrupoA");
    private static final UserId ANA = new UserId("Ana");
    private static final UserId LUIS = new UserId("Luis");
    private static final InactivityStrategy VENTANA_4S = new FixedWindowStrategy(Duration.ofSeconds(4));

    private final ChatGroup grupo = ChatGroup.nuevo(GRUPO_A);

    @Nested
    @DisplayName("registrarActividad()")
    class RegistrarActividad {

        @Test
        @DisplayName("la primera pulsación de un usuario produce TYPING")
        void primeraEsTyping() {
            Optional<StatusChange> cambio = grupo.registrarActividad(ANA, INICIO);

            assertEquals(Optional.of(new StatusChange(ANA, GRUPO_A, TypingStatus.TYPING)), cambio);
        }

        @Test
        @DisplayName("dos usuarios del mismo grupo son independientes")
        void usuariosIndependientes() {
            grupo.registrarActividad(ANA, INICIO);

            Optional<StatusChange> cambioLuis = grupo.registrarActividad(LUIS, INICIO.plusMillis(500));

            assertEquals(Optional.of(new StatusChange(LUIS, GRUPO_A, TypingStatus.TYPING)), cambioLuis);
            assertEquals(2, grupo.usuariosActivos());
        }
    }

    @Nested
    @DisplayName("evaluarInactividad()")
    class EvaluarInactividad {

        @Test
        @DisplayName("solo detiene a quien superó la ventana")
        void soloDetieneAlInactivo() {
            grupo.registrarActividad(ANA, INICIO);
            grupo.registrarActividad(LUIS, INICIO.plusSeconds(3));

            List<StatusChange> cambios = grupo.evaluarInactividad(INICIO.plusSeconds(5), VENTANA_4S);

            assertEquals(List.of(new StatusChange(ANA, GRUPO_A, TypingStatus.STOPPED)), cambios);
        }

        @Test
        @DisplayName("un usuario detenido deja de ocupar memoria")
        void liberaAlDetenido() {
            grupo.registrarActividad(ANA, INICIO);

            grupo.evaluarInactividad(INICIO.plusSeconds(4), VENTANA_4S);

            assertEquals(0, grupo.usuariosActivos());
        }

        @Test
        @DisplayName("sin usuarios devuelve una lista vacía, nunca null")
        void sinUsuariosListaVacia() {
            List<StatusChange> cambios = grupo.evaluarInactividad(INICIO, VENTANA_4S);

            assertTrue(cambios.isEmpty());
        }
    }

    @Test
    @DisplayName("rechaza un grupo nulo")
    void rechazaGrupoNulo() {
        NullPointerException error = assertThrows(NullPointerException.class, () -> ChatGroup.nuevo(null));

        assertEquals("El grupo es obligatorio", error.getMessage());
    }
}

package com.streamchat.typing_processor.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FixedWindowStrategy (patrón Strategy)")
class FixedWindowStrategyTest {

    private static final Instant ULTIMA = Instant.parse("2026-11-13T16:30:00Z");

    private final FixedWindowStrategy estrategia = new FixedWindowStrategy(Duration.ofSeconds(4));

    @Nested
    @DisplayName("estaInactivo()")
    class EstaInactivo {

        @Test
        @DisplayName("antes de la ventana no está inactivo")
        void antesDeLaVentana() {
            Instant ahora = ULTIMA.plusMillis(3999);

            boolean inactivo = estrategia.estaInactivo(ULTIMA, ahora);

            assertFalse(inactivo);
        }

        @Test
        @DisplayName("justo al cumplirse la ventana está inactivo")
        void justoEnLaVentana() {
            Instant ahora = ULTIMA.plusSeconds(4);

            boolean inactivo = estrategia.estaInactivo(ULTIMA, ahora);

            assertTrue(inactivo);
        }

        @Test
        @DisplayName("después de la ventana está inactivo")
        void despuesDeLaVentana() {
            Instant ahora = ULTIMA.plusSeconds(10);

            boolean inactivo = estrategia.estaInactivo(ULTIMA, ahora);

            assertTrue(inactivo);
        }
    }

    @Nested
    @DisplayName("Al construirla")
    class Construccion {

        @Test
        @DisplayName("rechaza una ventana nula")
        void rechazaNula() {
            NullPointerException error = assertThrows(NullPointerException.class, () -> new FixedWindowStrategy(null));

            assertEquals("La ventana de inactividad es obligatoria", error.getMessage());
        }

        @Test
        @DisplayName("rechaza una ventana de cero o negativa")
        void rechazaCeroONegativa() {
            Duration cero = Duration.ZERO;

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> new FixedWindowStrategy(cero));

            assertEquals("La ventana de inactividad debe ser mayor que cero", error.getMessage());
        }
    }
}

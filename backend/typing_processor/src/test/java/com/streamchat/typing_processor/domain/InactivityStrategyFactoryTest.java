package com.streamchat.typing_processor.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("InactivityStrategyFactory (patrón Factory)")
class InactivityStrategyFactoryTest {

    private static final Duration VENTANA = Duration.ofMillis(4000);

    @ParameterizedTest(name = "{0} crea la ventana fija")
    @ValueSource(strings = {"fixed", "FIXED", " Fixed "})
    @DisplayName("crea FixedWindowStrategy con la ventana configurada, sin distinguir mayúsculas")
    void creaVentanaFija(String nombre) {
        InactivityStrategy estrategia = InactivityStrategyFactory.crear(nombre, VENTANA);

        FixedWindowStrategy fija = assertInstanceOf(FixedWindowStrategy.class, estrategia);
        assertEquals(VENTANA, fija.ventana());
    }

    @Test
    @DisplayName("rechaza una estrategia desconocida para que el servicio no arranque")
    void rechazaDesconocida() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> InactivityStrategyFactory.crear("magica", VENTANA));

        assertEquals("Estrategia de inactividad desconocida: magica", error.getMessage());
    }

    @Test
    @DisplayName("rechaza un nombre nulo")
    void rechazaNulo() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> InactivityStrategyFactory.crear(null, VENTANA));

        assertEquals("El nombre de la estrategia es obligatorio", error.getMessage());
    }
}

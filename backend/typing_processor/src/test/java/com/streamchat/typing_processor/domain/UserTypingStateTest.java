package com.streamchat.typing_processor.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("UserTypingState")
class UserTypingStateTest {

    private static final Instant INICIO = Instant.parse("2026-11-13T16:30:00Z");
    private static final UserId ANA = new UserId("Ana");
    private static final GroupId GRUPO_A = new GroupId("GrupoA");
    private static final InactivityStrategy VENTANA_4S = new FixedWindowStrategy(Duration.ofSeconds(4));

    private final UserTypingState estado = UserTypingState.nuevo(ANA, GRUPO_A);

    @Nested
    @DisplayName("Al crearlo")
    class Creacion {

        @Test
        @DisplayName("empieza en STOPPED")
        void empiezaDetenido() {
            TypingStatus status = estado.status();

            assertEquals(TypingStatus.STOPPED, status);
        }

        @Test
        @DisplayName("rechaza un usuario nulo")
        void rechazaUsuarioNulo() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> UserTypingState.nuevo(null, GRUPO_A));

            assertEquals("El usuario es obligatorio", error.getMessage());
        }
    }

    @Nested
    @DisplayName("registrarPulsacion()")
    class RegistrarPulsacion {

        @Test
        @DisplayName("la primera pulsación produce la transición a TYPING")
        void primeraPulsacionEsTyping() {
            Optional<StatusChange> cambio = estado.registrarPulsacion(INICIO);

            assertEquals(Optional.of(new StatusChange(ANA, GRUPO_A, TypingStatus.TYPING)), cambio);
        }

        @Test
        @DisplayName("10 pulsaciones seguidas producen un solo TYPING")
        void diezPulsacionesUnSoloTyping() {
            List<StatusChange> cambios = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                estado.registrarPulsacion(INICIO.plusMillis(i * 100L)).ifPresent(cambios::add);
            }

            assertEquals(List.of(new StatusChange(ANA, GRUPO_A, TypingStatus.TYPING)), cambios);
        }
    }

    @Nested
    @DisplayName("evaluarEstadoTyping()")
    class EvaluarEstado {

        @Test
        @DisplayName("antes de la ventana no hay transición")
        void antesDeLaVentanaNoCambia() {
            estado.registrarPulsacion(INICIO);

            Optional<StatusChange> cambio = estado.evaluarEstadoTyping(INICIO.plusSeconds(3), VENTANA_4S);

            assertTrue(cambio.isEmpty());
            assertEquals(TypingStatus.TYPING, estado.status());
        }

        @Test
        @DisplayName("tras la ventana produce un solo STOPPED")
        void trasLaVentanaUnSoloStopped() {
            estado.registrarPulsacion(INICIO);

            Optional<StatusChange> primero = estado.evaluarEstadoTyping(INICIO.plusSeconds(4), VENTANA_4S);
            Optional<StatusChange> segundo = estado.evaluarEstadoTyping(INICIO.plusSeconds(8), VENTANA_4S);

            assertEquals(Optional.of(new StatusChange(ANA, GRUPO_A, TypingStatus.STOPPED)), primero);
            assertTrue(segundo.isEmpty());
        }

        @Test
        @DisplayName("la ventana se cuenta desde la última pulsación, no desde la primera")
        void cuentaDesdeLaUltimaPulsacion() {
            estado.registrarPulsacion(INICIO);
            estado.registrarPulsacion(INICIO.plusSeconds(3));

            Optional<StatusChange> cambio = estado.evaluarEstadoTyping(INICIO.plusSeconds(5), VENTANA_4S);

            assertTrue(cambio.isEmpty());
        }

        @Test
        @DisplayName("un usuario que nunca tecleó no produce transición")
        void sinPulsacionesNoCambia() {
            Optional<StatusChange> cambio = estado.evaluarEstadoTyping(INICIO.plusSeconds(10), VENTANA_4S);

            assertTrue(cambio.isEmpty());
        }

        @Test
        @DisplayName("después de STOPPED, volver a teclear produce otro TYPING")
        void vuelveATeclear() {
            estado.registrarPulsacion(INICIO);
            estado.evaluarEstadoTyping(INICIO.plusSeconds(4), VENTANA_4S);

            Optional<StatusChange> cambio = estado.registrarPulsacion(INICIO.plusSeconds(6));

            assertEquals(Optional.of(new StatusChange(ANA, GRUPO_A, TypingStatus.TYPING)), cambio);
        }
    }
}

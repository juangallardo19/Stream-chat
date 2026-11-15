package com.streamchat.typing_processor.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("ActivityState (patrón State)")
class ActivityStateTest {

    @Nested
    @DisplayName("StoppedState")
    class Detenido {

        private final StoppedState detenido = new StoppedState();

        @Test
        @DisplayName("su estado es STOPPED")
        void esStopped() {
            TypingStatus status = detenido.status();

            assertEquals(TypingStatus.STOPPED, status);
        }

        @Test
        @DisplayName("al teclear pasa a TypingState")
        void alTeclearPasaATyping() {
            ActivityState siguiente = detenido.alTeclear();

            assertInstanceOf(TypingState.class, siguiente);
        }

        @Test
        @DisplayName("al expirar la ventana se queda igual")
        void alExpirarSeQueda() {
            ActivityState siguiente = detenido.alExpirar();

            assertSame(detenido, siguiente);
        }
    }

    @Nested
    @DisplayName("TypingState")
    class Escribiendo {

        private final TypingState escribiendo = new TypingState();

        @Test
        @DisplayName("su estado es TYPING")
        void esTyping() {
            TypingStatus status = escribiendo.status();

            assertEquals(TypingStatus.TYPING, status);
        }

        @Test
        @DisplayName("al teclear se queda igual (debounce)")
        void alTeclearSeQueda() {
            ActivityState siguiente = escribiendo.alTeclear();

            assertSame(escribiendo, siguiente);
        }

        @Test
        @DisplayName("al expirar la ventana pasa a StoppedState")
        void alExpirarPasaAStopped() {
            ActivityState siguiente = escribiendo.alExpirar();

            assertInstanceOf(StoppedState.class, siguiente);
        }
    }
}

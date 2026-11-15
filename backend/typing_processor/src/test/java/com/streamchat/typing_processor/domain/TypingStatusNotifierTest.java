package com.streamchat.typing_processor.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypingStatusNotifier (patrón Observer)")
class TypingStatusNotifierTest {

    private static final StatusChange CAMBIO =
            new StatusChange(new UserId("Ana"), new GroupId("GrupoA"), TypingStatus.TYPING);

    private static final class ObservadorDePrueba implements TypingStatusObserver {

        private final List<StatusChange> recibidos = new ArrayList<>();

        @Override
        public void alCambiarEstado(StatusChange cambio) {
            recibidos.add(cambio);
        }
    }

    @Test
    @DisplayName("avisa a todos sus observadores una sola vez por transición")
    void avisaATodosUnaVez() {
        ObservadorDePrueba primero = new ObservadorDePrueba();
        ObservadorDePrueba segundo = new ObservadorDePrueba();
        TypingStatusNotifier notifier = new TypingStatusNotifier(List.of(primero, segundo));

        notifier.notificar(CAMBIO);

        assertEquals(List.of(CAMBIO), primero.recibidos);
        assertEquals(List.of(CAMBIO), segundo.recibidos);
    }

    @Test
    @DisplayName("guarda una copia: agregar observadores a la lista original después no cambia a quién avisa")
    void copiaDefensiva() {
        ObservadorDePrueba primero = new ObservadorDePrueba();
        ObservadorDePrueba tardio = new ObservadorDePrueba();
        List<TypingStatusObserver> lista = new ArrayList<>(List.of(primero));
        TypingStatusNotifier notifier = new TypingStatusNotifier(lista);
        lista.add(tardio);

        notifier.notificar(CAMBIO);

        assertEquals(List.of(), tardio.recibidos);
    }

    @Test
    @DisplayName("rechaza una transición nula")
    void rechazaNula() {
        TypingStatusNotifier notifier = new TypingStatusNotifier(List.of(new ObservadorDePrueba()));

        NullPointerException error = assertThrows(NullPointerException.class, () -> notifier.notificar(null));

        assertEquals("La transición de estado es obligatoria", error.getMessage());
    }
}

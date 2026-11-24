package com.streamchat.typing_processor.domain;

import java.util.List;
import java.util.Objects;

public class TypingStatusNotifier {

    private final List<TypingStatusObserver> observadores;

    public TypingStatusNotifier(List<TypingStatusObserver> observadores) {
        this.observadores = List.copyOf(observadores);
    }

    public void notificar(StatusChange cambio) {
        Objects.requireNonNull(cambio, "La transición de estado es obligatoria");
        observadores.forEach(observador -> observador.alCambiarEstado(cambio));
    }
}

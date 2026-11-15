package com.streamchat.typing_processor.domain;

import java.time.Duration;
import java.time.Instant;

public final class FixedWindowStrategy implements InactivityStrategy {

    private final Duration ventana;

    public FixedWindowStrategy(Duration ventana) {
        this.ventana = ventana;
    }

    public Duration ventana() {
        return ventana;
    }

    @Override
    public boolean estaInactivo(Instant ultimaActividad, Instant ahora) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}

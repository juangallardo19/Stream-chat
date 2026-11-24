package com.streamchat.typing_processor.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class FixedWindowStrategy implements InactivityStrategy {

    private final Duration ventana;

    public FixedWindowStrategy(Duration ventana) {
        Objects.requireNonNull(ventana, "La ventana de inactividad es obligatoria");
        if (ventana.isZero() || ventana.isNegative()) {
            throw new IllegalArgumentException("La ventana de inactividad debe ser mayor que cero");
        }
        this.ventana = ventana;
    }

    public Duration ventana() {
        return ventana;
    }

    @Override
    public boolean estaInactivo(Instant ultimaActividad, Instant ahora) {
        return Duration.between(ultimaActividad, ahora).compareTo(ventana) >= 0;
    }
}

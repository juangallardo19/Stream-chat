package com.streamchat.typing_processor.domain;

import java.time.Duration;

public final class InactivityStrategyFactory {

    private InactivityStrategyFactory() {
    }

    public static InactivityStrategy crear(String nombre, Duration ventana) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}

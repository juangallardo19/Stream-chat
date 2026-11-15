package com.streamchat.typing_processor.domain;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class InactivityStrategyFactory {

    private static final Map<String, Function<Duration, InactivityStrategy>> ESTRATEGIAS =
            Map.of("fixed", FixedWindowStrategy::new);

    private InactivityStrategyFactory() {
    }

    public static InactivityStrategy crear(String nombre, Duration ventana) {
        Objects.requireNonNull(nombre, "El nombre de la estrategia es obligatorio");
        String clave = nombre.strip().toLowerCase(Locale.ROOT);
        return Optional.ofNullable(ESTRATEGIAS.get(clave))
                .map(fabrica -> fabrica.apply(ventana))
                .orElseThrow(() -> new IllegalArgumentException("Estrategia de inactividad desconocida: " + nombre));
    }
}

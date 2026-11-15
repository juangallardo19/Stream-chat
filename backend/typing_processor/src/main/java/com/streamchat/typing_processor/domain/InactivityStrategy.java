package com.streamchat.typing_processor.domain;

import java.time.Instant;

public interface InactivityStrategy {

    boolean estaInactivo(Instant ultimaActividad, Instant ahora);
}

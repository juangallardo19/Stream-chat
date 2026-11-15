package com.streamchat.typing_api.dto;

import java.time.Instant;
import java.util.Objects;

public record TypingEventPayload(String user, String group, Instant occurredAt) {

    public TypingEventPayload {
        exigirTexto(user, "El usuario es obligatorio");
        exigirTexto(group, "El grupo es obligatorio");
        Objects.requireNonNull(occurredAt, "La hora del evento es obligatoria");
    }

    private static void exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}

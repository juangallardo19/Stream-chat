package com.streamchat.typing_api.domain;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public record TypingEvent(UserId user, GroupId group, Instant occurredAt) {

    public TypingEvent {
        Objects.requireNonNull(user, "El usuario es obligatorio");
        Objects.requireNonNull(group, "El grupo es obligatorio");
        Objects.requireNonNull(occurredAt, "La hora del evento es obligatoria");
        occurredAt = occurredAt.truncatedTo(ChronoUnit.MILLIS);
    }

    public static TypingEvent registrarTecleo(UserId user, GroupId group, Clock clock) {
        Objects.requireNonNull(clock, "El reloj es obligatorio");
        return new TypingEvent(user, group, Instant.now(clock));
    }

    public String claveParticion() {
        return group.value();
    }
}

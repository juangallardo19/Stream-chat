package com.streamchat.typing_processor.domain;

import java.time.Instant;
import java.util.Objects;

public record TypingEvent(UserId user, GroupId group, Instant occurredAt) {

    public TypingEvent {
        Objects.requireNonNull(user, "El usuario es obligatorio");
        Objects.requireNonNull(group, "El grupo es obligatorio");
        Objects.requireNonNull(occurredAt, "La hora del evento es obligatoria");
    }
}

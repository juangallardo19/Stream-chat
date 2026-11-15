package com.streamchat.typing_api.domain;

import java.time.Clock;
import java.time.Instant;

public record TypingEvent(UserId user, GroupId group, Instant occurredAt) {

    public static TypingEvent registrarTecleo(UserId user, GroupId group, Clock clock) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public String claveParticion() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}

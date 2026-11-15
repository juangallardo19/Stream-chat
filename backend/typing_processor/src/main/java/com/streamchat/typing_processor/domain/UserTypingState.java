package com.streamchat.typing_processor.domain;

import java.time.Instant;
import java.util.Optional;

public final class UserTypingState {

    private UserTypingState() {
    }

    public static UserTypingState nuevo(UserId user, GroupId group) {
        return new UserTypingState();
    }

    public Optional<StatusChange> registrarPulsacion(Instant ahora) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public Optional<StatusChange> evaluarEstadoTyping(Instant ahora, InactivityStrategy estrategia) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public TypingStatus status() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}

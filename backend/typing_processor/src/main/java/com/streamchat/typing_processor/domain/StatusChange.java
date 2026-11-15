package com.streamchat.typing_processor.domain;

import java.util.Objects;

public record StatusChange(UserId user, GroupId group, TypingStatus status) {

    public StatusChange {
        Objects.requireNonNull(user, "El usuario es obligatorio");
        Objects.requireNonNull(group, "El grupo es obligatorio");
        Objects.requireNonNull(status, "El estado es obligatorio");
    }
}

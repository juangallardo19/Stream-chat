package com.streamchat.status_backend.domain;

import java.util.Objects;

public record StatusUpdate(UserId user, GroupId group, TypingStatus status) {

    public StatusUpdate {
        Objects.requireNonNull(user, "El usuario es obligatorio");
        Objects.requireNonNull(group, "El grupo es obligatorio");
        Objects.requireNonNull(status, "El estado es obligatorio");
    }
}

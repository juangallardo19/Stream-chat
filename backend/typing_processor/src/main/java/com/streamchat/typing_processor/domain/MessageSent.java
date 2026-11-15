package com.streamchat.typing_processor.domain;

import java.util.Objects;

public record MessageSent(UserId user, GroupId group) {

    public MessageSent {
        Objects.requireNonNull(user, "El usuario es obligatorio");
        Objects.requireNonNull(group, "El grupo es obligatorio");
    }
}

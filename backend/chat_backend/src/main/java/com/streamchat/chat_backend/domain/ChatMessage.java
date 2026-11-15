package com.streamchat.chat_backend.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ChatMessage(UUID messageId, UserId user, GroupId group, MessageText text, Instant sentAt) {

    public ChatMessage {
        Objects.requireNonNull(messageId, "El identificador del mensaje es obligatorio");
        Objects.requireNonNull(user, "El usuario es obligatorio");
        Objects.requireNonNull(group, "El grupo es obligatorio");
        Objects.requireNonNull(text, "El texto del mensaje es obligatorio");
        Objects.requireNonNull(sentAt, "La hora de envío es obligatoria");
    }
}

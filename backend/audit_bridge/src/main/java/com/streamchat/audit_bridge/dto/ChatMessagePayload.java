package com.streamchat.audit_bridge.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ChatMessagePayload(UUID messageId, String user, String group, String text, Instant sentAt) {

    public ChatMessagePayload {
        Objects.requireNonNull(messageId, "El identificador del mensaje es obligatorio");
        Objects.requireNonNull(user, "El usuario es obligatorio");
        Objects.requireNonNull(group, "El grupo es obligatorio");
        Objects.requireNonNull(text, "El texto del mensaje es obligatorio");
        Objects.requireNonNull(sentAt, "La hora de envío es obligatoria");
    }
}

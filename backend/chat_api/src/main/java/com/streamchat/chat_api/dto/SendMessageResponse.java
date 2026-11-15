package com.streamchat.chat_api.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record SendMessageResponse(UUID messageId, Instant sentAt) {

    public SendMessageResponse {
        Objects.requireNonNull(messageId, "El identificador del mensaje es obligatorio");
        Objects.requireNonNull(sentAt, "La hora de envío es obligatoria");
    }
}

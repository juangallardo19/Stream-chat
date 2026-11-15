package com.streamchat.typing_api.mapper;

import com.streamchat.typing_api.domain.TypingEvent;
import com.streamchat.typing_api.dto.TypingEventPayload;

import java.util.Objects;

public final class TypingEventMapper {

    private TypingEventMapper() {
    }

    public static TypingEventPayload aPayload(TypingEvent evento) {
        Objects.requireNonNull(evento, "El evento de tecleo es obligatorio");
        return new TypingEventPayload(evento.user().value(), evento.group().value(), evento.occurredAt());
    }
}

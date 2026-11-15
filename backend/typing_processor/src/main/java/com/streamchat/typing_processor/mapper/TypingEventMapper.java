package com.streamchat.typing_processor.mapper;

import com.streamchat.typing_processor.domain.GroupId;
import com.streamchat.typing_processor.domain.TypingEvent;
import com.streamchat.typing_processor.domain.UserId;
import com.streamchat.typing_processor.dto.TypingEventPayload;

import java.util.Objects;

public final class TypingEventMapper {

    private TypingEventMapper() {
    }

    public static TypingEvent aDominio(TypingEventPayload payload) {
        Objects.requireNonNull(payload, "El payload es obligatorio");
        return new TypingEvent(new UserId(payload.user()), new GroupId(payload.group()), payload.occurredAt());
    }
}

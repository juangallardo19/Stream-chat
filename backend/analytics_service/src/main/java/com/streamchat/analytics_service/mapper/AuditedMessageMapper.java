package com.streamchat.analytics_service.mapper;

import com.streamchat.analytics_service.domain.AuditedMessage;
import com.streamchat.analytics_service.dto.ChatMessagePayload;

import java.util.Objects;

public final class AuditedMessageMapper {

    private AuditedMessageMapper() {
    }

    public static AuditedMessage aDominio(ChatMessagePayload payload) {
        Objects.requireNonNull(payload, "El payload es obligatorio");
        return new AuditedMessage(payload.messageId(), payload.user(), payload.group(), payload.text(), payload.sentAt());
    }
}

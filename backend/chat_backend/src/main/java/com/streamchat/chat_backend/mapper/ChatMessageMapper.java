package com.streamchat.chat_backend.mapper;

import com.streamchat.chat_backend.domain.ChatMessage;
import com.streamchat.chat_backend.domain.GroupId;
import com.streamchat.chat_backend.domain.MessageText;
import com.streamchat.chat_backend.domain.UserId;
import com.streamchat.chat_backend.dto.ChatMessagePayload;

import java.util.Objects;

public final class ChatMessageMapper {

    private ChatMessageMapper() {
    }

    public static ChatMessage aDominio(ChatMessagePayload payload) {
        Objects.requireNonNull(payload, "El payload es obligatorio");
        return new ChatMessage(
                payload.messageId(),
                new UserId(payload.user()),
                new GroupId(payload.group()),
                new MessageText(payload.text()),
                payload.sentAt());
    }

    public static ChatMessagePayload aPayload(ChatMessage mensaje) {
        Objects.requireNonNull(mensaje, "El mensaje es obligatorio");
        return new ChatMessagePayload(
                mensaje.messageId(),
                mensaje.user().value(),
                mensaje.group().value(),
                mensaje.text().value(),
                mensaje.sentAt());
    }
}

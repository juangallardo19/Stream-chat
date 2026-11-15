package com.streamchat.chat_api.mapper;

import com.streamchat.chat_api.domain.ChatMessage;
import com.streamchat.chat_api.dto.ChatMessagePayload;

import java.util.Objects;

public final class ChatMessageMapper {

    private ChatMessageMapper() {
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

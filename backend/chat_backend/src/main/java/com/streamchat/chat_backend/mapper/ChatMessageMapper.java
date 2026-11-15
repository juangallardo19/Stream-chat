package com.streamchat.chat_backend.mapper;

import com.streamchat.chat_backend.domain.ChatMessage;
import com.streamchat.chat_backend.dto.ChatMessagePayload;

public final class ChatMessageMapper {

    private ChatMessageMapper() {
    }

    public static ChatMessage aDominio(ChatMessagePayload payload) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public static ChatMessagePayload aPayload(ChatMessage mensaje) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}

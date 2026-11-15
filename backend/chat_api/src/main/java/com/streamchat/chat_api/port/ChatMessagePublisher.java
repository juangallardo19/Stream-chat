package com.streamchat.chat_api.port;

import com.streamchat.chat_api.domain.ChatMessage;

public interface ChatMessagePublisher {

    void publicar(ChatMessage mensaje);
}

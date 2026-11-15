package com.streamchat.chat_backend.domain;

public interface ChatSubscriber {

    void recibir(ChatMessage mensaje);
}

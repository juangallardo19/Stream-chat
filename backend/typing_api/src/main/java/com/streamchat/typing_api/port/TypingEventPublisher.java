package com.streamchat.typing_api.port;

import com.streamchat.typing_api.domain.TypingEvent;

public interface TypingEventPublisher {

    void publicar(TypingEvent evento);
}

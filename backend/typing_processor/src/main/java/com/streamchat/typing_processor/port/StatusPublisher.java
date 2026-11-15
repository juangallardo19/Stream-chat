package com.streamchat.typing_processor.port;

import com.streamchat.typing_processor.domain.StatusChange;

public interface StatusPublisher {

    void publicar(StatusChange cambio);
}

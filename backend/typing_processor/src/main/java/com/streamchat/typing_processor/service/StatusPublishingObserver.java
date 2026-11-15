package com.streamchat.typing_processor.service;

import com.streamchat.typing_processor.domain.StatusChange;
import com.streamchat.typing_processor.domain.TypingStatusObserver;
import com.streamchat.typing_processor.port.StatusPublisher;
import org.springframework.stereotype.Component;

@Component
class StatusPublishingObserver implements TypingStatusObserver {

    private final StatusPublisher publisher;

    StatusPublishingObserver(StatusPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void alCambiarEstado(StatusChange cambio) {
    }
}

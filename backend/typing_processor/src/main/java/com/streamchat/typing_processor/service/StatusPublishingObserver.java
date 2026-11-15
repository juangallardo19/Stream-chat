package com.streamchat.typing_processor.service;

import com.streamchat.typing_processor.domain.StatusChange;
import com.streamchat.typing_processor.domain.TypingStatusObserver;
import com.streamchat.typing_processor.port.StatusPublisher;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
class StatusPublishingObserver implements TypingStatusObserver {

    private final StatusPublisher publisher;

    StatusPublishingObserver(StatusPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void alCambiarEstado(StatusChange cambio) {
        Objects.requireNonNull(cambio, "La transición de estado es obligatoria");
        publisher.publicar(cambio);
    }
}

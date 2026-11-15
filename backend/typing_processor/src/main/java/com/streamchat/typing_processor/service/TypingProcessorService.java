package com.streamchat.typing_processor.service;

import com.streamchat.typing_processor.domain.InactivityStrategy;
import com.streamchat.typing_processor.domain.TypingEvent;
import com.streamchat.typing_processor.domain.TypingStatusNotifier;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class TypingProcessorService {

    private final TypingStatusNotifier notifier;
    private final InactivityStrategy estrategia;
    private final Clock clock;

    public TypingProcessorService(TypingStatusNotifier notifier, InactivityStrategy estrategia, Clock clock) {
        this.notifier = notifier;
        this.estrategia = estrategia;
        this.clock = clock;
    }

    public void procesar(TypingEvent evento) {
    }

    public void detectarInactividad() {
    }
}

package com.streamchat.typing_processor.listener;

import com.streamchat.typing_processor.service.TypingProcessorService;
import org.springframework.stereotype.Component;

@Component
class InactivitySweeper {

    private final TypingProcessorService servicio;

    InactivitySweeper(TypingProcessorService servicio) {
        this.servicio = servicio;
    }

    void barrer() {
    }
}

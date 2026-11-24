package com.streamchat.typing_processor.listener;

import com.streamchat.typing_processor.service.TypingProcessorService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class InactivitySweeper {

    private final TypingProcessorService servicio;

    InactivitySweeper(TypingProcessorService servicio) {
        this.servicio = servicio;
    }

    @Scheduled(fixedRateString = "${app.typing.sweep-ms:1000}")
    void barrer() {
        servicio.detectarInactividad();
    }
}

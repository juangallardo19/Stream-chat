package com.streamchat.typing_processor.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.typing_processor.adapter.DebounceMetricsObserver;
import com.streamchat.typing_processor.service.TypingProcessorService;
import org.springframework.stereotype.Component;

@Component
class TypingEventListener {

    private final TypingProcessorService servicio;
    private final ObjectMapper objectMapper;
    private final DebounceMetricsObserver metricas;

    TypingEventListener(TypingProcessorService servicio, ObjectMapper objectMapper, DebounceMetricsObserver metricas) {
        this.servicio = servicio;
        this.objectMapper = objectMapper;
        this.metricas = metricas;
    }

    void recibir(String json) {
    }
}

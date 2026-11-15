package com.streamchat.typing_processor.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.typing_processor.service.TypingProcessorService;
import org.springframework.stereotype.Component;

@Component
class MessageSentListener {

    private final TypingProcessorService servicio;
    private final ObjectMapper objectMapper;

    MessageSentListener(TypingProcessorService servicio, ObjectMapper objectMapper) {
        this.servicio = servicio;
        this.objectMapper = objectMapper;
    }

    void recibir(String json) {
    }
}

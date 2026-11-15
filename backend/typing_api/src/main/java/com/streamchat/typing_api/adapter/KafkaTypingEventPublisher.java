package com.streamchat.typing_api.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.typing_api.domain.TypingEvent;
import com.streamchat.typing_api.port.TypingEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class KafkaTypingEventPublisher implements TypingEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    KafkaTypingEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publicar(TypingEvent evento) {
    }
}

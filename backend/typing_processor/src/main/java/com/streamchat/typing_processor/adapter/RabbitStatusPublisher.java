package com.streamchat.typing_processor.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.typing_processor.domain.StatusChange;
import com.streamchat.typing_processor.port.StatusPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
class RabbitStatusPublisher implements StatusPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    RabbitStatusPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publicar(StatusChange cambio) {
    }
}

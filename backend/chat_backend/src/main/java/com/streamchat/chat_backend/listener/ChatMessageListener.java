package com.streamchat.chat_backend.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.chat_backend.service.DeliverMessageService;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

@Component
class ChatMessageListener {

    private final DeliverMessageService servicio;
    private final ObjectMapper objectMapper;

    ChatMessageListener(DeliverMessageService servicio, ObjectMapper objectMapper) {
        this.servicio = servicio;
        this.objectMapper = objectMapper;
    }

    void recibir(Message mensajeAmqp) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}

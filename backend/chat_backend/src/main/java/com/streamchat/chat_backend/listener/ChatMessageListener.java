package com.streamchat.chat_backend.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.chat_backend.domain.ChatMessage;
import com.streamchat.chat_backend.dto.ChatMessagePayload;
import com.streamchat.chat_backend.mapper.ChatMessageMapper;
import com.streamchat.chat_backend.service.DeliverMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
class ChatMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(ChatMessageListener.class);

    private final DeliverMessageService servicio;
    private final ObjectMapper objectMapper;

    ChatMessageListener(DeliverMessageService servicio, ObjectMapper objectMapper) {
        this.servicio = servicio;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "#{chatQueueNames}", concurrency = "1")
    void recibir(Message mensajeAmqp) {
        servicio.entregar(convertir(mensajeAmqp));
    }

    private ChatMessage convertir(Message mensajeAmqp) {
        try {
            ChatMessagePayload payload = objectMapper.readValue(mensajeAmqp.getBody(), ChatMessagePayload.class);
            return ChatMessageMapper.aDominio(payload);
        } catch (IOException | RuntimeException error) {
            LOG.warn("Mensaje de chat descartado por estar mal formado: {}", error.getMessage());
            throw new AmqpRejectAndDontRequeueException("Mensaje de chat mal formado", error);
        }
    }
}

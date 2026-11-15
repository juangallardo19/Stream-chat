package com.streamchat.chat_api.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.chat_api.config.RabbitConfig;
import com.streamchat.chat_api.domain.ChatMessage;
import com.streamchat.chat_api.mapper.ChatMessageMapper;
import com.streamchat.chat_api.port.ChatMessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
class RabbitChatMessagePublisher implements ChatMessagePublisher {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitChatMessagePublisher.class);
    private static final long ESPERA_CONFIRMACION_MS = 5_000L;

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    RabbitChatMessagePublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publicar(ChatMessage mensaje) {
        Message mensajeAmqp = construirMensaje(mensaje);
        rabbitTemplate.invoke(operaciones -> {
            operaciones.send(RabbitConfig.CHAT_EXCHANGE, mensaje.routingKey(), mensajeAmqp);
            operaciones.waitForConfirmsOrDie(ESPERA_CONFIRMACION_MS);
            return Boolean.TRUE;
        });
        LOG.info("Mensaje {} confirmado por RabbitMQ con routing key {}", mensaje.messageId(), mensaje.routingKey());
    }

    private Message construirMensaje(ChatMessage mensaje) {
        return MessageBuilder.withBody(aJson(mensaje))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding(StandardCharsets.UTF_8.name())
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setMessageId(mensaje.messageId().toString())
                .build();
    }

    private byte[] aJson(ChatMessage mensaje) {
        try {
            return objectMapper.writeValueAsBytes(ChatMessageMapper.aPayload(mensaje));
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("No se pudo convertir a JSON el mensaje " + mensaje.messageId(), error);
        }
    }
}

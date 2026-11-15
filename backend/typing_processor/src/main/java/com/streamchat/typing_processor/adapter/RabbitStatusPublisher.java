package com.streamchat.typing_processor.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.typing_processor.domain.StatusChange;
import com.streamchat.typing_processor.mapper.StatusChangeMapper;
import com.streamchat.typing_processor.port.StatusPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
class RabbitStatusPublisher implements StatusPublisher {

    static final String EXCHANGE = "status_updates";
    private static final String SIN_ROUTING_KEY = "";
    private static final Logger LOG = LoggerFactory.getLogger(RabbitStatusPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    RabbitStatusPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publicar(StatusChange cambio) {
        Objects.requireNonNull(cambio, "La transición de estado es obligatoria");
        try {
            rabbitTemplate.send(EXCHANGE, SIN_ROUTING_KEY, aMensaje(cambio));
        } catch (AmqpException error) {
            LOG.warn("Transición {} de {} en {} descartada: {}",
                    cambio.status(), cambio.user().value(), cambio.group().value(), error.getMessage());
        }
    }

    private Message aMensaje(StatusChange cambio) {
        return MessageBuilder.withBody(aJson(cambio))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding("UTF-8")
                .setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT)
                .build();
    }

    private byte[] aJson(StatusChange cambio) {
        try {
            return objectMapper.writeValueAsBytes(StatusChangeMapper.aPayload(cambio));
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("No se pudo convertir a JSON la transición de " + cambio.user().value(), error);
        }
    }
}

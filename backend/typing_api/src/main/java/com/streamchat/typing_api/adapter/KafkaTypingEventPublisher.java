package com.streamchat.typing_api.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.typing_api.domain.TypingEvent;
import com.streamchat.typing_api.mapper.TypingEventMapper;
import com.streamchat.typing_api.port.TypingEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
class KafkaTypingEventPublisher implements TypingEventPublisher {

    static final String TOPIC = "typing_events";
    private static final Logger LOG = LoggerFactory.getLogger(KafkaTypingEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    KafkaTypingEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publicar(TypingEvent evento) {
        Objects.requireNonNull(evento, "El evento de tecleo es obligatorio");
        kafkaTemplate.send(TOPIC, evento.claveParticion(), aJson(evento))
                .whenComplete((resultado, error) -> registrarFallo(evento, error));
    }

    private String aJson(TypingEvent evento) {
        try {
            return objectMapper.writeValueAsString(TypingEventMapper.aPayload(evento));
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("No se pudo convertir a JSON el evento de " + evento.user().value(), error);
        }
    }

    private static void registrarFallo(TypingEvent evento, Throwable error) {
        if (error == null) {
            return;
        }
        LOG.warn("Evento de tecleo de {} en {} descartado: {}",
                evento.user().value(), evento.group().value(), error.getMessage());
    }
}

package com.streamchat.typing_processor.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.typing_processor.adapter.DebounceMetricsObserver;
import com.streamchat.typing_processor.domain.TypingEvent;
import com.streamchat.typing_processor.dto.TypingEventPayload;
import com.streamchat.typing_processor.mapper.TypingEventMapper;
import com.streamchat.typing_processor.service.TypingProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class TypingEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(TypingEventListener.class);

    private final TypingProcessorService servicio;
    private final ObjectMapper objectMapper;
    private final DebounceMetricsObserver metricas;

    TypingEventListener(TypingProcessorService servicio, ObjectMapper objectMapper, DebounceMetricsObserver metricas) {
        this.servicio = servicio;
        this.objectMapper = objectMapper;
        this.metricas = metricas;
    }

    @KafkaListener(topics = "typing_events", groupId = "typing-processor-group")
    void recibir(String json) {
        convertir(json).ifPresent(this::procesar);
    }

    private void procesar(TypingEvent evento) {
        metricas.registrarEvento();
        servicio.procesar(evento);
    }

    private Optional<TypingEvent> convertir(String json) {
        try {
            TypingEventPayload payload = objectMapper.readValue(json, TypingEventPayload.class);
            return Optional.of(TypingEventMapper.aDominio(payload));
        } catch (JsonProcessingException | RuntimeException error) {
            LOG.warn("Evento de tecleo saltado por estar mal formado: {}", error.getMessage());
            return Optional.empty();
        }
    }
}

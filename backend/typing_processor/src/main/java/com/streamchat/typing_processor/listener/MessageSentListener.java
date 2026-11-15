package com.streamchat.typing_processor.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.typing_processor.domain.GroupId;
import com.streamchat.typing_processor.domain.MessageSent;
import com.streamchat.typing_processor.domain.UserId;
import com.streamchat.typing_processor.dto.MessageSentPayload;
import com.streamchat.typing_processor.service.TypingProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class MessageSentListener {

    private static final Logger LOG = LoggerFactory.getLogger(MessageSentListener.class);

    private final TypingProcessorService servicio;
    private final ObjectMapper objectMapper;

    MessageSentListener(TypingProcessorService servicio, ObjectMapper objectMapper) {
        this.servicio = servicio;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "message_audit_log", groupId = "typing-processor-group")
    void recibir(String json) {
        convertir(json).ifPresent(servicio::registrarEnvio);
    }

    private Optional<MessageSent> convertir(String json) {
        try {
            MessageSentPayload payload = objectMapper.readValue(json, MessageSentPayload.class);
            return Optional.of(new MessageSent(new UserId(payload.user()), new GroupId(payload.group())));
        } catch (JsonProcessingException | RuntimeException error) {
            LOG.warn("Registro de envío saltado por estar mal formado: {}", error.getMessage());
            return Optional.empty();
        }
    }
}

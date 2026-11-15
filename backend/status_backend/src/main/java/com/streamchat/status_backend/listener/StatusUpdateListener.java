package com.streamchat.status_backend.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.status_backend.domain.StatusUpdate;
import com.streamchat.status_backend.dto.StatusUpdatePayload;
import com.streamchat.status_backend.mapper.StatusUpdateMapper;
import com.streamchat.status_backend.service.BroadcastStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
class StatusUpdateListener {

    private static final Logger LOG = LoggerFactory.getLogger(StatusUpdateListener.class);

    private final BroadcastStatusService servicio;
    private final ObjectMapper objectMapper;

    StatusUpdateListener(BroadcastStatusService servicio, ObjectMapper objectMapper) {
        this.servicio = servicio;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "#{statusQueue.name}", concurrency = "1")
    void recibir(Message mensajeAmqp) {
        servicio.difundir(convertir(mensajeAmqp));
    }

    private StatusUpdate convertir(Message mensajeAmqp) {
        try {
            StatusUpdatePayload payload = objectMapper.readValue(mensajeAmqp.getBody(), StatusUpdatePayload.class);
            return StatusUpdateMapper.aDominio(payload);
        } catch (IOException | RuntimeException error) {
            LOG.warn("Actualización de estado descartada por estar mal formada: {}", error.getMessage());
            throw new AmqpRejectAndDontRequeueException("Actualización de estado mal formada", error);
        }
    }
}

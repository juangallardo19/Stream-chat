package com.streamchat.chat_backend.service;

import com.streamchat.chat_backend.domain.ChatMessage;
import com.streamchat.chat_backend.port.GroupBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DeliverMessageService {

    private static final Logger LOG = LoggerFactory.getLogger(DeliverMessageService.class);

    private final GroupBroadcaster broadcaster;

    public DeliverMessageService(GroupBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    public void entregar(ChatMessage mensaje) {
        Objects.requireNonNull(mensaje, "El mensaje es obligatorio");
        int entregas = broadcaster.difundir(mensaje);
        LOG.debug("Mensaje {} entregado a {} sesiones del grupo {}", mensaje.messageId(), entregas, mensaje.group().value());
    }
}

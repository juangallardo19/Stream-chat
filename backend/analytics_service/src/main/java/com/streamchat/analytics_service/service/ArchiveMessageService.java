package com.streamchat.analytics_service.service;

import com.streamchat.analytics_service.domain.ArchivedMessage;
import com.streamchat.analytics_service.domain.AuditedMessage;
import com.streamchat.analytics_service.domain.ChatGroup;
import com.streamchat.analytics_service.port.ChatGroupRepository;
import com.streamchat.analytics_service.port.ChatMessageRepository;
import com.streamchat.analytics_service.port.MetricsReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Objects;

@Service
public class ArchiveMessageService {

    private static final Logger LOG = LoggerFactory.getLogger(ArchiveMessageService.class);

    private final ChatMessageRepository mensajes;
    private final ChatGroupRepository grupos;
    private final MetricsReporter metricas;
    private final Clock reloj;

    public ArchiveMessageService(ChatMessageRepository mensajes, ChatGroupRepository grupos, MetricsReporter metricas, Clock reloj) {
        this.mensajes = mensajes;
        this.grupos = grupos;
        this.metricas = metricas;
        this.reloj = reloj;
    }

    @Transactional
    public void archivar(AuditedMessage mensaje) {
        Objects.requireNonNull(mensaje, "El mensaje auditado es obligatorio");
        if (mensajes.existsById(mensaje.messageId())) {
            LOG.info("Mensaje {} duplicado: ya estaba archivado, se ignora", mensaje.messageId());
            return;
        }
        ChatGroup grupo = grupos.findById(mensaje.group()).orElseGet(() -> ChatGroup.nuevo(mensaje.group()));
        ArchivedMessage archivado = ArchivedMessage.archivar(mensaje, reloj.instant());
        grupo.registrarMensaje(archivado);
        grupos.save(grupo);
        mensajes.save(archivado);
        metricas.reportar(grupo);
    }
}

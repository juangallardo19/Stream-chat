package com.streamchat.audit_bridge.service;

import com.streamchat.audit_bridge.domain.AuditRecord;
import com.streamchat.audit_bridge.port.AuditLogPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuditBridgeService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditBridgeService.class);

    private final AuditLogPublisher publicador;

    public AuditBridgeService(AuditLogPublisher publicador) {
        this.publicador = publicador;
    }

    public void transferir(AuditRecord registro) {
        Objects.requireNonNull(registro, "El registro de auditoría es obligatorio");
        publicador.publicar(registro);
        LOG.debug("Mensaje {} del grupo {} copiado al log de auditoría", registro.messageId(), registro.group());
    }
}

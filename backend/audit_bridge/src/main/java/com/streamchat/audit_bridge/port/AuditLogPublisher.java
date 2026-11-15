package com.streamchat.audit_bridge.port;

import com.streamchat.audit_bridge.domain.AuditRecord;

public interface AuditLogPublisher {

    void publicar(AuditRecord registro);
}

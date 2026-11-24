package com.streamchat.audit_bridge.mapper;

import com.streamchat.audit_bridge.domain.AuditRecord;
import com.streamchat.audit_bridge.dto.ChatMessagePayload;

import java.util.Objects;

public final class AuditRecordMapper {

    private AuditRecordMapper() {
    }

    public static AuditRecord aDominio(ChatMessagePayload payload) {
        Objects.requireNonNull(payload, "El payload es obligatorio");
        return new AuditRecord(payload.messageId(), payload.user(), payload.group(), payload.text(), payload.sentAt());
    }

    public static ChatMessagePayload aPayload(AuditRecord registro) {
        Objects.requireNonNull(registro, "El registro de auditoría es obligatorio");
        return new ChatMessagePayload(registro.messageId(), registro.user(), registro.group(), registro.text(), registro.sentAt());
    }
}

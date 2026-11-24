package com.streamchat.analytics_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
public class ArchivedMessage {

    @Id
    @Column(name = "message_id", updatable = false, nullable = false)
    private UUID messageId;

    @Column(name = "group_id", updatable = false, nullable = false, length = 40)
    private String groupId;

    @Column(name = "user_id", updatable = false, nullable = false, length = 40)
    private String userId;

    @Column(name = "text", updatable = false, nullable = false, length = 1000)
    private String text;

    @Column(name = "sent_at", updatable = false, nullable = false)
    private Instant sentAt;

    @Column(name = "archived_at", updatable = false, nullable = false)
    private Instant archivedAt;

    protected ArchivedMessage() {
    }

    private ArchivedMessage(AuditedMessage mensaje, Instant archivadoEn) {
        this.messageId = mensaje.messageId();
        this.groupId = mensaje.group();
        this.userId = mensaje.user();
        this.text = mensaje.text();
        this.sentAt = mensaje.sentAt();
        this.archivedAt = archivadoEn.truncatedTo(ChronoUnit.MILLIS);
    }

    public static ArchivedMessage archivar(AuditedMessage mensaje, Instant archivadoEn) {
        Objects.requireNonNull(mensaje, "El mensaje auditado es obligatorio");
        Objects.requireNonNull(archivadoEn, "La hora de archivo es obligatoria");
        return new ArchivedMessage(mensaje, archivadoEn);
    }

    public boolean perteneceA(String grupo) {
        return groupId.equals(grupo);
    }

    public UUID getMessageId() {
        return messageId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getUserId() {
        return userId;
    }

    public String getText() {
        return text;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }
}

package com.streamchat.analytics_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "chat_groups")
public class ChatGroup {

    @Id
    @Column(name = "group_id", updatable = false, nullable = false, length = 40)
    private String groupId;

    @Column(name = "total_messages", nullable = false)
    private long totalMessages;

    @Column(name = "first_message_at")
    private Instant firstMessageAt;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    protected ChatGroup() {
    }

    private ChatGroup(String groupId) {
        this.groupId = groupId;
        this.totalMessages = 0;
    }

    public static ChatGroup nuevo(String groupId) {
        if (groupId == null || groupId.isBlank()) {
            throw new IllegalArgumentException("El grupo es obligatorio");
        }
        return new ChatGroup(groupId);
    }

    public void registrarMensaje(ArchivedMessage mensaje) {
        Objects.requireNonNull(mensaje, "El mensaje es obligatorio");
        if (!mensaje.perteneceA(groupId)) {
            throw new IllegalArgumentException("El mensaje pertenece al grupo " + mensaje.getGroupId() + " y no a " + groupId);
        }
        totalMessages++;
        actualizarFechas(mensaje.getSentAt());
    }

    private void actualizarFechas(Instant enviado) {
        if (firstMessageAt == null || enviado.isBefore(firstMessageAt)) {
            firstMessageAt = enviado;
        }
        if (lastMessageAt == null || enviado.isAfter(lastMessageAt)) {
            lastMessageAt = enviado;
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public Optional<Instant> getFirstMessageAt() {
        return Optional.ofNullable(firstMessageAt);
    }

    public Optional<Instant> getLastMessageAt() {
        return Optional.ofNullable(lastMessageAt);
    }
}

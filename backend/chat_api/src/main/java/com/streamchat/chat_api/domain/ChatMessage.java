package com.streamchat.chat_api.domain;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

public final class ChatMessage {

    private final UUID messageId;
    private final UserId user;
    private final GroupId group;
    private final MessageText text;
    private final Instant sentAt;

    private ChatMessage(UUID messageId, UserId user, GroupId group, MessageText text, Instant sentAt) {
        this.messageId = messageId;
        this.user = user;
        this.group = group;
        this.text = text;
        this.sentAt = sentAt;
    }

    public static ChatMessage registrarMensaje(UserId user, GroupId group, MessageText text, Clock clock) {
        Objects.requireNonNull(user, "El usuario es obligatorio");
        Objects.requireNonNull(group, "El grupo es obligatorio");
        Objects.requireNonNull(text, "El texto del mensaje es obligatorio");
        Objects.requireNonNull(clock, "El reloj es obligatorio");
        return new ChatMessage(UUID.randomUUID(), user, group, text, Instant.now(clock).truncatedTo(ChronoUnit.MILLIS));
    }

    public String routingKey() {
        return group.routingKey();
    }

    public UUID messageId() {
        return messageId;
    }

    public UserId user() {
        return user;
    }

    public GroupId group() {
        return group;
    }

    public MessageText text() {
        return text;
    }

    public Instant sentAt() {
        return sentAt;
    }
}

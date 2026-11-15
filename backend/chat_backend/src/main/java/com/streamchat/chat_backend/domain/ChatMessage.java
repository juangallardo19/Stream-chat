package com.streamchat.chat_backend.domain;

import java.time.Instant;
import java.util.UUID;

public record ChatMessage(UUID messageId, UserId user, GroupId group, MessageText text, Instant sentAt) {
}

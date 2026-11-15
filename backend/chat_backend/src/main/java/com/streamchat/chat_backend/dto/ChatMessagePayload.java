package com.streamchat.chat_backend.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatMessagePayload(UUID messageId, String user, String group, String text, Instant sentAt) {
}

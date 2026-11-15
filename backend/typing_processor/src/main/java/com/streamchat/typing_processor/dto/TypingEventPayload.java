package com.streamchat.typing_processor.dto;

import java.time.Instant;

public record TypingEventPayload(String user, String group, Instant occurredAt) {
}

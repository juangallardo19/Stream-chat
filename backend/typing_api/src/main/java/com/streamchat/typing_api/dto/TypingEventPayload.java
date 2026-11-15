package com.streamchat.typing_api.dto;

import java.time.Instant;

public record TypingEventPayload(String user, String group, Instant occurredAt) {
}

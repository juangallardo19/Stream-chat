package com.streamchat.typing_processor.domain;

import java.time.Instant;

public record TypingEvent(UserId user, GroupId group, Instant occurredAt) {
}

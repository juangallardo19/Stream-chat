package com.streamchat.typing_processor.domain;

public record StatusChange(UserId user, GroupId group, TypingStatus status) {
}

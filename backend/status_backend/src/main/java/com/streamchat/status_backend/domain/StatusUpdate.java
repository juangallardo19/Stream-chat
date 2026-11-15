package com.streamchat.status_backend.domain;

public record StatusUpdate(UserId user, GroupId group, TypingStatus status) {
}

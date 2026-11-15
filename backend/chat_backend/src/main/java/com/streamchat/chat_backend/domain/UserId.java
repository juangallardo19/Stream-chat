package com.streamchat.chat_backend.domain;

public record UserId(String value) {

    public UserId {
        IdentifierRules.validar(value, "usuario");
    }
}

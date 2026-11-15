package com.streamchat.chat_api.domain;

public record UserId(String value) {

    public UserId {
        IdentifierRules.validar(value, "usuario");
    }
}

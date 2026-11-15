package com.streamchat.typing_processor.domain;

public record UserId(String value) {

    public UserId {
        IdentifierRules.validar(value, "usuario");
    }
}

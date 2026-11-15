package com.streamchat.typing_api.domain;

public record UserId(String value) {

    public UserId {
        IdentifierRules.validar(value, "usuario");
    }
}

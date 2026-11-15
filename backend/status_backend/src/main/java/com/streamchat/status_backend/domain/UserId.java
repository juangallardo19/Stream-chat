package com.streamchat.status_backend.domain;

public record UserId(String value) {

    public UserId {
        IdentifierRules.validar(value, "usuario");
    }
}

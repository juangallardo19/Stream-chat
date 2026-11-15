package com.streamchat.status_backend.domain;

public record GroupId(String value) {

    public GroupId {
        IdentifierRules.validar(value, "grupo");
    }
}

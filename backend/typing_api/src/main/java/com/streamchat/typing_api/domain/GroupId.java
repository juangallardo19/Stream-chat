package com.streamchat.typing_api.domain;

public record GroupId(String value) {

    public GroupId {
        IdentifierRules.validar(value, "grupo");
    }
}

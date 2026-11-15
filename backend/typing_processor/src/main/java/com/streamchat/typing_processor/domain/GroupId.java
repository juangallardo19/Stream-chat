package com.streamchat.typing_processor.domain;

public record GroupId(String value) {

    public GroupId {
        IdentifierRules.validar(value, "grupo");
    }
}

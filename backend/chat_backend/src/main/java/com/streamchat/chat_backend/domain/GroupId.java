package com.streamchat.chat_backend.domain;

import java.util.Locale;

public record GroupId(String value) {

    public GroupId {
        IdentifierRules.validar(value, "grupo");
    }

    public String routingKey() {
        return "chat." + value.toLowerCase(Locale.ROOT);
    }
}

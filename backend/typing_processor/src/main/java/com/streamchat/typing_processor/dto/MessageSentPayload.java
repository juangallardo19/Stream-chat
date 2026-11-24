package com.streamchat.typing_processor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MessageSentPayload(String user, String group) {

    public MessageSentPayload {
        exigirTexto(user, "El usuario es obligatorio");
        exigirTexto(group, "El grupo es obligatorio");
    }

    private static void exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}

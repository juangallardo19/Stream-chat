package com.streamchat.chat_api.domain;

public record MessageText(String value) {

    private static final int LONGITUD_MAXIMA = 1000;

    public MessageText {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El texto del mensaje es obligatorio");
        }
        if (value.length() > LONGITUD_MAXIMA) {
            throw new IllegalArgumentException("El texto del mensaje admite como máximo 1000 caracteres");
        }
    }
}

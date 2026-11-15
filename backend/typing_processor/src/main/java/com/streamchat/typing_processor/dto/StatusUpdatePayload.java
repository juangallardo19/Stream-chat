package com.streamchat.typing_processor.dto;

import com.streamchat.typing_processor.domain.TypingStatus;

import java.util.Arrays;

public record StatusUpdatePayload(String user, String group, String status) {

    public StatusUpdatePayload {
        exigirTexto(user, "El usuario es obligatorio");
        exigirTexto(group, "El grupo es obligatorio");
        if (!esEstadoValido(status)) {
            throw new IllegalArgumentException("El estado solo puede ser TYPING o STOPPED");
        }
    }

    private static boolean esEstadoValido(String status) {
        return Arrays.stream(TypingStatus.values()).anyMatch(valido -> valido.name().equals(status));
    }

    private static void exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}

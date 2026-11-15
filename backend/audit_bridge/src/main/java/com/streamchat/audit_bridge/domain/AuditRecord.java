package com.streamchat.audit_bridge.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public record AuditRecord(UUID messageId, String user, String group, String text, Instant sentAt) {

    private static final int LONGITUD_MAXIMA_IDENTIFICADOR = 40;
    private static final int LONGITUD_MAXIMA_TEXTO = 1000;
    private static final Pattern FORMATO_IDENTIFICADOR = Pattern.compile("^[A-Za-z0-9_-]+$");

    public AuditRecord {
        Objects.requireNonNull(messageId, "El identificador del mensaje es obligatorio");
        validarIdentificador(user, "usuario");
        validarIdentificador(group, "grupo");
        validarTexto(text);
        Objects.requireNonNull(sentAt, "La hora de envío es obligatoria");
        sentAt = sentAt.truncatedTo(ChronoUnit.MILLIS);
    }

    public String claveParticion() {
        return group;
    }

    private static void validarIdentificador(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El " + campo + " es obligatorio");
        }
        if (valor.length() > LONGITUD_MAXIMA_IDENTIFICADOR) {
            throw new IllegalArgumentException("El " + campo + " admite como máximo " + LONGITUD_MAXIMA_IDENTIFICADOR + " caracteres");
        }
        if (!FORMATO_IDENTIFICADOR.matcher(valor).matches()) {
            throw new IllegalArgumentException("El " + campo + " solo admite letras, números, guion y guion bajo");
        }
    }

    private static void validarTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El texto del mensaje es obligatorio");
        }
        if (valor.length() > LONGITUD_MAXIMA_TEXTO) {
            throw new IllegalArgumentException("El texto del mensaje admite como máximo " + LONGITUD_MAXIMA_TEXTO + " caracteres");
        }
    }
}

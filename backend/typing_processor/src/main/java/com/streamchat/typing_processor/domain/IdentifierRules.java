package com.streamchat.typing_processor.domain;

import java.util.regex.Pattern;

final class IdentifierRules {

    private static final int LONGITUD_MAXIMA = 40;
    private static final Pattern FORMATO = Pattern.compile("^[A-Za-z0-9_-]+$");

    private IdentifierRules() {
    }

    static void validar(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El " + campo + " es obligatorio");
        }
        if (valor.length() > LONGITUD_MAXIMA) {
            throw new IllegalArgumentException("El " + campo + " admite como máximo " + LONGITUD_MAXIMA + " caracteres");
        }
        if (!FORMATO.matcher(valor).matches()) {
            throw new IllegalArgumentException("El " + campo + " solo admite letras, números, guion y guion bajo");
        }
    }
}

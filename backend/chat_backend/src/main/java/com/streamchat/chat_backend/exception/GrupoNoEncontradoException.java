package com.streamchat.chat_backend.exception;

public class GrupoNoEncontradoException extends RuntimeException {

    public GrupoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

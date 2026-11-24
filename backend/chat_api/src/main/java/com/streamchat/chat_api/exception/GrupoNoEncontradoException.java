package com.streamchat.chat_api.exception;

public class GrupoNoEncontradoException extends RuntimeException {

    public GrupoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

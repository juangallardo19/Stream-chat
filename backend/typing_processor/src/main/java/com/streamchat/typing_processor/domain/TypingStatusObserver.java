package com.streamchat.typing_processor.domain;

public interface TypingStatusObserver {

    void alCambiarEstado(StatusChange cambio);
}

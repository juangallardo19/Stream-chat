package com.streamchat.status_backend.domain;

public interface StatusSubscriber {

    void recibir(StatusUpdate actualizacion);
}

package com.streamchat.typing_processor.domain;

public final class StoppedState implements ActivityState {

    @Override
    public ActivityState alTeclear() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public ActivityState alExpirar() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public TypingStatus status() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}

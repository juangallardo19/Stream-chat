package com.streamchat.typing_processor.domain;

public final class TypingState implements ActivityState {

    @Override
    public ActivityState alTeclear() {
        return this;
    }

    @Override
    public ActivityState alExpirar() {
        return new StoppedState();
    }

    @Override
    public ActivityState alEnviarMensaje() {
        return new StoppedState();
    }

    @Override
    public TypingStatus status() {
        return TypingStatus.TYPING;
    }
}

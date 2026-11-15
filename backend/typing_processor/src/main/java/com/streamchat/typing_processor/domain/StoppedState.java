package com.streamchat.typing_processor.domain;

public final class StoppedState implements ActivityState {

    @Override
    public ActivityState alTeclear() {
        return new TypingState();
    }

    @Override
    public ActivityState alExpirar() {
        return this;
    }

    @Override
    public TypingStatus status() {
        return TypingStatus.STOPPED;
    }
}

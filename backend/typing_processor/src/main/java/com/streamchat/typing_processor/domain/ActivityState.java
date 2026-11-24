package com.streamchat.typing_processor.domain;

public sealed interface ActivityState permits TypingState, StoppedState {

    ActivityState alTeclear();

    ActivityState alExpirar();

    ActivityState alEnviarMensaje();

    TypingStatus status();
}

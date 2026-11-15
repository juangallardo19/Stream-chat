package com.streamchat.typing_processor.adapter;

import com.streamchat.typing_processor.domain.StatusChange;
import com.streamchat.typing_processor.domain.TypingStatusObserver;
import org.springframework.stereotype.Component;

@Component
public class DebounceMetricsObserver implements TypingStatusObserver {

    public void registrarEvento() {
    }

    @Override
    public void alCambiarEstado(StatusChange cambio) {
    }

    public long eventosRecibidos() {
        return 0;
    }

    public long transicionesPublicadas() {
        return 0;
    }
}

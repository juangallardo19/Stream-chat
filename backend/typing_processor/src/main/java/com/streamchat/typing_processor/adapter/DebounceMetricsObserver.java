package com.streamchat.typing_processor.adapter;

import com.streamchat.typing_processor.domain.StatusChange;
import com.streamchat.typing_processor.domain.TypingStatusObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class DebounceMetricsObserver implements TypingStatusObserver {

    private static final Logger LOG = LoggerFactory.getLogger(DebounceMetricsObserver.class);

    private final AtomicLong eventos = new AtomicLong();
    private final AtomicLong transiciones = new AtomicLong();

    public void registrarEvento() {
        eventos.incrementAndGet();
    }

    @Override
    public void alCambiarEstado(StatusChange cambio) {
        long total = transiciones.incrementAndGet();
        LOG.info("Debounce: {} eventos -> {} transiciones ({} {} en {})",
                eventos.get(), total, cambio.user().value(), cambio.status(), cambio.group().value());
    }

    public long eventosRecibidos() {
        return eventos.get();
    }

    public long transicionesPublicadas() {
        return transiciones.get();
    }
}

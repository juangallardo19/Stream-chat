package com.streamchat.typing_processor.adapter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.streamchat.typing_processor.domain.GroupId;
import com.streamchat.typing_processor.domain.StatusChange;
import com.streamchat.typing_processor.domain.TypingStatus;
import com.streamchat.typing_processor.domain.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DebounceMetricsObserver")
class DebounceMetricsObserverTest {

    private final DebounceMetricsObserver metricas = new DebounceMetricsObserver();
    private final Logger logger = (Logger) LoggerFactory.getLogger(DebounceMetricsObserver.class);
    private final ListAppender<ILoggingEvent> registros = new ListAppender<>();
    private final StatusChange typing = new StatusChange(new UserId("Ana"), new GroupId("GrupoA"), TypingStatus.TYPING);

    @BeforeEach
    void setUp() {
        registros.start();
        logger.addAppender(registros);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(registros);
    }

    @Test
    @DisplayName("cuenta los eventos recibidos y las transiciones publicadas")
    void cuentaEventosYTransiciones() {
        for (int i = 0; i < 10; i++) {
            metricas.registrarEvento();
        }

        metricas.alCambiarEstado(typing);

        assertEquals(10, metricas.eventosRecibidos());
        assertEquals(1, metricas.transicionesPublicadas());
    }

    @Test
    @DisplayName("deja en el log cuántos eventos se convirtieron en transiciones")
    void registraEnElLog() {
        for (int i = 0; i < 10; i++) {
            metricas.registrarEvento();
        }

        metricas.alCambiarEstado(typing);

        List<String> mensajes = registros.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
        assertTrue(mensajes.contains("Debounce: 10 eventos -> 1 transiciones (Ana TYPING en GrupoA)"));
    }
}

package com.streamchat.analytics_service.adapter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.streamchat.analytics_service.domain.ArchivedMessage;
import com.streamchat.analytics_service.domain.AuditedMessage;
import com.streamchat.analytics_service.domain.ChatGroup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ConsoleMetricsReporter")
class ConsoleMetricsReporterTest {

    private final ConsoleMetricsReporter reporter = new ConsoleMetricsReporter();
    private final Logger logger = (Logger) LoggerFactory.getLogger(ConsoleMetricsReporter.class);
    private final ListAppender<ILoggingEvent> registros = new ListAppender<>();

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
    @DisplayName("imprime el total acumulado del grupo en el log")
    void imprimeElTotal() {
        ChatGroup grupo = ChatGroup.nuevo("GrupoA");
        Instant ahora = Instant.parse("2026-11-13T16:30:10.123Z");
        grupo.registrarMensaje(ArchivedMessage.archivar(new AuditedMessage(UUID.randomUUID(), "Ana", "GrupoA", "uno", ahora), ahora));
        grupo.registrarMensaje(ArchivedMessage.archivar(new AuditedMessage(UUID.randomUUID(), "Luis", "GrupoA", "dos", ahora), ahora));

        reporter.reportar(grupo);

        List<String> mensajes = registros.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
        assertEquals(List.of("Mensajes totales en GrupoA: 2"), mensajes);
    }

    @Test
    @DisplayName("rechaza un grupo nulo")
    void rechazaNulo() {
        NullPointerException error = assertThrows(NullPointerException.class, () -> reporter.reportar(null));

        assertEquals("El grupo es obligatorio", error.getMessage());
    }
}

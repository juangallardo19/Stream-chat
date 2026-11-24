package com.streamchat.typing_processor.service;

import com.streamchat.typing_processor.domain.FixedWindowStrategy;
import com.streamchat.typing_processor.domain.GroupId;
import com.streamchat.typing_processor.domain.MessageSent;
import com.streamchat.typing_processor.domain.StatusChange;
import com.streamchat.typing_processor.domain.TypingEvent;
import com.streamchat.typing_processor.domain.TypingStatus;
import com.streamchat.typing_processor.domain.TypingStatusNotifier;
import com.streamchat.typing_processor.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TypingProcessorService al enviar un mensaje")
class TypingProcessorServiceEnvioTest {

    private static final Instant INICIO = Instant.parse("2026-11-13T16:30:00Z");
    private static final UserId ANA = new UserId("Ana");
    private static final GroupId GRUPO_A = new GroupId("GrupoA");
    private static final StatusChange ANA_STOPPED = new StatusChange(ANA, GRUPO_A, TypingStatus.STOPPED);

    @Mock
    private TypingStatusNotifier notifier;

    private TypingProcessorService servicio;

    @BeforeEach
    void preparar() {
        servicio = new TypingProcessorService(notifier, new FixedWindowStrategy(Duration.ofSeconds(4)),
                Clock.fixed(INICIO.plusSeconds(1), ZoneOffset.UTC));
    }

    @Test
    @DisplayName("si Ana estaba escribiendo, notifica STOPPED una vez sin esperar la ventana")
    void notificaStoppedYa() {
        servicio.procesar(new TypingEvent(ANA, GRUPO_A, INICIO));

        servicio.registrarEnvio(new MessageSent(ANA, GRUPO_A));

        verify(notifier, times(1)).notificar(ANA_STOPPED);
    }

    @Test
    @DisplayName("si nadie del grupo estaba escribiendo, no notifica nada")
    void sinActividadNoNotifica() {
        servicio.registrarEnvio(new MessageSent(ANA, GRUPO_A));

        verify(notifier, never()).notificar(any());
    }

    @Test
    @DisplayName("rechaza un envío nulo")
    void rechazaNulo() {
        NullPointerException error = assertThrows(NullPointerException.class, () -> servicio.registrarEnvio(null));

        assertEquals("El envío es obligatorio", error.getMessage());
    }
}

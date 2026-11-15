package com.streamchat.typing_processor.service;

import com.streamchat.typing_processor.domain.FixedWindowStrategy;
import com.streamchat.typing_processor.domain.GroupId;
import com.streamchat.typing_processor.domain.StatusChange;
import com.streamchat.typing_processor.domain.TypingEvent;
import com.streamchat.typing_processor.domain.TypingStatus;
import com.streamchat.typing_processor.domain.TypingStatusNotifier;
import com.streamchat.typing_processor.domain.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("TypingProcessorService")
class TypingProcessorServiceTest {

    private static final Instant INICIO = Instant.parse("2026-11-13T16:30:00Z");
    private static final UserId ANA = new UserId("Ana");
    private static final UserId LUIS = new UserId("Luis");
    private static final GroupId GRUPO_A = new GroupId("GrupoA");
    private static final StatusChange ANA_TYPING = new StatusChange(ANA, GRUPO_A, TypingStatus.TYPING);
    private static final StatusChange ANA_STOPPED = new StatusChange(ANA, GRUPO_A, TypingStatus.STOPPED);

    @Mock
    private TypingStatusNotifier notifier;

    private TypingProcessorService servicioConRelojEn(Instant ahora) {
        return new TypingProcessorService(notifier, new FixedWindowStrategy(Duration.ofSeconds(4)),
                Clock.fixed(ahora, ZoneOffset.UTC));
    }

    private static TypingEvent tecla(UserId usuario, long milisegundos) {
        return new TypingEvent(usuario, GRUPO_A, INICIO.plusMillis(milisegundos));
    }

    @Nested
    @DisplayName("procesar()")
    class Procesar {

        @Test
        @DisplayName("10 pulsaciones de Ana notifican un solo TYPING")
        void diezPulsacionesUnTyping() {
            TypingProcessorService servicio = servicioConRelojEn(INICIO);

            for (int i = 0; i < 10; i++) {
                servicio.procesar(tecla(ANA, i * 100L));
            }

            verify(notifier, times(1)).notificar(ANA_TYPING);
            verifyNoMoreInteractions(notifier);
        }

        @Test
        @DisplayName("dos usuarios del mismo grupo notifican cada uno su TYPING")
        void dosUsuariosIndependientes() {
            TypingProcessorService servicio = servicioConRelojEn(INICIO);

            servicio.procesar(tecla(ANA, 0));
            servicio.procesar(tecla(LUIS, 200));

            verify(notifier, times(1)).notificar(ANA_TYPING);
            verify(notifier, times(1)).notificar(new StatusChange(LUIS, GRUPO_A, TypingStatus.TYPING));
        }

        @Test
        @DisplayName("rechaza un evento nulo sin notificar")
        void rechazaNulo() {
            TypingProcessorService servicio = servicioConRelojEn(INICIO);

            NullPointerException error = assertThrows(NullPointerException.class, () -> servicio.procesar(null));

            assertEquals("El evento de tecleo es obligatorio", error.getMessage());
            verify(notifier, never()).notificar(any());
        }
    }

    @Nested
    @DisplayName("detectarInactividad()")
    class DetectarInactividad {

        @Test
        @DisplayName("antes de la ventana no notifica STOPPED")
        void antesDeLaVentana() {
            TypingProcessorService servicio = servicioConRelojEn(INICIO.plusSeconds(3));
            servicio.procesar(tecla(ANA, 0));

            servicio.detectarInactividad();

            verify(notifier, never()).notificar(ANA_STOPPED);
        }

        @Test
        @DisplayName("tras la ventana notifica un solo STOPPED aunque el barrido se repita")
        void trasLaVentanaUnStopped() {
            TypingProcessorService servicio = servicioConRelojEn(INICIO.plusSeconds(5));
            servicio.procesar(tecla(ANA, 0));

            servicio.detectarInactividad();
            servicio.detectarInactividad();

            verify(notifier, times(1)).notificar(ANA_TYPING);
            verify(notifier, times(1)).notificar(ANA_STOPPED);
        }

        @Test
        @DisplayName("sin actividad no notifica nada")
        void sinActividad() {
            TypingProcessorService servicio = servicioConRelojEn(INICIO);

            servicio.detectarInactividad();

            verify(notifier, never()).notificar(any());
        }
    }
}

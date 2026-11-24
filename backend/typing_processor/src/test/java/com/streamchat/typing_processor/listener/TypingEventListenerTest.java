package com.streamchat.typing_processor.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.streamchat.typing_processor.adapter.DebounceMetricsObserver;
import com.streamchat.typing_processor.domain.GroupId;
import com.streamchat.typing_processor.domain.TypingEvent;
import com.streamchat.typing_processor.domain.UserId;
import com.streamchat.typing_processor.service.TypingProcessorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TypingEventListener")
class TypingEventListenerTest {

    @Mock
    private TypingProcessorService servicio;

    @Mock
    private DebounceMetricsObserver metricas;

    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @InjectMocks
    private TypingEventListener listener;

    @Nested
    @DisplayName("Con un evento bien formado")
    class EventoValido {

        @Test
        @DisplayName("lo convierte, lo procesa una vez y lo cuenta para las métricas")
        void procesaYCuenta() {
            String json = "{\"user\":\"Ana\",\"group\":\"GrupoA\",\"occurredAt\":\"2026-11-13T16:30:01.050Z\"}";

            listener.recibir(json);

            verify(servicio, times(1)).procesar(new TypingEvent(new UserId("Ana"), new GroupId("GrupoA"),
                    Instant.parse("2026-11-13T16:30:01.050Z")));
            verify(metricas, times(1)).registrarEvento();
        }
    }

    @Nested
    @DisplayName("Con un evento mal formado")
    class EventoInvalido {

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "esto no es JSON",
                "{\"group\":\"GrupoA\",\"occurredAt\":\"2026-11-13T16:30:01.050Z\"}",
                "{\"user\":\"Ana\",\"group\":\"Grupo.A\",\"occurredAt\":\"2026-11-13T16:30:01.050Z\"}"
        })
        @DisplayName("lo salta sin lanzar error y no lo procesa")
        void loSalta(String json) {
            assertDoesNotThrow(() -> listener.recibir(json));

            verify(servicio, never()).procesar(any());
        }
    }
}

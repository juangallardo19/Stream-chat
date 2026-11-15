package com.streamchat.typing_processor.service;

import com.streamchat.typing_processor.domain.GroupId;
import com.streamchat.typing_processor.domain.StatusChange;
import com.streamchat.typing_processor.domain.TypingStatus;
import com.streamchat.typing_processor.domain.UserId;
import com.streamchat.typing_processor.port.StatusPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatusPublishingObserver")
class StatusPublishingObserverTest {

    @Mock
    private StatusPublisher statusPublisher;

    @InjectMocks
    private StatusPublishingObserver observador;

    @Test
    @DisplayName("entrega cada transición al puerto StatusPublisher una sola vez")
    void publicaUnaVez() {
        StatusChange cambio = new StatusChange(new UserId("Ana"), new GroupId("GrupoA"), TypingStatus.STOPPED);

        observador.alCambiarEstado(cambio);

        verify(statusPublisher, times(1)).publicar(cambio);
    }

    @Test
    @DisplayName("rechaza una transición nula sin publicar")
    void rechazaNula() {
        NullPointerException error = assertThrows(NullPointerException.class, () -> observador.alCambiarEstado(null));

        assertEquals("La transición de estado es obligatoria", error.getMessage());
        verify(statusPublisher, never()).publicar(any());
    }
}

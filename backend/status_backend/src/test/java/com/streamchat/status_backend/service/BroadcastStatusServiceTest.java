package com.streamchat.status_backend.service;

import com.streamchat.status_backend.domain.GroupId;
import com.streamchat.status_backend.domain.StatusUpdate;
import com.streamchat.status_backend.domain.TypingStatus;
import com.streamchat.status_backend.domain.UserId;
import com.streamchat.status_backend.port.StatusBroadcaster;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BroadcastStatusService")
class BroadcastStatusServiceTest {

    @Mock
    private StatusBroadcaster broadcaster;

    @InjectMocks
    private BroadcastStatusService servicio;

    private final StatusUpdate actualizacion =
            new StatusUpdate(new UserId("Ana"), new GroupId("GrupoA"), TypingStatus.TYPING);

    @Test
    @DisplayName("entrega la actualización al puerto exactamente una vez")
    void difundeUnaVez() {
        when(broadcaster.difundir(actualizacion)).thenReturn(2);

        servicio.difundir(actualizacion);

        verify(broadcaster, times(1)).difundir(actualizacion);
    }

    @Test
    @DisplayName("un grupo sin sesiones conectadas no falla")
    void grupoSinSesiones() {
        when(broadcaster.difundir(actualizacion)).thenReturn(0);

        assertDoesNotThrow(() -> servicio.difundir(actualizacion));

        verify(broadcaster, times(1)).difundir(actualizacion);
    }

    @Test
    @DisplayName("rechaza una actualización nula sin difundir nada")
    void rechazaNula() {
        NullPointerException error = assertThrows(NullPointerException.class, () -> servicio.difundir(null));

        assertEquals("La actualización de estado es obligatoria", error.getMessage());
        verify(broadcaster, never()).difundir(any());
    }
}

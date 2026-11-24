package com.streamchat.status_backend.adapter;

import com.streamchat.status_backend.domain.GroupId;
import com.streamchat.status_backend.domain.GroupSessions;
import com.streamchat.status_backend.domain.StatusUpdate;
import com.streamchat.status_backend.domain.TypingStatus;
import com.streamchat.status_backend.domain.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketStatusBroadcaster")
class WebSocketStatusBroadcasterTest {

    @Mock
    private GroupSessions sesiones;

    @InjectMocks
    private WebSocketStatusBroadcaster broadcaster;

    @Test
    @DisplayName("delega la difusión en las sesiones una vez y devuelve cuántas recibieron")
    void delegaEnLasSesiones() {
        StatusUpdate actualizacion = new StatusUpdate(new UserId("Ana"), new GroupId("GrupoA"), TypingStatus.TYPING);
        when(sesiones.difundir(actualizacion)).thenReturn(3);

        int entregas = broadcaster.difundir(actualizacion);

        assertEquals(3, entregas);
        verify(sesiones, times(1)).difundir(actualizacion);
    }
}

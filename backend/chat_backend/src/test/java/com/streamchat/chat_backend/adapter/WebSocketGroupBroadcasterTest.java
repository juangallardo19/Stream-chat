package com.streamchat.chat_backend.adapter;

import com.streamchat.chat_backend.domain.ChatMessage;
import com.streamchat.chat_backend.domain.GroupId;
import com.streamchat.chat_backend.domain.GroupSessions;
import com.streamchat.chat_backend.domain.MessageText;
import com.streamchat.chat_backend.domain.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketGroupBroadcaster")
class WebSocketGroupBroadcasterTest {

    @Mock
    private GroupSessions sesiones;

    @InjectMocks
    private WebSocketGroupBroadcaster broadcaster;

    private final ChatMessage mensaje = new ChatMessage(UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b"),
            new UserId("Ana"), new GroupId("GrupoA"), new MessageText("¡Hola!"),
            Instant.parse("2026-11-13T16:30:10.123Z"));

    @Test
    @DisplayName("delega la difusión en las sesiones del grupo una sola vez")
    void delegaEnLasSesiones() {
        when(sesiones.difundir(mensaje)).thenReturn(3);

        broadcaster.difundir(mensaje);

        verify(sesiones, times(1)).difundir(mensaje);
    }

    @Test
    @DisplayName("devuelve cuántas sesiones recibieron el mensaje")
    void devuelveEntregas() {
        when(sesiones.difundir(mensaje)).thenReturn(3);

        int entregas = broadcaster.difundir(mensaje);

        assertEquals(3, entregas);
    }
}

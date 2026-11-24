package com.streamchat.status_backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.streamchat.status_backend.domain.GroupId;
import com.streamchat.status_backend.domain.StatusUpdate;
import com.streamchat.status_backend.domain.TypingStatus;
import com.streamchat.status_backend.domain.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketStatusSubscriber")
class WebSocketStatusSubscriberTest {

    @Mock
    private WebSocketSession sesion;

    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder().build();

    @InjectMocks
    private WebSocketStatusSubscriber suscriptor;

    @Captor
    private ArgumentCaptor<TextMessage> textoCaptor;

    private final StatusUpdate actualizacion =
            new StatusUpdate(new UserId("Ana"), new GroupId("GrupoA"), TypingStatus.TYPING);

    @Test
    @DisplayName("envía al navegador el JSON exacto del contrato")
    void enviaElJson() throws IOException {
        when(sesion.isOpen()).thenReturn(true);

        suscriptor.recibir(actualizacion);

        verify(sesion).sendMessage(textoCaptor.capture());
        assertEquals("{\"user\":\"Ana\",\"group\":\"GrupoA\",\"status\":\"TYPING\"}", textoCaptor.getValue().getPayload());
    }

    @Test
    @DisplayName("no intenta enviar a una pestaña ya cerrada")
    void noEnviaACerrada() throws IOException {
        when(sesion.isOpen()).thenReturn(false);

        suscriptor.recibir(actualizacion);

        verify(sesion, never()).sendMessage(any());
    }

    @Test
    @DisplayName("si falla el envío a una pestaña, no interrumpe a las demás")
    void fallaNoSePropaga() throws IOException {
        when(sesion.isOpen()).thenReturn(true);
        doThrow(new IOException("Conexión reiniciada")).when(sesion).sendMessage(any());

        assertDoesNotThrow(() -> suscriptor.recibir(actualizacion));

        verify(sesion).sendMessage(any());
    }
}

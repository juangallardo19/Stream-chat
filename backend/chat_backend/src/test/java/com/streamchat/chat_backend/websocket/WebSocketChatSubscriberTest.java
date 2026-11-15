package com.streamchat.chat_backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.streamchat.chat_backend.domain.ChatMessage;
import com.streamchat.chat_backend.domain.GroupId;
import com.streamchat.chat_backend.domain.MessageText;
import com.streamchat.chat_backend.domain.UserId;
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
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketChatSubscriber")
class WebSocketChatSubscriberTest {

    @Mock
    private WebSocketSession sesion;

    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @InjectMocks
    private WebSocketChatSubscriber suscriptor;

    @Captor
    private ArgumentCaptor<TextMessage> textoCaptor;

    private final ChatMessage mensaje = new ChatMessage(UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b"),
            new UserId("Ana"), new GroupId("GrupoA"), new MessageText("¡Hola!"),
            Instant.parse("2026-11-13T16:30:10.123Z"));

    @Test
    @DisplayName("envía al navegador el JSON exacto del contrato de la sección 5.5")
    void enviaElJsonDelContrato() throws IOException {
        when(sesion.isOpen()).thenReturn(true);

        suscriptor.recibir(mensaje);

        verify(sesion).sendMessage(textoCaptor.capture());
        assertEquals("{\"messageId\":\"3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b\",\"user\":\"Ana\",\"group\":\"GrupoA\","
                + "\"text\":\"¡Hola!\",\"sentAt\":\"2026-11-13T16:30:10.123Z\"}", textoCaptor.getValue().getPayload());
    }

    @Test
    @DisplayName("no intenta enviar a una sesión que ya se cerró")
    void noEnviaASesionCerrada() throws IOException {
        when(sesion.isOpen()).thenReturn(false);

        suscriptor.recibir(mensaje);

        verify(sesion, never()).sendMessage(any());
    }

    @Test
    @DisplayName("si falla el envío a un navegador, lo registra y no interrumpe a los demás")
    void fallaDeEnvioNoSePropaga() throws IOException {
        when(sesion.isOpen()).thenReturn(true);
        doThrow(new IOException("Conexión reiniciada")).when(sesion).sendMessage(any());

        assertDoesNotThrow(() -> suscriptor.recibir(mensaje));

        verify(sesion).sendMessage(any());
    }
}

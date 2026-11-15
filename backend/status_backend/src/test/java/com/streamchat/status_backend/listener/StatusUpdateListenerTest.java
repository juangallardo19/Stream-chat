package com.streamchat.status_backend.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.streamchat.status_backend.domain.GroupId;
import com.streamchat.status_backend.domain.StatusUpdate;
import com.streamchat.status_backend.domain.TypingStatus;
import com.streamchat.status_backend.domain.UserId;
import com.streamchat.status_backend.service.BroadcastStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatusUpdateListener")
class StatusUpdateListenerTest {

    @Mock
    private BroadcastStatusService servicio;

    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder().build();

    @InjectMocks
    private StatusUpdateListener listener;

    private static Message mensajeAmqp(String cuerpo) {
        return new Message(cuerpo.getBytes(StandardCharsets.UTF_8), new MessageProperties());
    }

    @Test
    @DisplayName("convierte el JSON de status_updates y lo difunde una vez")
    void difundeUnaVez() {
        String json = "{\"user\":\"Ana\",\"group\":\"GrupoA\",\"status\":\"TYPING\"}";

        listener.recibir(mensajeAmqp(json));

        verify(servicio, times(1)).difundir(
                new StatusUpdate(new UserId("Ana"), new GroupId("GrupoA"), TypingStatus.TYPING));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "esto no es JSON",
            "{\"user\":\"Ana\",\"group\":\"GrupoA\",\"status\":\"WRITING\"}",
            "{\"user\":\"Ana\",\"group\":\"Grupo.A\",\"status\":\"TYPING\"}",
            "{\"group\":\"GrupoA\",\"status\":\"TYPING\"}"
    })
    @DisplayName("un mensaje mal formado se descarta sin volver a la cola y no se difunde")
    void descartaMalFormado(String cuerpo) {
        Message mensaje = mensajeAmqp(cuerpo);

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.recibir(mensaje));

        verify(servicio, never()).difundir(any());
    }
}

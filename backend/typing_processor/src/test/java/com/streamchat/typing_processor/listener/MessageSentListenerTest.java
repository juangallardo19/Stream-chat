package com.streamchat.typing_processor.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.streamchat.typing_processor.domain.GroupId;
import com.streamchat.typing_processor.domain.MessageSent;
import com.streamchat.typing_processor.domain.UserId;
import com.streamchat.typing_processor.service.TypingProcessorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageSentListener")
class MessageSentListenerTest {

    @Mock
    private TypingProcessorService servicio;

    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder().build();

    @InjectMocks
    private MessageSentListener listener;

    @Test
    @DisplayName("toma autor y grupo del registro de auditoría e ignora los demás campos")
    void registraElEnvio() {
        String json = "{\"messageId\":\"3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b\",\"user\":\"Ana\",\"group\":\"GrupoA\","
                + "\"text\":\"¡Hola!\",\"sentAt\":\"2026-11-13T16:30:10.123Z\"}";

        listener.recibir(json);

        verify(servicio, times(1)).registrarEnvio(new MessageSent(new UserId("Ana"), new GroupId("GrupoA")));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "esto no es JSON",
            "{\"group\":\"GrupoA\",\"text\":\"hola\"}",
            "{\"user\":\"Ana\",\"group\":\"Grupo.A\"}"
    })
    @DisplayName("un registro mal formado se salta sin lanzar error")
    void saltaMalFormado(String json) {
        assertDoesNotThrow(() -> listener.recibir(json));

        verify(servicio, never()).registrarEnvio(any());
    }
}

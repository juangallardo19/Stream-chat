package com.streamchat.chat_backend.service;

import com.streamchat.chat_backend.domain.ChatMessage;
import com.streamchat.chat_backend.domain.GroupId;
import com.streamchat.chat_backend.domain.MessageText;
import com.streamchat.chat_backend.domain.UserId;
import com.streamchat.chat_backend.port.GroupBroadcaster;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliverMessageService")
class DeliverMessageServiceTest {

    @Mock
    private GroupBroadcaster broadcaster;

    @InjectMocks
    private DeliverMessageService servicio;

    private final ChatMessage mensaje = new ChatMessage(
            UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b"),
            new UserId("Ana"),
            new GroupId("GrupoA"),
            new MessageText("¡Hola!"),
            Instant.parse("2026-11-13T16:30:10.123Z"));

    @Nested
    @DisplayName("Con un mensaje válido")
    class MensajeValido {

        @Test
        @DisplayName("lo entrega al puerto exactamente una vez")
        void entregaUnaVez() {
            when(broadcaster.difundir(mensaje)).thenReturn(2);

            servicio.entregar(mensaje);

            verify(broadcaster, times(1)).difundir(mensaje);
        }

        @Test
        @DisplayName("un grupo sin sesiones conectadas no falla")
        void grupoSinSesionesNoFalla() {
            when(broadcaster.difundir(mensaje)).thenReturn(0);

            assertDoesNotThrow(() -> servicio.entregar(mensaje));

            verify(broadcaster, times(1)).difundir(mensaje);
        }
    }

    @Nested
    @DisplayName("Con errores")
    class Errores {

        @Test
        @DisplayName("rechaza un mensaje nulo y no difunde nada")
        void rechazaMensajeNulo() {
            NullPointerException error = assertThrows(NullPointerException.class, () -> servicio.entregar(null));

            assertEquals("El mensaje es obligatorio", error.getMessage());
            verify(broadcaster, never()).difundir(any());
        }

        @Test
        @DisplayName("si el puerto falla, propaga el error para que RabbitMQ no dé el mensaje por entregado")
        void propagaErrorDelPuerto() {
            when(broadcaster.difundir(mensaje)).thenThrow(new IllegalStateException("Fallo al difundir"));

            IllegalStateException error = assertThrows(IllegalStateException.class, () -> servicio.entregar(mensaje));

            assertEquals("Fallo al difundir", error.getMessage());
        }
    }
}

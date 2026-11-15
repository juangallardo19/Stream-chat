package com.streamchat.chat_api.service;

import com.streamchat.chat_api.domain.ChatMessage;
import com.streamchat.chat_api.domain.GroupCatalog;
import com.streamchat.chat_api.domain.GroupId;
import com.streamchat.chat_api.domain.MessageText;
import com.streamchat.chat_api.domain.UserId;
import com.streamchat.chat_api.dto.SendMessageRequest;
import com.streamchat.chat_api.dto.SendMessageResponse;
import com.streamchat.chat_api.exception.GrupoNoEncontradoException;
import com.streamchat.chat_api.port.ChatMessagePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendMessageService")
class SendMessageServiceTest {

    private static final Instant INSTANTE = Instant.parse("2026-11-13T16:30:10.123Z");

    @Mock
    private ChatMessagePublisher publisher;

    @Captor
    private ArgumentCaptor<ChatMessage> mensajeCaptor;

    private SendMessageService servicio;

    @BeforeEach
    void prepararServicio() {
        GroupCatalog catalogo = GroupCatalog.desde(List.of("GrupoA", "GrupoB"));
        servicio = new SendMessageService(publisher, catalogo, Clock.fixed(INSTANTE, ZoneOffset.UTC));
    }

    @Nested
    @DisplayName("Con una solicitud válida")
    class SolicitudValida {

        @Test
        @DisplayName("publica el mensaje una sola vez en el puerto")
        void publicaUnaSolaVez() {
            SendMessageRequest solicitud = new SendMessageRequest("Ana", "GrupoA", "¡Hola!");

            servicio.enviar(solicitud);

            verify(publisher, times(1)).publicar(any(ChatMessage.class));
        }

        @Test
        @DisplayName("el mensaje publicado lleva usuario, grupo, texto y la hora del reloj")
        void mensajePublicadoConDatosCompletos() {
            SendMessageRequest solicitud = new SendMessageRequest("Ana", "GrupoA", "¡Hola!");

            servicio.enviar(solicitud);

            verify(publisher).publicar(mensajeCaptor.capture());
            ChatMessage publicado = mensajeCaptor.getValue();
            assertAll(
                    () -> assertEquals(new UserId("Ana"), publicado.user()),
                    () -> assertEquals(new GroupId("GrupoA"), publicado.group()),
                    () -> assertEquals(new MessageText("¡Hola!"), publicado.text()),
                    () -> assertEquals(INSTANTE, publicado.sentAt()),
                    () -> assertEquals("chat.grupoa", publicado.routingKey()));
        }

        @Test
        @DisplayName("publica con el nombre canónico del catálogo aunque llegue en minúsculas")
        void publicaConNombreCanonico() {
            SendMessageRequest solicitud = new SendMessageRequest("Ana", "grupoa", "¡Hola!");

            servicio.enviar(solicitud);

            verify(publisher).publicar(mensajeCaptor.capture());
            assertEquals(new GroupId("GrupoA"), mensajeCaptor.getValue().group());
        }

        @Test
        @DisplayName("responde con el identificador y la hora del mensaje publicado")
        void respondeConIdentificadorYHora() {
            SendMessageRequest solicitud = new SendMessageRequest("Ana", "GrupoA", "¡Hola!");

            SendMessageResponse respuesta = servicio.enviar(solicitud);

            verify(publisher).publicar(mensajeCaptor.capture());
            assertAll(
                    () -> assertEquals(mensajeCaptor.getValue().messageId(), respuesta.messageId()),
                    () -> assertEquals(INSTANTE, respuesta.sentAt()));
        }
    }

    @Nested
    @DisplayName("Con un grupo que no está en el catálogo")
    class GrupoDesconocido {

        @Test
        @DisplayName("lanza GrupoNoEncontradoException y no publica nada")
        void noPublicaConGrupoDesconocido() {
            SendMessageRequest solicitud = new SendMessageRequest("Ana", "GrupoZ", "¡Hola!");

            GrupoNoEncontradoException error = assertThrows(GrupoNoEncontradoException.class,
                    () -> servicio.enviar(solicitud));

            assertEquals("El grupo GrupoZ no existe", error.getMessage());
            verify(publisher, never()).publicar(any());
        }
    }

    @Nested
    @DisplayName("Con datos que violan el dominio")
    class DatosInvalidos {

        @Test
        @DisplayName("no publica si el grupo trae caracteres de routing key")
        void noPublicaConGrupoInvalido() {
            SendMessageRequest solicitud = new SendMessageRequest("Ana", "Grupo.A", "¡Hola!");

            assertThrows(IllegalArgumentException.class, () -> servicio.enviar(solicitud));

            verify(publisher, never()).publicar(any());
        }

        @Test
        @DisplayName("no publica si el usuario está en blanco")
        void noPublicaConUsuarioEnBlanco() {
            SendMessageRequest solicitud = new SendMessageRequest("   ", "GrupoA", "¡Hola!");

            assertThrows(IllegalArgumentException.class, () -> servicio.enviar(solicitud));

            verify(publisher, never()).publicar(any());
        }

        @Test
        @DisplayName("no publica si el texto supera los 1000 caracteres")
        void noPublicaConTextoDemasiadoLargo() {
            SendMessageRequest solicitud = new SendMessageRequest("Ana", "GrupoA", "t".repeat(1001));

            assertThrows(IllegalArgumentException.class, () -> servicio.enviar(solicitud));

            verify(publisher, never()).publicar(any());
        }
    }

    @Nested
    @DisplayName("Cuando el broker no confirma")
    class BrokerFalla {

        @Test
        @DisplayName("propaga el error para que el cliente no reciba 202")
        void propagaElError() {
            SendMessageRequest solicitud = new SendMessageRequest("Ana", "GrupoA", "¡Hola!");
            doThrow(new IllegalStateException("RabbitMQ no confirmó el mensaje")).when(publisher).publicar(any());

            IllegalStateException error = assertThrows(IllegalStateException.class, () -> servicio.enviar(solicitud));

            assertEquals("RabbitMQ no confirmó el mensaje", error.getMessage());
        }
    }
}

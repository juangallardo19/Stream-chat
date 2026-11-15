package com.streamchat.typing_api.service;

import com.streamchat.typing_api.domain.GroupCatalog;
import com.streamchat.typing_api.domain.GroupId;
import com.streamchat.typing_api.domain.TypingEvent;
import com.streamchat.typing_api.domain.UserId;
import com.streamchat.typing_api.dto.RegisterTypingRequest;
import com.streamchat.typing_api.exception.GrupoNoEncontradoException;
import com.streamchat.typing_api.port.TypingEventPublisher;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterTypingService")
class RegisterTypingServiceTest {

    private static final Instant INSTANTE = Instant.parse("2026-11-13T16:30:01.050Z");

    @Mock
    private TypingEventPublisher publisher;

    @Captor
    private ArgumentCaptor<TypingEvent> eventoCaptor;

    private RegisterTypingService servicio;

    @BeforeEach
    void prepararServicio() {
        GroupCatalog catalogo = GroupCatalog.desde(List.of("GrupoA", "GrupoB"));
        servicio = new RegisterTypingService(publisher, catalogo, Clock.fixed(INSTANTE, ZoneOffset.UTC));
    }

    @Nested
    @DisplayName("Con una solicitud válida")
    class SolicitudValida {

        @Test
        @DisplayName("publica el evento una sola vez en el puerto")
        void publicaUnaSolaVez() {
            RegisterTypingRequest solicitud = new RegisterTypingRequest("Ana", "GrupoA");

            servicio.registrar(solicitud);

            verify(publisher, times(1)).publicar(any(TypingEvent.class));
        }

        @Test
        @DisplayName("el evento publicado lleva usuario, grupo y la hora del reloj")
        void eventoConDatosCompletos() {
            RegisterTypingRequest solicitud = new RegisterTypingRequest("Ana", "GrupoA");

            servicio.registrar(solicitud);

            verify(publisher).publicar(eventoCaptor.capture());
            assertEquals(new TypingEvent(new UserId("Ana"), new GroupId("GrupoA"), INSTANTE), eventoCaptor.getValue());
        }

        @Test
        @DisplayName("publica con el nombre canónico del catálogo aunque llegue en minúsculas")
        void publicaConNombreCanonico() {
            RegisterTypingRequest solicitud = new RegisterTypingRequest("Ana", "grupoa");

            servicio.registrar(solicitud);

            verify(publisher).publicar(eventoCaptor.capture());
            assertEquals("GrupoA", eventoCaptor.getValue().claveParticion());
        }
    }

    @Nested
    @DisplayName("Con un grupo que no está en el catálogo")
    class GrupoDesconocido {

        @Test
        @DisplayName("lanza GrupoNoEncontradoException y no publica nada")
        void noPublica() {
            RegisterTypingRequest solicitud = new RegisterTypingRequest("Ana", "GrupoZ");

            GrupoNoEncontradoException error = assertThrows(GrupoNoEncontradoException.class,
                    () -> servicio.registrar(solicitud));

            assertEquals("El grupo GrupoZ no existe", error.getMessage());
            verify(publisher, never()).publicar(any());
        }
    }

    @Nested
    @DisplayName("Con datos que violan el dominio")
    class DatosInvalidos {

        @Test
        @DisplayName("no publica si el usuario está en blanco")
        void noPublicaConUsuarioEnBlanco() {
            RegisterTypingRequest solicitud = new RegisterTypingRequest("   ", "GrupoA");

            assertThrows(IllegalArgumentException.class, () -> servicio.registrar(solicitud));

            verify(publisher, never()).publicar(any());
        }

        @Test
        @DisplayName("rechaza una solicitud nula sin publicar")
        void rechazaSolicitudNula() {
            NullPointerException error = assertThrows(NullPointerException.class, () -> servicio.registrar(null));

            assertEquals("La solicitud es obligatoria", error.getMessage());
            verify(publisher, never()).publicar(any());
        }
    }
}

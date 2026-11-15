package com.streamchat.typing_processor.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Señal \"mensaje enviado\"")
class MessageSentSignalTest {

    private static final Instant INICIO = Instant.parse("2026-11-13T16:30:00Z");
    private static final UserId ANA = new UserId("Ana");
    private static final UserId LUIS = new UserId("Luis");
    private static final GroupId GRUPO_A = new GroupId("GrupoA");
    private static final StatusChange ANA_STOPPED = new StatusChange(ANA, GRUPO_A, TypingStatus.STOPPED);
    private static final StatusChange ANA_TYPING = new StatusChange(ANA, GRUPO_A, TypingStatus.TYPING);

    @Nested
    @DisplayName("En el patrón State")
    class Estados {

        @Test
        @DisplayName("TypingState al enviar mensaje pasa a StoppedState")
        void typingPasaAStopped() {
            ActivityState siguiente = new TypingState().alEnviarMensaje();

            assertInstanceOf(StoppedState.class, siguiente);
        }

        @Test
        @DisplayName("StoppedState al enviar mensaje se queda igual")
        void stoppedSeQueda() {
            StoppedState detenido = new StoppedState();

            ActivityState siguiente = detenido.alEnviarMensaje();

            assertSame(detenido, siguiente);
        }
    }

    @Nested
    @DisplayName("En UserTypingState")
    class Usuario {

        @Test
        @DisplayName("si estaba escribiendo, enviar produce STOPPED de inmediato, sin esperar la ventana")
        void enviarDetieneYa() {
            UserTypingState estado = UserTypingState.nuevo(ANA, GRUPO_A);
            estado.registrarPulsacion(INICIO);

            Optional<StatusChange> cambio = estado.registrarEnvio();

            assertEquals(Optional.of(ANA_STOPPED), cambio);
        }

        @Test
        @DisplayName("si ya estaba detenido, enviar no produce transición")
        void detenidoNoCambia() {
            UserTypingState estado = UserTypingState.nuevo(ANA, GRUPO_A);

            Optional<StatusChange> cambio = estado.registrarEnvio();

            assertTrue(cambio.isEmpty());
        }

        @Test
        @DisplayName("volver a teclear después de enviar produce un nuevo TYPING")
        void volverATeclearEsTyping() {
            UserTypingState estado = UserTypingState.nuevo(ANA, GRUPO_A);
            estado.registrarPulsacion(INICIO);
            estado.registrarEnvio();

            Optional<StatusChange> cambio = estado.registrarPulsacion(INICIO.plusSeconds(1));

            assertEquals(Optional.of(ANA_TYPING), cambio);
        }
    }

    @Nested
    @DisplayName("En ChatGroup")
    class Grupo {

        @Test
        @DisplayName("detiene solo al autor del mensaje y lo retira de memoria")
        void detieneSoloAlAutor() {
            ChatGroup grupo = ChatGroup.nuevo(GRUPO_A);
            grupo.registrarActividad(ANA, INICIO);
            grupo.registrarActividad(LUIS, INICIO);

            Optional<StatusChange> cambio = grupo.registrarEnvio(ANA);

            assertEquals(Optional.of(ANA_STOPPED), cambio);
            assertEquals(1, grupo.usuariosActivos());
        }

        @Test
        @DisplayName("el barrido posterior no repite el STOPPED del autor")
        void barridoNoRepite() {
            ChatGroup grupo = ChatGroup.nuevo(GRUPO_A);
            grupo.registrarActividad(ANA, INICIO);
            grupo.registrarEnvio(ANA);

            assertTrue(grupo.evaluarInactividad(INICIO.plusSeconds(10),
                    new FixedWindowStrategy(Duration.ofSeconds(4))).isEmpty());
        }

        @Test
        @DisplayName("un autor que no estaba escribiendo no produce transición ni ocupa memoria")
        void autorDesconocido() {
            ChatGroup grupo = ChatGroup.nuevo(GRUPO_A);

            Optional<StatusChange> cambio = grupo.registrarEnvio(ANA);

            assertTrue(cambio.isEmpty());
            assertEquals(0, grupo.usuariosActivos());
        }
    }

    @Nested
    @DisplayName("MessageSent")
    class Evento {

        @Test
        @DisplayName("rechaza un envío sin usuario o sin grupo")
        void rechazaNulos() {
            NullPointerException sinUsuario = assertThrows(NullPointerException.class,
                    () -> new MessageSent(null, GRUPO_A));
            NullPointerException sinGrupo = assertThrows(NullPointerException.class,
                    () -> new MessageSent(ANA, null));

            assertEquals("El usuario es obligatorio", sinUsuario.getMessage());
            assertEquals("El grupo es obligatorio", sinGrupo.getMessage());
        }
    }
}

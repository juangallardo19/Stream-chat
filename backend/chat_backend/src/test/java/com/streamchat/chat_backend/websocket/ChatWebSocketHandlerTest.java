package com.streamchat.chat_backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.streamchat.chat_backend.domain.ChatMessage;
import com.streamchat.chat_backend.domain.GroupCatalog;
import com.streamchat.chat_backend.domain.GroupId;
import com.streamchat.chat_backend.domain.GroupSessions;
import com.streamchat.chat_backend.domain.MessageText;
import com.streamchat.chat_backend.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatWebSocketHandler")
class ChatWebSocketHandlerTest {

    private static final GroupId GRUPO_A = new GroupId("GrupoA");

    @Mock
    private WebSocketSession sesion;

    private final Map<String, Object> atributos = new HashMap<>();
    private GroupSessions sesiones;
    private ChatWebSocketHandler handler;

    @BeforeEach
    void preparar() {
        sesiones = new GroupSessions();
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        handler = new ChatWebSocketHandler(sesiones, GroupCatalog.desde(List.of("GrupoA", "GrupoB")), objectMapper);
        lenient().when(sesion.getAttributes()).thenReturn(atributos);
    }

    private void conectarCon(String consulta) {
        when(sesion.getUri()).thenReturn(URI.create("ws://localhost:8081/ws/chat?" + consulta));
    }

    @Nested
    @DisplayName("Al conectarse a un grupo del catálogo")
    class GrupoValido {

        @Test
        @DisplayName("suscribe la sesión al grupo con su nombre canónico aunque llegue en minúsculas")
        void suscribeConNombreCanonico() throws Exception {
            conectarCon("user=Ana&group=grupoa");

            handler.afterConnectionEstablished(sesion);

            assertEquals(Set.of(GRUPO_A), sesiones.gruposActivos());
            verify(sesion, never()).close(any());
        }

        @Test
        @DisplayName("la sesión suscrita recibe los mensajes de su grupo")
        void recibeMensajesDelGrupo() throws Exception {
            conectarCon("user=Ana&group=GrupoA");
            when(sesion.isOpen()).thenReturn(true);
            handler.afterConnectionEstablished(sesion);

            int entregas = sesiones.difundir(new ChatMessage(UUID.randomUUID(), new UserId("Luis"), GRUPO_A,
                    new MessageText("¡Hola!"), Instant.parse("2026-11-13T16:30:10.123Z")));

            assertEquals(1, entregas);
            verify(sesion).sendMessage(any());
        }

        @Test
        @DisplayName("al cerrarse la conexión, la sesión se retira del grupo")
        void retiraAlCerrar() throws Exception {
            conectarCon("user=Ana&group=GrupoA");
            handler.afterConnectionEstablished(sesion);

            handler.afterConnectionClosed(sesion, CloseStatus.NORMAL);

            assertTrue(sesiones.gruposActivos().isEmpty());
        }
    }

    @Nested
    @DisplayName("Al conectarse con un grupo inválido cierra la conexión (regla 6b)")
    class GrupoInvalido {

        @Test
        @DisplayName("grupo que no está en el catálogo")
        void cierraGrupoDesconocido() throws Exception {
            conectarCon("user=Ana&group=GrupoZ");

            handler.afterConnectionEstablished(sesion);

            verify(sesion).close(CloseStatus.POLICY_VIOLATION.withReason("El grupo GrupoZ no existe"));
            assertTrue(sesiones.gruposActivos().isEmpty());
        }

        @Test
        @DisplayName("sin parámetro group")
        void cierraSinGrupo() throws Exception {
            conectarCon("user=Ana");

            handler.afterConnectionEstablished(sesion);

            verify(sesion).close(CloseStatus.POLICY_VIOLATION.withReason("El grupo es obligatorio"));
            assertTrue(sesiones.gruposActivos().isEmpty());
        }

        @Test
        @DisplayName("grupo con formato inválido")
        void cierraFormatoInvalido() throws Exception {
            conectarCon("user=Ana&group=Grupo.A");

            handler.afterConnectionEstablished(sesion);

            verify(sesion).close(CloseStatus.POLICY_VIOLATION
                    .withReason("El grupo solo admite letras, números, guion y guion bajo"));
            assertTrue(sesiones.gruposActivos().isEmpty());
        }

        @Test
        @DisplayName("cerrar una sesión que nunca se suscribió no falla")
        void cerrarSinSuscripcionNoFalla() throws Exception {
            conectarCon("user=Ana&group=GrupoZ");
            handler.afterConnectionEstablished(sesion);

            assertDoesNotThrow(() -> handler.afterConnectionClosed(sesion, CloseStatus.POLICY_VIOLATION));

            assertTrue(sesiones.gruposActivos().isEmpty());
        }
    }
}

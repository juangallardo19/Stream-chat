package com.streamchat.status_backend.websocket;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.streamchat.status_backend.domain.GroupCatalog;
import com.streamchat.status_backend.domain.GroupId;
import com.streamchat.status_backend.domain.GroupSessions;
import com.streamchat.status_backend.domain.StatusUpdate;
import com.streamchat.status_backend.domain.TypingStatus;
import com.streamchat.status_backend.domain.UserId;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatusWebSocketHandler")
class StatusWebSocketHandlerTest {

    private static final GroupId GRUPO_A = new GroupId("GrupoA");

    @Mock
    private WebSocketSession sesion;

    private final Map<String, Object> atributos = new HashMap<>();
    private GroupSessions sesiones;
    private StatusWebSocketHandler handler;

    @BeforeEach
    void preparar() {
        sesiones = new GroupSessions();
        handler = new StatusWebSocketHandler(sesiones, GroupCatalog.desde(List.of("GrupoA", "GrupoB")),
                JsonMapper.builder().build());
        lenient().when(sesion.getAttributes()).thenReturn(atributos);
    }

    private void conectarCon(String consulta) {
        when(sesion.getUri()).thenReturn(URI.create("ws://localhost:8082/ws/status?" + consulta));
    }

    @Nested
    @DisplayName("Al conectarse a un grupo del catálogo")
    class GrupoValido {

        @Test
        @DisplayName("suscribe la pestaña al grupo con su nombre canónico")
        void suscribeConNombreCanonico() throws Exception {
            conectarCon("user=Luis&group=grupoa");

            handler.afterConnectionEstablished(sesion);

            assertEquals(Set.of(GRUPO_A), sesiones.gruposActivos());
            verify(sesion, never()).close(any());
        }

        @Test
        @DisplayName("la pestaña suscrita recibe los estados de su grupo")
        void recibeEstados() throws Exception {
            conectarCon("user=Luis&group=GrupoA");
            when(sesion.isOpen()).thenReturn(true);
            handler.afterConnectionEstablished(sesion);

            int entregas = sesiones.difundir(new StatusUpdate(new UserId("Ana"), GRUPO_A, TypingStatus.TYPING));

            assertEquals(1, entregas);
            verify(sesion).sendMessage(any());
        }

        @Test
        @DisplayName("al cerrarse la pestaña, se retira del grupo")
        void retiraAlCerrar() throws Exception {
            conectarCon("user=Luis&group=GrupoA");
            handler.afterConnectionEstablished(sesion);

            handler.afterConnectionClosed(sesion, CloseStatus.NORMAL);

            assertTrue(sesiones.gruposActivos().isEmpty());
        }
    }

    @Nested
    @DisplayName("Con un grupo inválido cierra la conexión (regla 6b)")
    class GrupoInvalido {

        @Test
        @DisplayName("grupo fuera del catálogo")
        void cierraDesconocido() throws Exception {
            conectarCon("user=Luis&group=GrupoZ");

            handler.afterConnectionEstablished(sesion);

            verify(sesion).close(CloseStatus.POLICY_VIOLATION.withReason("El grupo GrupoZ no existe"));
            assertTrue(sesiones.gruposActivos().isEmpty());
        }

        @Test
        @DisplayName("sin parámetro group")
        void cierraSinGrupo() throws Exception {
            conectarCon("user=Luis");

            handler.afterConnectionEstablished(sesion);

            verify(sesion).close(CloseStatus.POLICY_VIOLATION.withReason("El grupo es obligatorio"));
        }
    }
}

package com.streamchat.status_backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamchat.status_backend.domain.StatusSubscriber;
import com.streamchat.status_backend.domain.GroupCatalog;
import com.streamchat.status_backend.domain.GroupId;
import com.streamchat.status_backend.domain.GroupSessions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

public class StatusWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(StatusWebSocketHandler.class);
    private static final String ATRIBUTO_GRUPO = "estado.grupo";
    private static final String ATRIBUTO_SUSCRIPTOR = "estado.suscriptor";
    private static final int TIEMPO_MAXIMO_ENVIO_MS = 5_000;
    private static final int BUFFER_MAXIMO_BYTES = 64 * 1024;

    private final GroupSessions sesiones;
    private final GroupCatalog catalogo;
    private final ObjectMapper objectMapper;

    public StatusWebSocketHandler(GroupSessions sesiones, GroupCatalog catalogo, ObjectMapper objectMapper) {
        this.sesiones = Objects.requireNonNull(sesiones, "Las sesiones de grupo son obligatorias");
        this.catalogo = Objects.requireNonNull(catalogo, "El catálogo de grupos es obligatorio");
        this.objectMapper = Objects.requireNonNull(objectMapper, "El ObjectMapper es obligatorio");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession sesion) throws Exception {
        GroupId grupo;
        try {
            grupo = catalogo.resolver(parametro(sesion, "group"));
        } catch (RuntimeException error) {
            LOG.info("Conexión WebSocket rechazada: {}", error.getMessage());
            sesion.close(CloseStatus.POLICY_VIOLATION.withReason(error.getMessage()));
            return;
        }
        suscribir(sesion, grupo);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession sesion, CloseStatus estado) {
        Object grupo = sesion.getAttributes().remove(ATRIBUTO_GRUPO);
        Object suscriptor = sesion.getAttributes().remove(ATRIBUTO_SUSCRIPTOR);
        if (grupo instanceof GroupId grupoId && suscriptor instanceof StatusSubscriber suscriptorEstado) {
            sesiones.retirar(grupoId, suscriptorEstado);
        }
    }

    private void suscribir(WebSocketSession sesion, GroupId grupo) {
        WebSocketSession segura = new ConcurrentWebSocketSessionDecorator(sesion, TIEMPO_MAXIMO_ENVIO_MS,
                BUFFER_MAXIMO_BYTES);
        StatusSubscriber suscriptor = new WebSocketStatusSubscriber(segura, objectMapper);
        sesion.getAttributes().put(ATRIBUTO_GRUPO, grupo);
        sesion.getAttributes().put(ATRIBUTO_SUSCRIPTOR, suscriptor);
        sesiones.suscribir(grupo, suscriptor);
        LOG.debug("Sesión {} suscrita al grupo {}", sesion.getId(), grupo.value());
    }

    private static String parametro(WebSocketSession sesion, String nombre) {
        if (sesion.getUri() == null) {
            return null;
        }
        return UriComponentsBuilder.fromUri(sesion.getUri()).build().getQueryParams().getFirst(nombre);
    }
}

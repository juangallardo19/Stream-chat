package com.streamchat.status_backend.adapter;

import com.streamchat.status_backend.domain.GroupSessions;
import com.streamchat.status_backend.domain.StatusUpdate;
import com.streamchat.status_backend.port.StatusBroadcaster;
import org.springframework.stereotype.Component;

@Component
class WebSocketStatusBroadcaster implements StatusBroadcaster {

    private final GroupSessions sesiones;

    WebSocketStatusBroadcaster(GroupSessions sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    public int difundir(StatusUpdate actualizacion) {
        return sesiones.difundir(actualizacion);
    }
}

package com.streamchat.chat_backend.adapter;

import com.streamchat.chat_backend.domain.ChatMessage;
import com.streamchat.chat_backend.domain.GroupSessions;
import com.streamchat.chat_backend.port.GroupBroadcaster;
import org.springframework.stereotype.Component;

@Component
class WebSocketGroupBroadcaster implements GroupBroadcaster {

    private final GroupSessions sesiones;

    WebSocketGroupBroadcaster(GroupSessions sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    public int difundir(ChatMessage mensaje) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}

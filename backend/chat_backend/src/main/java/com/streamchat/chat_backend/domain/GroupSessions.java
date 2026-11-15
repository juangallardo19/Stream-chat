package com.streamchat.chat_backend.domain;

import java.util.Set;

public final class GroupSessions {

    public void suscribir(GroupId grupo, ChatSubscriber suscriptor) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void retirar(GroupId grupo, ChatSubscriber suscriptor) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public int difundir(ChatMessage mensaje) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public Set<GroupId> gruposActivos() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}

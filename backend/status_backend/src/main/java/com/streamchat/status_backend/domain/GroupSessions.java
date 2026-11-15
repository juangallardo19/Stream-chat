package com.streamchat.status_backend.domain;

import java.util.Set;

public final class GroupSessions {

    public void suscribir(GroupId grupo, StatusSubscriber suscriptor) {
    }

    public void retirar(GroupId grupo, StatusSubscriber suscriptor) {
    }

    public int difundir(StatusUpdate actualizacion) {
        return 0;
    }

    public Set<GroupId> gruposActivos() {
        return Set.of();
    }
}

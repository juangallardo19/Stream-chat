package com.streamchat.typing_processor.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public final class ChatGroup {

    private ChatGroup() {
    }

    public static ChatGroup nuevo(GroupId groupId) {
        return new ChatGroup();
    }

    public Optional<StatusChange> registrarActividad(UserId user, Instant ahora) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public List<StatusChange> evaluarInactividad(Instant ahora, InactivityStrategy estrategia) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public int usuariosActivos() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}

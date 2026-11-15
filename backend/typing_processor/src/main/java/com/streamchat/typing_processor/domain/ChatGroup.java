package com.streamchat.typing_processor.domain;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ChatGroup {

    private final GroupId groupId;
    private final Map<UserId, UserTypingState> usuarios = new HashMap<>();

    private ChatGroup(GroupId groupId) {
        this.groupId = groupId;
    }

    public static ChatGroup nuevo(GroupId groupId) {
        Objects.requireNonNull(groupId, "El grupo es obligatorio");
        return new ChatGroup(groupId);
    }

    public synchronized Optional<StatusChange> registrarActividad(UserId user, Instant ahora) {
        Objects.requireNonNull(user, "El usuario es obligatorio");
        return usuarios.computeIfAbsent(user, nuevo -> UserTypingState.nuevo(nuevo, groupId))
                .registrarPulsacion(ahora);
    }

    public synchronized List<StatusChange> evaluarInactividad(Instant ahora, InactivityStrategy estrategia) {
        List<StatusChange> cambios = usuarios.values().stream()
                .map(estado -> estado.evaluarEstadoTyping(ahora, estrategia))
                .flatMap(Optional::stream)
                .toList();
        usuarios.values().removeIf(estado -> estado.status() == TypingStatus.STOPPED);
        return cambios;
    }

    public synchronized Optional<StatusChange> registrarEnvio(UserId user) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public synchronized int usuariosActivos() {
        return usuarios.size();
    }

    public GroupId groupId() {
        return groupId;
    }
}

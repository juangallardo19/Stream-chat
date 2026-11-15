package com.streamchat.typing_processor.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class UserTypingState {

    private final UserId user;
    private final GroupId group;
    private ActivityState estado;
    private Instant ultimaActividad;

    private UserTypingState(UserId user, GroupId group) {
        this.user = user;
        this.group = group;
        this.estado = new StoppedState();
    }

    public static UserTypingState nuevo(UserId user, GroupId group) {
        Objects.requireNonNull(user, "El usuario es obligatorio");
        Objects.requireNonNull(group, "El grupo es obligatorio");
        return new UserTypingState(user, group);
    }

    public Optional<StatusChange> registrarPulsacion(Instant ahora) {
        Objects.requireNonNull(ahora, "La hora de la pulsación es obligatoria");
        this.ultimaActividad = ahora;
        return transicionarA(estado.alTeclear());
    }

    public Optional<StatusChange> evaluarEstadoTyping(Instant ahora, InactivityStrategy estrategia) {
        Objects.requireNonNull(ahora, "La hora de evaluación es obligatoria");
        Objects.requireNonNull(estrategia, "La estrategia de inactividad es obligatoria");
        if (ultimaActividad == null || !estrategia.estaInactivo(ultimaActividad, ahora)) {
            return Optional.empty();
        }
        return transicionarA(estado.alExpirar());
    }

    public TypingStatus status() {
        return estado.status();
    }

    private Optional<StatusChange> transicionarA(ActivityState siguiente) {
        if (siguiente.status() == estado.status()) {
            return Optional.empty();
        }
        this.estado = siguiente;
        return Optional.of(new StatusChange(user, group, siguiente.status()));
    }
}

package com.streamchat.status_backend.mapper;

import com.streamchat.status_backend.domain.GroupId;
import com.streamchat.status_backend.domain.StatusUpdate;
import com.streamchat.status_backend.domain.TypingStatus;
import com.streamchat.status_backend.domain.UserId;
import com.streamchat.status_backend.dto.StatusUpdatePayload;

import java.util.Objects;

public final class StatusUpdateMapper {

    private StatusUpdateMapper() {
    }

    public static StatusUpdate aDominio(StatusUpdatePayload payload) {
        Objects.requireNonNull(payload, "El payload es obligatorio");
        return new StatusUpdate(new UserId(payload.user()), new GroupId(payload.group()),
                TypingStatus.valueOf(payload.status()));
    }

    public static StatusUpdatePayload aPayload(StatusUpdate actualizacion) {
        Objects.requireNonNull(actualizacion, "La actualización de estado es obligatoria");
        return new StatusUpdatePayload(actualizacion.user().value(), actualizacion.group().value(),
                actualizacion.status().name());
    }
}

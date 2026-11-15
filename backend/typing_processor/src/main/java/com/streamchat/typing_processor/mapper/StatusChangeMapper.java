package com.streamchat.typing_processor.mapper;

import com.streamchat.typing_processor.domain.StatusChange;
import com.streamchat.typing_processor.dto.StatusUpdatePayload;

import java.util.Objects;

public final class StatusChangeMapper {

    private StatusChangeMapper() {
    }

    public static StatusUpdatePayload aPayload(StatusChange cambio) {
        Objects.requireNonNull(cambio, "La transición de estado es obligatoria");
        return new StatusUpdatePayload(cambio.user().value(), cambio.group().value(), cambio.status().name());
    }
}

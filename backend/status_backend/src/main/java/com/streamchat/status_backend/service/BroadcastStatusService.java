package com.streamchat.status_backend.service;

import com.streamchat.status_backend.domain.StatusUpdate;
import com.streamchat.status_backend.port.StatusBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class BroadcastStatusService {

    private static final Logger LOG = LoggerFactory.getLogger(BroadcastStatusService.class);

    private final StatusBroadcaster broadcaster;

    public BroadcastStatusService(StatusBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    public void difundir(StatusUpdate actualizacion) {
        Objects.requireNonNull(actualizacion, "La actualización de estado es obligatoria");
        int entregas = broadcaster.difundir(actualizacion);
        LOG.debug("{} {} entregado a {} sesiones de {}", actualizacion.user().value(), actualizacion.status(),
                entregas, actualizacion.group().value());
    }
}

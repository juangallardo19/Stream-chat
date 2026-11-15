package com.streamchat.status_backend.service;

import com.streamchat.status_backend.domain.StatusUpdate;
import com.streamchat.status_backend.port.StatusBroadcaster;
import org.springframework.stereotype.Service;

@Service
public class BroadcastStatusService {

    private final StatusBroadcaster broadcaster;

    public BroadcastStatusService(StatusBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    public void difundir(StatusUpdate actualizacion) {
    }
}

package com.streamchat.status_backend.port;

import com.streamchat.status_backend.domain.StatusUpdate;

public interface StatusBroadcaster {

    int difundir(StatusUpdate actualizacion);
}

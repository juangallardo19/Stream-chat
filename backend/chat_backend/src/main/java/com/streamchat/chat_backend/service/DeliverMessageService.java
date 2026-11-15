package com.streamchat.chat_backend.service;

import com.streamchat.chat_backend.domain.ChatMessage;
import com.streamchat.chat_backend.port.GroupBroadcaster;
import org.springframework.stereotype.Service;

@Service
public class DeliverMessageService {

    private final GroupBroadcaster broadcaster;

    public DeliverMessageService(GroupBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    public void entregar(ChatMessage mensaje) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}

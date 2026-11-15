package com.streamchat.typing_api.service;

import com.streamchat.typing_api.domain.GroupCatalog;
import com.streamchat.typing_api.dto.RegisterTypingRequest;
import com.streamchat.typing_api.port.TypingEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class RegisterTypingService {

    private final TypingEventPublisher publisher;
    private final GroupCatalog catalogo;
    private final Clock clock;

    public RegisterTypingService(TypingEventPublisher publisher, GroupCatalog catalogo, Clock clock) {
        this.publisher = publisher;
        this.catalogo = catalogo;
        this.clock = clock;
    }

    public void registrar(RegisterTypingRequest solicitud) {
    }
}

package com.streamchat.typing_processor.service;

import com.streamchat.typing_processor.domain.ChatGroup;
import com.streamchat.typing_processor.domain.GroupId;
import com.streamchat.typing_processor.domain.InactivityStrategy;
import com.streamchat.typing_processor.domain.MessageSent;
import com.streamchat.typing_processor.domain.StatusChange;
import com.streamchat.typing_processor.domain.TypingEvent;
import com.streamchat.typing_processor.domain.TypingStatusNotifier;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TypingProcessorService {

    private final TypingStatusNotifier notifier;
    private final InactivityStrategy estrategia;
    private final Clock clock;
    private final Map<GroupId, ChatGroup> grupos = new ConcurrentHashMap<>();

    public TypingProcessorService(TypingStatusNotifier notifier, InactivityStrategy estrategia, Clock clock) {
        this.notifier = notifier;
        this.estrategia = estrategia;
        this.clock = clock;
    }

    public void procesar(TypingEvent evento) {
        Objects.requireNonNull(evento, "El evento de tecleo es obligatorio");
        grupos.computeIfAbsent(evento.group(), ChatGroup::nuevo)
                .registrarActividad(evento.user(), evento.occurredAt())
                .ifPresent(notifier::notificar);
    }

    public void registrarEnvio(MessageSent envio) {
    }

    public void detectarInactividad() {
        Instant ahora = Instant.now(clock);
        List<StatusChange> cambios = grupos.values().stream()
                .flatMap(grupo -> grupo.evaluarInactividad(ahora, estrategia).stream())
                .toList();
        cambios.forEach(notifier::notificar);
    }
}

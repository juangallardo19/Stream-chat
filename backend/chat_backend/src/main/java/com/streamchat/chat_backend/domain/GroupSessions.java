package com.streamchat.chat_backend.domain;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class GroupSessions {

    private final Map<GroupId, Set<ChatSubscriber>> suscriptoresPorGrupo = new HashMap<>();

    public void suscribir(GroupId grupo, ChatSubscriber suscriptor) {
        Objects.requireNonNull(grupo, "El grupo es obligatorio");
        Objects.requireNonNull(suscriptor, "La sesión suscriptora es obligatoria");
        synchronized (suscriptoresPorGrupo) {
            suscriptoresPorGrupo.computeIfAbsent(grupo, clave -> new LinkedHashSet<>()).add(suscriptor);
        }
    }

    public void retirar(GroupId grupo, ChatSubscriber suscriptor) {
        Objects.requireNonNull(grupo, "El grupo es obligatorio");
        Objects.requireNonNull(suscriptor, "La sesión suscriptora es obligatoria");
        synchronized (suscriptoresPorGrupo) {
            Set<ChatSubscriber> suscriptores = suscriptoresPorGrupo.get(grupo);
            if (suscriptores == null) {
                return;
            }
            suscriptores.remove(suscriptor);
            if (suscriptores.isEmpty()) {
                suscriptoresPorGrupo.remove(grupo);
            }
        }
    }

    public int difundir(ChatMessage mensaje) {
        Objects.requireNonNull(mensaje, "El mensaje es obligatorio");
        List<ChatSubscriber> destinatarios = suscriptoresDe(mensaje.group());
        destinatarios.forEach(suscriptor -> suscriptor.recibir(mensaje));
        return destinatarios.size();
    }

    public Set<GroupId> gruposActivos() {
        synchronized (suscriptoresPorGrupo) {
            return Set.copyOf(suscriptoresPorGrupo.keySet());
        }
    }

    private List<ChatSubscriber> suscriptoresDe(GroupId grupo) {
        synchronized (suscriptoresPorGrupo) {
            return List.copyOf(suscriptoresPorGrupo.getOrDefault(grupo, Set.of()));
        }
    }
}

package com.streamchat.status_backend.domain;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class GroupSessions {

    private final Map<GroupId, Set<StatusSubscriber>> suscriptoresPorGrupo = new HashMap<>();

    public void suscribir(GroupId grupo, StatusSubscriber suscriptor) {
        Objects.requireNonNull(grupo, "El grupo es obligatorio");
        Objects.requireNonNull(suscriptor, "La sesión suscriptora es obligatoria");
        synchronized (suscriptoresPorGrupo) {
            suscriptoresPorGrupo.computeIfAbsent(grupo, clave -> new LinkedHashSet<>()).add(suscriptor);
        }
    }

    public void retirar(GroupId grupo, StatusSubscriber suscriptor) {
        Objects.requireNonNull(grupo, "El grupo es obligatorio");
        Objects.requireNonNull(suscriptor, "La sesión suscriptora es obligatoria");
        synchronized (suscriptoresPorGrupo) {
            Set<StatusSubscriber> suscriptores = suscriptoresPorGrupo.get(grupo);
            if (suscriptores == null) {
                return;
            }
            suscriptores.remove(suscriptor);
            if (suscriptores.isEmpty()) {
                suscriptoresPorGrupo.remove(grupo);
            }
        }
    }

    public int difundir(StatusUpdate actualizacion) {
        Objects.requireNonNull(actualizacion, "La actualización de estado es obligatoria");
        List<StatusSubscriber> destinatarios = suscriptoresDe(actualizacion.group());
        destinatarios.forEach(suscriptor -> suscriptor.recibir(actualizacion));
        return destinatarios.size();
    }

    public Set<GroupId> gruposActivos() {
        synchronized (suscriptoresPorGrupo) {
            return Set.copyOf(suscriptoresPorGrupo.keySet());
        }
    }

    private List<StatusSubscriber> suscriptoresDe(GroupId grupo) {
        synchronized (suscriptoresPorGrupo) {
            return List.copyOf(suscriptoresPorGrupo.getOrDefault(grupo, Set.of()));
        }
    }
}

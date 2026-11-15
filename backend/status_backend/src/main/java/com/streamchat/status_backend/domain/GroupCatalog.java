package com.streamchat.status_backend.domain;

import com.streamchat.status_backend.exception.GrupoNoEncontradoException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class GroupCatalog {

    private final Map<String, GroupId> gruposPorClave;
    private final List<GroupId> gruposEnOrden;

    private GroupCatalog(Map<String, GroupId> gruposPorClave) {
        this.gruposPorClave = Map.copyOf(gruposPorClave);
        this.gruposEnOrden = List.copyOf(gruposPorClave.values());
    }

    public static GroupCatalog desde(List<String> nombres) {
        Objects.requireNonNull(nombres, "La lista de grupos es obligatoria");
        if (nombres.isEmpty()) {
            throw new IllegalArgumentException("El catálogo de grupos no puede estar vacío");
        }
        Map<String, GroupId> grupos = new LinkedHashMap<>();
        nombres.forEach(nombre -> registrar(grupos, new GroupId(nombre)));
        return new GroupCatalog(grupos);
    }

    public GroupId resolver(String nombre) {
        GroupId solicitado = new GroupId(nombre);
        return Optional.ofNullable(gruposPorClave.get(claveDe(solicitado)))
                .orElseThrow(() -> new GrupoNoEncontradoException("El grupo " + solicitado.value() + " no existe"));
    }

    public List<GroupId> grupos() {
        return gruposEnOrden;
    }

    private static void registrar(Map<String, GroupId> grupos, GroupId grupo) {
        GroupId anterior = grupos.putIfAbsent(claveDe(grupo), grupo);
        if (anterior != null) {
            throw new IllegalArgumentException("El catálogo de grupos tiene nombres repetidos: " + grupo.value());
        }
    }

    private static String claveDe(GroupId grupo) {
        return grupo.value().toLowerCase(Locale.ROOT);
    }
}

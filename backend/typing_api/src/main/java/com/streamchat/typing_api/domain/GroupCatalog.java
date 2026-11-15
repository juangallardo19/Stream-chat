package com.streamchat.typing_api.domain;

import com.streamchat.typing_api.exception.GrupoNoEncontradoException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class GroupCatalog {

    private final Map<String, GroupId> gruposPorClave;

    private GroupCatalog(Map<String, GroupId> gruposPorClave) {
        this.gruposPorClave = Map.copyOf(gruposPorClave);
    }

    public static GroupCatalog desde(List<String> nombres) {
        Objects.requireNonNull(nombres, "La lista de grupos es obligatoria");
        if (nombres.isEmpty()) {
            throw new IllegalArgumentException("El catálogo de grupos no puede estar vacío");
        }
        Map<String, GroupId> grupos = new HashMap<>();
        nombres.forEach(nombre -> registrar(grupos, new GroupId(nombre)));
        return new GroupCatalog(grupos);
    }

    public GroupId resolver(String nombre) {
        GroupId solicitado = new GroupId(nombre);
        return Optional.ofNullable(gruposPorClave.get(claveDe(solicitado)))
                .orElseThrow(() -> new GrupoNoEncontradoException("El grupo " + solicitado.value() + " no existe"));
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

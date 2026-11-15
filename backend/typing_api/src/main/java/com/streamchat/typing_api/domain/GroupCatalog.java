package com.streamchat.typing_api.domain;

import java.util.List;

public final class GroupCatalog {

    private GroupCatalog() {
    }

    public static GroupCatalog desde(List<String> nombres) {
        return new GroupCatalog();
    }

    public GroupId resolver(String nombre) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}

package com.streamchat.analytics_service.adapter;

import com.streamchat.analytics_service.domain.ChatGroup;
import com.streamchat.analytics_service.port.MetricsReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
class ConsoleMetricsReporter implements MetricsReporter {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleMetricsReporter.class);

    @Override
    public void reportar(ChatGroup grupo) {
        Objects.requireNonNull(grupo, "El grupo es obligatorio");
        LOG.info("Mensajes totales en {}: {}", grupo.getGroupId(), grupo.getTotalMessages());
    }
}

package com.streamchat.analytics_service.adapter;

import com.streamchat.analytics_service.domain.ChatGroup;
import com.streamchat.analytics_service.port.MetricsReporter;
import org.springframework.stereotype.Component;

@Component
class ConsoleMetricsReporter implements MetricsReporter {

    @Override
    public void reportar(ChatGroup grupo) {
    }
}

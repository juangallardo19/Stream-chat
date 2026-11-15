package com.streamchat.analytics_service.port;

import com.streamchat.analytics_service.domain.ChatGroup;

public interface MetricsReporter {

    void reportar(ChatGroup grupo);
}

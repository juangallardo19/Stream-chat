package com.streamchat.chat_backend.config;

import com.streamchat.chat_backend.domain.GroupCatalog;
import com.streamchat.chat_backend.domain.GroupSessions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
class DomainConfig {

    @Bean
    GroupCatalog groupCatalog(@Value("${app.chat.groups}") String grupos) {
        List<String> nombres = Arrays.stream(grupos.split(","))
                .map(String::strip)
                .toList();
        return GroupCatalog.desde(nombres);
    }

    @Bean
    GroupSessions groupSessions() {
        return new GroupSessions();
    }
}

package com.streamchat.typing_api.config;

import com.streamchat.typing_api.domain.GroupCatalog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DomainConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    GroupCatalog groupCatalog(@Value("${app.chat.groups}") String grupos) {
        List<String> nombres = Arrays.stream(grupos.split(","))
                .map(String::strip)
                .toList();
        return GroupCatalog.desde(nombres);
    }
}

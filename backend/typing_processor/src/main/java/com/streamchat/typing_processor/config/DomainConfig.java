package com.streamchat.typing_processor.config;

import com.streamchat.typing_processor.domain.InactivityStrategy;
import com.streamchat.typing_processor.domain.InactivityStrategyFactory;
import com.streamchat.typing_processor.domain.TypingStatusNotifier;
import com.streamchat.typing_processor.domain.TypingStatusObserver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

@Configuration
@EnableScheduling
class DomainConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    InactivityStrategy inactivityStrategy(@Value("${app.typing.strategy}") String nombre,
            @Value("${app.typing.window-ms}") long ventanaMs) {
        return InactivityStrategyFactory.crear(nombre, Duration.ofMillis(ventanaMs));
    }

    @Bean
    TypingStatusNotifier typingStatusNotifier(List<TypingStatusObserver> observadores) {
        return new TypingStatusNotifier(observadores);
    }
}

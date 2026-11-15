package com.streamchat.analytics_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class ClockConfig {

    @Bean
    Clock reloj() {
        return Clock.systemUTC();
    }
}

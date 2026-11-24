package com.streamchat.analytics_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
class KafkaConfig {

    private static final long ESPERA_ENTRE_REINTENTOS_MS = 5000L;

    @Bean
    CommonErrorHandler reintentarSinPerder() {
        return new DefaultErrorHandler(new FixedBackOff(ESPERA_ENTRE_REINTENTOS_MS, FixedBackOff.UNLIMITED_ATTEMPTS));
    }
}

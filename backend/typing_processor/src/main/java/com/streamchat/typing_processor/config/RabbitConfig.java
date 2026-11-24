package com.streamchat.typing_processor.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RabbitConfig {

    static final String STATUS_EXCHANGE = "status_updates";

    @Bean
    FanoutExchange statusExchange() {
        return new FanoutExchange(STATUS_EXCHANGE, true, false);
    }
}

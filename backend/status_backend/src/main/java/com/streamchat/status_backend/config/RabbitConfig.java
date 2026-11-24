package com.streamchat.status_backend.config;

import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RabbitConfig {

    static final String STATUS_EXCHANGE = "status_updates";

    @Bean
    FanoutExchange statusExchange() {
        return new FanoutExchange(STATUS_EXCHANGE, true, false);
    }

    @Bean
    Queue statusQueue() {
        return new AnonymousQueue();
    }

    @Bean
    Binding enlaceEstado(Queue statusQueue, FanoutExchange statusExchange) {
        return BindingBuilder.bind(statusQueue).to(statusExchange);
    }
}

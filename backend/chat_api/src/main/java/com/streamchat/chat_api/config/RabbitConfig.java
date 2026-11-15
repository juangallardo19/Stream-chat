package com.streamchat.chat_api.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String CHAT_EXCHANGE = "chat_messages";

    @Bean
    TopicExchange chatMessagesExchange() {
        return new TopicExchange(CHAT_EXCHANGE, true, false);
    }
}

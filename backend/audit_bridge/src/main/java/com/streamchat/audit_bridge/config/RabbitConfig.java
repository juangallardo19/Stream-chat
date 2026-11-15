package com.streamchat.audit_bridge.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RabbitConfig {

    static final String CHAT_EXCHANGE = "chat_messages";
    static final String COLA_AUDITORIA = "audit_bridge.messages";
    private static final String TODOS_LOS_GRUPOS = "chat.#";

    @Bean
    TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE, true, false);
    }

    @Bean
    Queue colaAuditoria() {
        return QueueBuilder.durable(COLA_AUDITORIA).singleActiveConsumer().build();
    }

    @Bean
    Binding enlaceAuditoria(Queue colaAuditoria, TopicExchange chatExchange) {
        return BindingBuilder.bind(colaAuditoria).to(chatExchange).with(TODOS_LOS_GRUPOS);
    }
}

package com.streamchat.chat_backend.config;

import com.streamchat.chat_backend.domain.GroupCatalog;
import com.streamchat.chat_backend.domain.GroupId;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Configuration
class RabbitConfig {

    static final String CHAT_EXCHANGE = "chat_messages";
    private static final String PREFIJO_COLA = "chat_backend.";
    private static final String PREFIJO_ROUTING_KEY = "chat.";

    @Bean
    TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE, true, false);
    }

    @Bean
    Declarables colasPorGrupo(GroupCatalog catalogo, TopicExchange chatExchange) {
        List<Declarable> declarables = new ArrayList<>();
        catalogo.grupos().forEach(grupo -> {
            Queue cola = QueueBuilder.durable(nombreDeCola(grupo)).build();
            Binding enlace = BindingBuilder.bind(cola).to(chatExchange).with(PREFIJO_ROUTING_KEY + clave(grupo));
            declarables.add(cola);
            declarables.add(enlace);
        });
        return new Declarables(declarables);
    }

    @Bean
    String[] chatQueueNames(GroupCatalog catalogo) {
        return catalogo.grupos().stream()
                .map(RabbitConfig::nombreDeCola)
                .toArray(String[]::new);
    }

    private static String nombreDeCola(GroupId grupo) {
        return PREFIJO_COLA + clave(grupo);
    }

    private static String clave(GroupId grupo) {
        return grupo.value().toLowerCase(Locale.ROOT);
    }
}

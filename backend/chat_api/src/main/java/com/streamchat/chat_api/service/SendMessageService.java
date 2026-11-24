package com.streamchat.chat_api.service;

import com.streamchat.chat_api.domain.ChatMessage;
import com.streamchat.chat_api.domain.GroupCatalog;
import com.streamchat.chat_api.domain.GroupId;
import com.streamchat.chat_api.domain.MessageText;
import com.streamchat.chat_api.domain.UserId;
import com.streamchat.chat_api.dto.SendMessageRequest;
import com.streamchat.chat_api.dto.SendMessageResponse;
import com.streamchat.chat_api.port.ChatMessagePublisher;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Objects;

@Service
public class SendMessageService {

    private final ChatMessagePublisher publisher;
    private final GroupCatalog catalogo;
    private final Clock clock;

    public SendMessageService(ChatMessagePublisher publisher, GroupCatalog catalogo, Clock clock) {
        this.publisher = publisher;
        this.catalogo = catalogo;
        this.clock = clock;
    }

    public SendMessageResponse enviar(SendMessageRequest solicitud) {
        Objects.requireNonNull(solicitud, "La solicitud es obligatoria");
        GroupId grupo = catalogo.resolver(solicitud.group());
        ChatMessage mensaje = ChatMessage.registrarMensaje(
                new UserId(solicitud.user()), grupo, new MessageText(solicitud.text()), clock);
        publisher.publicar(mensaje);
        return new SendMessageResponse(mensaje.messageId(), mensaje.sentAt());
    }
}

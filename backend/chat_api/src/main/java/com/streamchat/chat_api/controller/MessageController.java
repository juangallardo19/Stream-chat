package com.streamchat.chat_api.controller;

import com.streamchat.chat_api.dto.SendMessageRequest;
import com.streamchat.chat_api.dto.SendMessageResponse;
import com.streamchat.chat_api.service.SendMessageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    private final SendMessageService sendMessageService;

    public MessageController(SendMessageService sendMessageService) {
        this.sendMessageService = sendMessageService;
    }

    @PostMapping("/message")
    public ResponseEntity<SendMessageResponse> enviar(@Valid @RequestBody SendMessageRequest solicitud) {
        return ResponseEntity.accepted().body(sendMessageService.enviar(solicitud));
    }
}

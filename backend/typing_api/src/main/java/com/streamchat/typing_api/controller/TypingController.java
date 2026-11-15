package com.streamchat.typing_api.controller;

import com.streamchat.typing_api.dto.RegisterTypingRequest;
import com.streamchat.typing_api.service.RegisterTypingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TypingController {

    private final RegisterTypingService registerTypingService;

    public TypingController(RegisterTypingService registerTypingService) {
        this.registerTypingService = registerTypingService;
    }

    @PostMapping("/typing")
    public ResponseEntity<Void> registrar(@Valid @RequestBody RegisterTypingRequest solicitud) {
        registerTypingService.registrar(solicitud);
        return ResponseEntity.accepted().build();
    }
}

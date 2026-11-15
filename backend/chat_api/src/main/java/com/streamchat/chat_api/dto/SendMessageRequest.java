package com.streamchat.chat_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 40, message = "El usuario admite como máximo 40 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9_-]*$", message = "El usuario solo admite letras, números, guion y guion bajo")
        String user,

        @NotBlank(message = "El grupo es obligatorio")
        @Size(max = 40, message = "El grupo admite como máximo 40 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9_-]*$", message = "El grupo solo admite letras, números, guion y guion bajo")
        String group,

        @NotBlank(message = "El texto del mensaje es obligatorio")
        @Size(max = 1000, message = "El texto del mensaje admite como máximo 1000 caracteres")
        String text) {
}

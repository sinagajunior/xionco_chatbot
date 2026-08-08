package com.xionco.chatbot.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "Pesan tidak boleh kosong")
        String message,
        String sessionId
) {
}

package com.xionco.chatbot.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ChatMessage(
        String role,
        String content,
        LocalDateTime timestamp
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static ChatMessage ofUser(String content) {
        return new ChatMessage("user", content, LocalDateTime.now());
    }

    public static ChatMessage ofAssistant(String content) {
        return new ChatMessage("assistant", content, LocalDateTime.now());
    }

    public boolean isUser() {
        return "user".equals(role);
    }

    public boolean isAssistant() {
        return "assistant".equals(role);
    }

    public String formattedTimestamp() {
        return timestamp != null ? timestamp.format(FORMATTER) : "";
    }
}

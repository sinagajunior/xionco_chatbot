package com.xionco.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xionco.chatbot.dto.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {
    private static final String DEFAULT_SESSION_ID = "default";
    private static final String SYSTEM_PROMPT = """
            Anda adalah Xionco, asisten AI cerdas yang ramah dan membantu.
            Respons Anda selalu dalam Bahasa Indonesia yang jelas dan mudah dipahami.
            Berikan jawaban yang singkat, relevan, dan bermanfaat.
            Jika Anda tidak tahu jawaban, katakan dengan jujur dan tawarkan bantuan lain.
            """;

    private final RestTemplate restTemplate;
    private final String ollamaBaseUrl;
    private final ConcurrentHashMap<String, List<ChatMessage>> conversationHistory;
    private final ObjectMapper objectMapper;

    public ChatService(@Value("${spring.ai.ollama.base-url:http://localhost:11434}") String ollamaBaseUrl,
                      RestTemplate restTemplate,
                      ObjectMapper objectMapper) {
        this.ollamaBaseUrl = ollamaBaseUrl;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.conversationHistory = new ConcurrentHashMap<>();
    }

    public ChatMessage chat(String userInput, String sessionId) {
        String session = sessionId != null && !sessionId.isBlank() ? sessionId : DEFAULT_SESSION_ID;

        // Initialize session with system message if new
        conversationHistory.computeIfAbsent(session, k -> new ArrayList<>());
        List<ChatMessage> messages = conversationHistory.get(session);

        // Add user message
        ChatMessage userMessage = new ChatMessage("user", userInput, LocalDateTime.now());
        messages.add(userMessage);

        try {
            // Build messages for Ollama
            List<Map<String, String>> ollamaMessages = new ArrayList<>();
            ollamaMessages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

            for (ChatMessage msg : messages) {
                ollamaMessages.add(Map.of("role", msg.role(), "content", msg.content()));
            }

            // Call Ollama API
            Map<String, Object> request = Map.of(
                    "model", "tinyllama:latest",
                    "messages", ollamaMessages,
                    "stream", false
            );

            String response = restTemplate.postForObject(
                    ollamaBaseUrl + "/api/chat",
                    request,
                    String.class
            );

            // Parse response
            var responseMap = objectMapper.readValue(response, Map.class);
            var assistantMessage = (Map<String, Object>) responseMap.get("message");
            String content = (String) assistantMessage.get("content");

            // Add assistant response to history
            ChatMessage chatMessage = new ChatMessage("assistant", content, LocalDateTime.now());
            messages.add(chatMessage);

            return chatMessage;
        } catch (Exception e) {
            throw new RuntimeException("Kesalahan saat berkomunikasi dengan Ollama: " + e.getMessage(), e);
        }
    }

    public void clearHistory(String sessionId) {
        String session = sessionId != null && !sessionId.isBlank() ? sessionId : DEFAULT_SESSION_ID;
        conversationHistory.remove(session);
    }

    public List<ChatMessage> getHistory(String sessionId) {
        String session = sessionId != null && !sessionId.isBlank() ? sessionId : DEFAULT_SESSION_ID;
        return new ArrayList<>(conversationHistory.getOrDefault(session, new ArrayList<>()));
    }
}

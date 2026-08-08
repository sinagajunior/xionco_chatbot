package com.xionco.chatbot.service;

import com.xionco.chatbot.dto.ChatMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    private final ChatModel chatModel;
    private final ConcurrentHashMap<String, List<Message>> conversationHistory;

    public ChatService(ChatModel chatModel) {
        this.chatModel = chatModel;
        this.conversationHistory = new ConcurrentHashMap<>();
    }

    public ChatMessage chat(String userInput, String sessionId) {
        String session = sessionId != null && !sessionId.isBlank() ? sessionId : DEFAULT_SESSION_ID;

        // Initialize session with system message if new
        conversationHistory.computeIfAbsent(session, k -> {
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(SYSTEM_PROMPT));
            return messages;
        });

        // Add user message
        List<Message> messages = conversationHistory.get(session);
        messages.add(new UserMessage(userInput));

        try {
            // Create prompt and call chat model
            Prompt prompt = new Prompt(messages);
            var chatResponse = chatModel.call(prompt);
            String response = chatResponse.getResult().getOutput().getText();

            // Add assistant response to history
            Message assistantMessage = new org.springframework.ai.chat.messages.AssistantMessage(response);
            messages.add(assistantMessage);

            return ChatMessage.ofAssistant(response);
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
        List<Message> messages = conversationHistory.getOrDefault(session, new ArrayList<>());

        // Filter out system messages and convert to ChatMessage records
        return messages.stream()
                .filter(msg -> !(msg instanceof SystemMessage))
                .map(msg -> {
                    String role = msg instanceof org.springframework.ai.chat.messages.AssistantMessage ? "assistant" : "user";
                    return new ChatMessage(role, msg.getText(), null);
                })
                .toList();
    }
}

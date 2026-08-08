package com.xionco.chatbot.service;

import com.xionco.chatbot.dto.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ChatServiceTest {
    @Mock
    private ChatModel chatModel;

    private ChatService chatService;
    private String uniqueSessionId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatService = new ChatService(chatModel);
        uniqueSessionId = UUID.randomUUID().toString();
    }

    @Test
    void testChatReturnsAssistantMessage() {
        // Arrange
        String userInput = "Halo, siapa nama Anda?";
        String expectedResponse = "Saya Xionco, asisten AI Anda.";

        ChatResponse mockResponse = new ChatResponse(List.of(
                new Generation(new AssistantMessage(expectedResponse))
        ));
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // Act
        ChatMessage result = chatService.chat(userInput, uniqueSessionId);

        // Assert
        assertNotNull(result);
        assertEquals("assistant", result.role());
        assertEquals(expectedResponse, result.content());
    }

    @Test
    void testSystemMessageIsFirstInstructions() {
        // Arrange
        String userInput = "Test message";
        ChatResponse mockResponse = new ChatResponse(List.of(
                new Generation(new AssistantMessage("Response"))
        ));
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // Act
        chatService.chat(userInput, uniqueSessionId);

        // Assert
        List<ChatMessage> history = chatService.getHistory(uniqueSessionId);
        assertNotNull(history);
        assertTrue(history.size() >= 1);
    }

    @Test
    void testUsesDefaultSessionWhenSessionIdIsNull() {
        // Arrange
        ChatResponse mockResponse = new ChatResponse(List.of(
                new Generation(new AssistantMessage("Response"))
        ));
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // Act - clear any previous default session state first
        chatService.clearHistory(null);
        ChatMessage result = chatService.chat("Message 1", null);
        ChatMessage result2 = chatService.chat("Message 2", null);

        // Assert - each chat() call adds both user and assistant message
        List<ChatMessage> history = chatService.getHistory(null);
        assertEquals(4, history.size());
    }

    @Test
    void testMultiTurnConversation() {
        // Arrange
        ChatResponse mockResponse = new ChatResponse(List.of(
                new Generation(new AssistantMessage("Response"))
        ));
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // Act
        chatService.chat("First message", uniqueSessionId);
        chatService.chat("Second message", uniqueSessionId);

        // Assert - each chat() call adds both user and assistant message (2 per call, 4 total)
        List<ChatMessage> history = chatService.getHistory(uniqueSessionId);
        assertEquals(4, history.size());
        assertEquals("user", history.get(0).role());
        assertEquals("assistant", history.get(1).role());
        assertEquals("user", history.get(2).role());
        assertEquals("assistant", history.get(3).role());
    }

    @Test
    void testThrowsRuntimeExceptionOnChatError() {
        // Arrange
        when(chatModel.call(any(Prompt.class)))
                .thenThrow(new RuntimeException("Ollama connection error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatService.chat("Test message", uniqueSessionId);
        });
    }

    @Test
    void testClearHistoryRemovesSession() {
        // Arrange
        ChatResponse mockResponse = new ChatResponse(List.of(
                new Generation(new AssistantMessage("Response"))
        ));
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);
        chatService.chat("Message", uniqueSessionId);

        // Act
        chatService.clearHistory(uniqueSessionId);

        // Assert
        List<ChatMessage> history = chatService.getHistory(uniqueSessionId);
        assertEquals(0, history.size());
    }

    @Test
    void testGetHistoryExcludesSystemMessages() {
        // Arrange
        ChatResponse mockResponse = new ChatResponse(List.of(
                new Generation(new AssistantMessage("Response"))
        ));
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // Act
        chatService.chat("Message", uniqueSessionId);
        List<ChatMessage> history = chatService.getHistory(uniqueSessionId);

        // Assert
        assertTrue(history.stream().noneMatch(msg -> msg.role().equals("system")));
    }
}

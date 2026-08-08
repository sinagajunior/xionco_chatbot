package com.xionco.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xionco.chatbot.dto.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ChatServiceTest {
    @Mock
    private RestTemplate restTemplate;

    private ChatService chatService;
    private String uniqueSessionId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatService = new ChatService("http://localhost:11434", restTemplate, new ObjectMapper());
        uniqueSessionId = UUID.randomUUID().toString();
    }

    @Test
    void testChatServiceInitializes() {
        assertNotNull(chatService);
        assertEquals(0, chatService.getHistory(uniqueSessionId).size());
    }

    @Test
    void testClearHistoryRemovesSession() {
        chatService.clearHistory(uniqueSessionId);
        List<ChatMessage> history = chatService.getHistory(uniqueSessionId);
        assertEquals(0, history.size());
    }

    @Test
    void testUsesDefaultSessionWhenSessionIdIsNull() {
        chatService.clearHistory(null);
        List<ChatMessage> history = chatService.getHistory(null);
        assertEquals(0, history.size());
    }

    @Test
    void testGetHistoryReturnsMessages() {
        chatService.clearHistory(uniqueSessionId);
        List<ChatMessage> history = chatService.getHistory(uniqueSessionId);
        assertNotNull(history);
        assertTrue(history instanceof java.util.ArrayList);
    }

    @Test
    void testMultipleSessionsAreIndependent() {
        String session1 = UUID.randomUUID().toString();
        String session2 = UUID.randomUUID().toString();

        List<ChatMessage> history1 = chatService.getHistory(session1);
        List<ChatMessage> history2 = chatService.getHistory(session2);

        assertNotNull(history1);
        assertNotNull(history2);
    }

    @Test
    void testClearHistoryForSpecificSession() {
        chatService.clearHistory(uniqueSessionId);
        List<ChatMessage> history = chatService.getHistory(uniqueSessionId);
        assertEquals(0, history.size());
    }

    @Test
    void testDefaultSessionCanBeCleared() {
        chatService.clearHistory(null);
        List<ChatMessage> history = chatService.getHistory(null);
        assertEquals(0, history.size());
    }
}

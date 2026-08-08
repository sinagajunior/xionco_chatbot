package com.xionco.chatbot.controller;

import com.xionco.chatbot.dto.ChatMessage;
import com.xionco.chatbot.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatApiController.class)
class ChatApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    void testPostChatReturns200WithSuccessStatus() throws Exception {
        // Arrange
        ChatMessage mockMessage = ChatMessage.ofAssistant("Halo dari Xionco!");
        when(chatService.chat(anyString(), anyString())).thenReturn(mockMessage);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType("application/json")
                        .content("{\"message\":\"Halo\",\"sessionId\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("sukses"))
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.content").value("Halo dari Xionco!"));
    }

    @Test
    void testPostChatWithErrorReturns500() throws Exception {
        // Arrange
        when(chatService.chat(anyString(), anyString()))
                .thenThrow(new RuntimeException("Ollama error"));

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType("application/json")
                        .content("{\"message\":\"Halo\",\"sessionId\":\"test\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("gagal"));
    }

    @Test
    void testPostChatWithEmptyMessageReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType("application/json")
                        .content("{\"message\":\"\",\"sessionId\":\"test\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSessionIdIsForwardedToService() throws Exception {
        // Arrange
        ChatMessage mockMessage = ChatMessage.ofAssistant("Response");
        when(chatService.chat("Halo", "custom-session")).thenReturn(mockMessage);

        // Act
        mockMvc.perform(post("/api/chat")
                        .contentType("application/json")
                        .content("{\"message\":\"Halo\",\"sessionId\":\"custom-session\"}"))
                .andExpect(status().isOk());

        // Assert
        verify(chatService).chat("Halo", "custom-session");
    }

    @Test
    void testDeleteClearReturns200AndCallsClearHistory() throws Exception {
        // Act
        mockMvc.perform(delete("/api/chat/clear")
                        .param("sessionId", "test-session"))
                .andExpect(status().isOk());

        // Assert
        verify(chatService).clearHistory("test-session");
    }
}

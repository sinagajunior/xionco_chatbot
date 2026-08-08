package com.xionco.chatbot.controller;

import com.xionco.chatbot.dto.ChatMessage;
import com.xionco.chatbot.dto.ChatRequest;
import com.xionco.chatbot.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatApiController {
    private final ChatService chatService;

    public ChatApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@Valid @RequestBody ChatRequest request) {
        try {
            ChatMessage response = chatService.chat(request.message(), request.sessionId());

            Map<String, String> result = new HashMap<>();
            result.put("status", "sukses");
            result.put("role", response.role());
            result.put("content", response.content());
            result.put("timestamp", response.formattedTimestamp());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "gagal");
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clear(@RequestParam(required = false) String sessionId) {
        chatService.clearHistory(sessionId);
        return ResponseEntity.ok().build();
    }
}

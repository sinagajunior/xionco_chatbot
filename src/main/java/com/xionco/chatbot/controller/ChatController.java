package com.xionco.chatbot.controller;

import com.xionco.chatbot.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class ChatController {
    private final ChatService chatService;

    @Value("${app.name}")
    private String appName;

    @Value("${app.description}")
    private String appDescription;

    @Value("${app.greeting}")
    private String greeting;

    @Value("${app.placeholder}")
    private String placeholder;

    @Value("${app.send-button-text}")
    private String sendButtonText;

    @Value("${app.loading-text}")
    private String loadingText;

    @Value("${app.error-text}")
    private String errorText;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public String index(Model model) {
        return chat(model);
    }

    @GetMapping("chat")
    public String chat(Model model) {
        model.addAttribute("appName", appName);
        model.addAttribute("appDescription", appDescription);
        model.addAttribute("greeting", greeting);
        model.addAttribute("placeholder", placeholder);
        model.addAttribute("sendButtonText", sendButtonText);
        model.addAttribute("loadingText", loadingText);
        model.addAttribute("errorText", errorText);
        model.addAttribute("history", chatService.getHistory("default"));
        return "chat";
    }

    @GetMapping("chat/clear")
    public String clearChat() {
        chatService.clearHistory("default");
        return "redirect:/chat";
    }
}

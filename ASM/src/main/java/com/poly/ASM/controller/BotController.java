package com.poly.ASM.controller;

import com.poly.ASM.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/bot")
public class BotController {

    @Autowired
    GeminiService geminiService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        String aiResponse = geminiService.getChatResponse(userMessage);
        return ResponseEntity.ok(Map.of("response", aiResponse));
    }
}
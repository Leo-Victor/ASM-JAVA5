package com.poly.ASM.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CheckModelController {

    // API Key của bạn (Giữ nguyên)
    private static final String API_KEY = "AIzaSyChBrLllJ8sTIMkOmA7KG0Q3r_n9hqEOUE";

    // Đường dẫn để hỏi danh sách model
    private static final String LIST_URL = "https://generativelanguage.googleapis.com/v1beta/models?key=" + API_KEY;

    @GetMapping("/check-models")
    public String checkAvailableModels() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            // Gửi lệnh GET để lấy danh sách
            String response = restTemplate.getForObject(LIST_URL, String.class);
            return "<h1>DANH SÁCH MODEL CỦA BẠN:</h1><pre>" + response + "</pre>";
        } catch (Exception e) {
            return "<h1>LỖI CHECK MODEL:</h1> " + e.getMessage();
        }
    }
}
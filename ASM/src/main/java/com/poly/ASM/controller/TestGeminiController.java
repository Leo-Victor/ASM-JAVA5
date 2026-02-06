package com.poly.ASM.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
public class TestGeminiController {

    // Dán lại chính xác API Key của bạn vào đây
    private static final String API_KEY = "AIzaSyChBrLllJ8sTIMkOmA7KG0Q3r_n9hqEOUE";
    // Thử dùng bản Flash cho ổn định trước
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

    @GetMapping("/test-ai")
    public String testConnection() {
        try {
            // Gửi đúng chữ "Xin chào" đơn giản nhất
            String json = "{ \"contents\": [{ \"parts\": [{ \"text\": \"Xin chào\" }] }] }";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            RestTemplate restTemplate = new RestTemplate();

            String response = restTemplate.postForObject(URL, entity, String.class);
            return "✅ KẾT NỐI THÀNH CÔNG! Google trả lời: <br><br>" + response;

        } catch (HttpClientErrorException e) {
            return "❌ LỖI TỪ GOOGLE (Mã " + e.getStatusCode() + "): <br>" + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "❌ LỖI JAVA: " + e.getMessage();
        }
    }
}
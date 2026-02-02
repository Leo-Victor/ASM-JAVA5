package com.poly.ASM.controller;

import com.poly.ASM.dao.ChatMessageRepository;
import com.poly.ASM.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageRepository chatRepo;

    // 1. Vào trang Admin Chat
    @GetMapping("/admin/chat")
    public String adminChatPage() {
        return "admin/chat-manager";
    }

    // 2. API lấy danh sách người chat (LOẠI BỎ ADMIN KHỎI DANH SÁCH)
    @GetMapping("/api/chat/users")
    @ResponseBody
    public List<String> getActiveUsers() {
        return chatRepo.findActiveUsers();
    }

    // 3. API lấy lịch sử chat
    @GetMapping("/api/chat/history/{username}")
    @ResponseBody
    public List<ChatMessage> getHistory(@PathVariable("username") String username) {
        return chatRepo.findChatHistory(username);
    }

    // 4. KHÁCH HÀNG GỬI -> ADMIN
    @MessageMapping("/chat.sendToAdmin")
    public void sendToAdmin(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(new Date());
        chatRepo.save(chatMessage);

        // Gửi tin nhắn vào kênh chung của Admin
        messagingTemplate.convertAndSend("/topic/admin", chatMessage);
    }

    // 5. ADMIN TRẢ LỜI -> KHÁCH HÀNG
    @MessageMapping("/chat.replyToUser")
    public void replyToUser(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(new Date());
        chatRepo.save(chatMessage); // Lưu vào DB

        // [SỬA LẠI ĐOẠN NÀY]
        // Thay vì dùng /user/..., ta dùng /topic/private/... để đảm bảo ai cũng nhận được
        String destination = "/topic/private/" + chatMessage.getReceiver();

        messagingTemplate.convertAndSend(destination, chatMessage);
    }
}
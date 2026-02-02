package com.poly.ASM.dao;

import com.poly.ASM.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 1. Lấy lịch sử chat giữa Admin và User cụ thể (Sắp xếp theo thời gian)
    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.sender = :user AND m.receiver = 'Admin') OR " +
            "(m.sender = 'Admin' AND m.receiver = :user) " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessage> findChatHistory(String user);

    // 2. Lấy danh sách những người đã từng nhắn tin cho Admin (Để hiện cột bên trái)
    @Query("SELECT DISTINCT m.sender FROM ChatMessage m WHERE m.sender != 'Admin'")
    List<String> findActiveUsers();
}
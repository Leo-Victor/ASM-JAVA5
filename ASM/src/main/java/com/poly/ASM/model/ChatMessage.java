package com.poly.ASM.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat_messages") // Tên bảng trong CSDL
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;    // Người gửi
    private String receiver;  // Người nhận (Admin hoặc Guest_123)
    private String content;   // Nội dung

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timestamp")
    private Date timestamp = new Date(); // Thời gian gửi

    // Loại tin nhắn (CHAT, JOIN, LEAVE) - Không cần lưu vào DB cũng được, nhưng cứ để
    @Transient // @Transient nghĩa là không tạo cột này trong SQL
    private String type;
}
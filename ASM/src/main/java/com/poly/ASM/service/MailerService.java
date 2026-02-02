package com.poly.ASM.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MailerService {

    @Autowired
    JavaMailSender sender;

    // 1. Gửi Email thông thường (Không đính kèm file)
    // Dùng cho: Quên mật khẩu, Đăng ký, Đặt hàng...
    public void send(String to, String subject, String body) {
        try {
            // Gọi hàm chính bên dưới, truyền null vào phần file
            this.send(to, subject, body, null);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // 2. Gửi Email có đính kèm file (Ảnh/Tài liệu)
    // Dùng cho: Chức năng Marketing
    public void send(String to, String subject, String body, MultipartFile attachment) throws MessagingException {
        // Tạo tin nhắn Mime
        MimeMessage message = sender.createMimeMessage();

        // true = Cho phép Multipart (đính kèm file + html)
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

        helper.setFrom("Tech Store <poly@fpt.edu.vn>"); // Tên người gửi hiển thị
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true); // true = Cho phép body chứa mã HTML

        // Xử lý đính kèm file (Nếu có)
        if (attachment != null && !attachment.isEmpty()) {
            String fileName = attachment.getOriginalFilename();
            // Thêm file vào mail
            helper.addAttachment(fileName, attachment);
        }

        // Gửi ngay
        sender.send(message);
    }
}
package com.poly.ASM.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint "/ws" để client (JS) kết nối vào
        // setAllowedOriginPatterns("*") là CỰC KỲ QUAN TRỌNG để tránh lỗi CORS khi chạy localhost
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. Kích hoạt Memory Broker
        // "/topic": Dùng cho chat Public hoặc Private kiểu "Hack" (/topic/private/...)
        // "/queue": Dùng cho Private chuẩn Spring Security (Giữ lại để dự phòng)
        registry.enableSimpleBroker("/topic", "/queue");

        // 2. Tiền tố để Client gửi tin lên Server (vào các hàm @MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");

        // 3. Tiền tố dành riêng cho User (Spring Security)
        registry.setUserDestinationPrefix("/user");
    }
}
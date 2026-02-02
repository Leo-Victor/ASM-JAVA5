package com.poly.ASM.interceptor;

import com.poly.ASM.service.interfaces.ShoppingCartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class GlobalInterceptor implements HandlerInterceptor {

    @Autowired
    ShoppingCartService cartService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. [QUAN TRỌNG] Bắt buộc tạo Session ngay lập tức để tránh lỗi "Response committed"
        request.getSession(true);

        // 2. Đưa giỏ hàng vào request để giao diện (Thymeleaf) có thể lấy được (biến 'cart')
        request.setAttribute("cart", cartService);

        return true; // Cho phép chạy tiếp
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Không cần làm gì ở đây nữa
    }
}
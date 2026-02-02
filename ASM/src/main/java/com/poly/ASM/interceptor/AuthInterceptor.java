package com.poly.ASM.interceptor;

import com.poly.ASM.model.Account;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    HttpSession session;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        Account user = (Account) session.getAttribute("user");

        // 1. LUÔN LUÔN Gửi đường dẫn hiện tại sang giao diện (để tô màu menu)
        request.setAttribute("currentURI", uri);

        String error = "";

        // 2. LOGIC BẢO MẬT (Chỉ chặn những trang cần thiết)
        if (user == null) {
            // Nếu chưa đăng nhập, CHỈ CHẶN khi vào các trang sau:
            if (uri.startsWith("/admin") ||
                    uri.startsWith("/order") ||
                    uri.startsWith("/account/change-password") ||
                    uri.startsWith("/account/edit-profile")) {

                error = "Vui lòng đăng nhập!";
            }
        }
        // 3. Nếu đã đăng nhập user thường mà cố vào Admin
        else if (!user.isAdmin() && uri.startsWith("/admin/")) {
            error = "Truy cập bị từ chối!";
        }

        // 4. Xử lý lỗi (Nếu có)
        if (error.length() > 0) {
            session.setAttribute("security-uri", uri);
            String encodedMsg = URLEncoder.encode(error, StandardCharsets.UTF_8);
            response.sendRedirect("/account/login?message=" + encodedMsg);
            return false;
        }

        return true;
    }
}
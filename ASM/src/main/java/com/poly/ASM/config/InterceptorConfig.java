package com.poly.ASM.config;

import com.poly.ASM.interceptor.AuthInterceptor;
import com.poly.ASM.interceptor.GlobalInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    AuthInterceptor authInterceptor;

    @Autowired
    GlobalInterceptor globalInterceptor; // 1. Phải Tiêm (Autowired) vào đây

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Đăng ký AuthInterceptor (Chặn đăng nhập)
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/admin/**", "/order/**", "/account/change-password", "/account/edit-profile");

        // 2. [QUAN TRỌNG] Đăng ký GlobalInterceptor để mọi trang đều có biến 'cart'
        registry.addInterceptor(globalInterceptor)
                .addPathPatterns("/**") // Chạy trên mọi đường dẫn
                .excludePathPatterns("/static/**", "/images/**", "/css/**", "/js/**");
    }
}

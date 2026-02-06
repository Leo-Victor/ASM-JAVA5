package com.poly.ASM.controller;

import com.poly.ASM.dao.CategoryDAO;
import com.poly.ASM.model.Category;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalController {

    @Autowired
    CategoryDAO categoryDAO;

    @Autowired
    HttpServletRequest request;

    /**
     * Hàm này sẽ chạy tự động trước mọi Request
     * Giúp nạp các dữ liệu dùng chung vào Model
     */
    @ModelAttribute
    public void globalAttributes(Model model) {
        // 1. Gửi đường dẫn hiện tại (currentURI) ra View
        // Giúp tô màu menu (Active) bên trang Admin và User
        String uri = request.getRequestURI();
        model.addAttribute("currentURI", uri);

        // 2. Gửi danh sách Loại hàng (Categories)
        // Để hiển thị menu "Danh mục" động ở header trang bán hàng
        List<Category> categories = categoryDAO.findAll();
        model.addAttribute("categories", categories);
    }
}
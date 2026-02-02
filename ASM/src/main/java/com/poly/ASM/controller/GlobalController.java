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

    @ModelAttribute
    public void globalAttributes(Model model, HttpServletRequest request) {
        // 1. Gửi URI để highlight menu Admin
        model.addAttribute("currentURI", request.getRequestURI());

        // 2. Gửi danh sách Loại hàng để tạo Menu Động (cho User)
        List<Category> categories = categoryDAO.findAll();
        model.addAttribute("categories", categories);
    }
}

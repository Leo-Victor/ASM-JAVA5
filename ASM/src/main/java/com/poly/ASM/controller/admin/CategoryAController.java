package com.poly.ASM.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/category")
public class CategoryAController {

    // URL: /admin/category/index
    @GetMapping("/index")
    public String index(Model model) {
        // Sau này gọi CategoryDAO để lấy list
        model.addAttribute("message", "Quản lý loại hàng");
        return "admin/categories"; // Bạn cần tạo file categories.html tương tự products.html
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, Model model) {
        model.addAttribute("message", "Chỉnh sửa loại hàng: " + id);
        return "admin/categories";
    }

    @PostMapping("/create")
    public String create(Model model) {
        // Logic thêm mới
        return "redirect:/admin/category/index";
    }

    @PostMapping("/update")
    public String update(Model model) {
        // Logic cập nhật
        return "redirect:/admin/category/edit/" + 1;
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") String id) {
        // Logic xóa
        return "redirect:/admin/category/index";
    }
}
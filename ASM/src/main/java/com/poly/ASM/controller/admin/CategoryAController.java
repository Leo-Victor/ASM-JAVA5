package com.poly.ASM.controller.admin;

import com.poly.ASM.dao.CategoryDAO;
import com.poly.ASM.model.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/category")
public class CategoryAController {

    @Autowired
    CategoryDAO categoryDAO;

    // 1. HIỂN THỊ TRANG CHỦ
    @GetMapping("/index")
    public String index(Model model) {
        Category item = new Category();
        model.addAttribute("item", item); // Dùng tên biến 'item' cho đồng bộ với Product

        // Lấy danh sách đổ xuống bảng
        model.addAttribute("items", categoryDAO.findAll());

        return "admin/categories"; // Trả về file HTML
    }

    // 2. EDIT (Đổ dữ liệu lên form)
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, Model model) {
        // Tìm loại hàng theo ID
        Category item = categoryDAO.findById(id).orElse(new Category());
        model.addAttribute("item", item);

        // Vẫn load danh sách bên dưới
        model.addAttribute("items", categoryDAO.findAll());

        return "admin/categories";
    }

    // 3. LƯU (Thêm mới hoặc Cập nhật)
    @PostMapping("/save")
    public String save(Category item, Model model) {
        try {
            // JPA tự động: Có ID rồi thì Update, chưa có thì Insert
            categoryDAO.save(item);
            model.addAttribute("message", "Lưu dữ liệu thành công!");
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
        }

        // Reset form sau khi lưu
        return "redirect:/admin/category/index";
    }

    // 4. XÓA
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") String id, Model model) {
        try {
            categoryDAO.deleteById(id);
            model.addAttribute("message", "Xóa thành công!");
        } catch (Exception e) {
            model.addAttribute("error", "Không thể xóa loại hàng này (đang chứa sản phẩm)!");
        }
        return "redirect:/admin/category/index";
    }
}
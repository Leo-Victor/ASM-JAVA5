package com.poly.ASM.controller.admin;

import com.poly.ASM.dao.CategoryDAO;
import com.poly.ASM.dao.ProductDAO;
import com.poly.ASM.model.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/report")
public class ReportAController {

    @Autowired
    ProductDAO productDAO;

    @Autowired
    CategoryDAO categoryDAO;

    // 1. TRANG CHỦ BÁO CÁO (Mặc định là Thống kê Tồn kho)
    // URL: /admin/report/index
    @GetMapping("/index")
    public String inventory(Model model) {
        // Lấy danh sách loại hàng
        // (Trong file HTML sẽ dùng vòng lặp để đếm số sản phẩm trong từng loại)
        List<Category> items = categoryDAO.findAll();

        model.addAttribute("items", items);
        model.addAttribute("title", "THỐNG KÊ HÀNG TỒN KHO");

        return "admin/reports";
    }

    // 2. THỐNG KÊ DOANH THU (Theo loại hàng)
    // URL: /admin/report/revenue
    @GetMapping("/revenue")
    public String revenue(Model model) {
        // Sau này bạn sẽ viết câu lệnh JPQL để tính tổng tiền bán được
        // Hiện tại mình lấy tạm danh sách Category để hiển thị demo cho không bị lỗi trang
        List<Category> items = categoryDAO.findAll();

        model.addAttribute("items", items);
        model.addAttribute("title", "BÁO CÁO DOANH THU CHUYÊN ĐỀ");

        return "admin/reports";
    }

    // 3. THỐNG KÊ KHÁCH HÀNG VIP
    // URL: /admin/report/vip
    @GetMapping("/vip")
    public String vip(Model model) {
        // Logic tìm khách hàng mua nhiều nhất sẽ viết ở đây
        // Tạm thời trả về trang reports để giữ khung giao diện
        return "admin/reports";
    }
}

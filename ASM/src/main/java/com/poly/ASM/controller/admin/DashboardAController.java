package com.poly.ASM.controller.admin;

import com.poly.ASM.dao.AccountDAO;
import com.poly.ASM.dao.OrderDAO;
import com.poly.ASM.dao.ProductDAO;
import com.poly.ASM.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardAController {

    @Autowired
    ProductDAO productDAO;

    @Autowired
    AccountDAO accountDAO;

    @Autowired
    OrderDAO orderDAO;

    // URL truy cập: http://localhost:8080/admin/dashboard
    @GetMapping("/admin/dashboard")
    public String index(Model model) {

        // 1. TỔNG SỐ SẢN PHẨM
        // Dùng hàm count() có sẵn của JPA
        long totalProducts = productDAO.count();

        // 2. TỔNG SỐ KHÁCH HÀNG (Loại trừ tài khoản Admin)
        // Lấy tất cả tài khoản -> Lọc những ai không phải Admin -> Đếm
        long totalUsers = accountDAO.findAll().stream()
                .filter(acc -> !acc.isAdmin())
                .count();

        // 3. ĐƠN HÀNG CHỜ DUYỆT (Status = 0)
        // Lấy tất cả đơn -> Lọc status = 0 -> Đếm
        long pendingOrders = orderDAO.findAll().stream()
                .filter(o -> o.getStatus() == 0)
                .count();

        // 4. TỔNG DOANH THU THỰC TẾ
        // Lấy tất cả đơn -> Lọc status = 2 (Thành công) -> Cộng dồn tổng tiền
        List<Order> successOrders = orderDAO.findAll().stream()
                .filter(o -> o.getStatus() == 2 && o.getTotalAmount() != null)
                .collect(Collectors.toList());

        double totalRevenue = 0;
        for (Order o : successOrders) {
            totalRevenue += o.getTotalAmount();
        }

        // 5. Gửi số liệu sang giao diện (admin/index.html)
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("totalRevenue", totalRevenue);

        return "admin/index"; // Trỏ đến file templates/admin/index.html
    }
}
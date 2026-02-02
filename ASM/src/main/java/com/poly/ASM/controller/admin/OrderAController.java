package com.poly.ASM.controller.admin;

import com.poly.ASM.dao.OrderDAO;
import com.poly.ASM.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/order") // Định nghĩa gốc là /admin/order
public class OrderAController {

    @Autowired
    OrderDAO orderDAO;

    // 1. Hiển thị danh sách đơn hàng
    @GetMapping("/index")
    public String index(Model model) {
        // Lấy tất cả đơn hàng, sắp xếp theo ngày mới nhất (nếu muốn thì thêm sort)
        List<Order> items = orderDAO.findAll();
        model.addAttribute("items", items);
        return "admin/orders"; // Trả về file html
    }

    // 2. Xem chi tiết / Sửa đơn hàng
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model) {
        Order order = orderDAO.findById(id).get();
        model.addAttribute("order", order);
        model.addAttribute("items", orderDAO.findAll());
        return "admin/orders";
    }

    // 3. Cập nhật đơn hàng (Ví dụ cập nhật địa chỉ)
    @PostMapping("/update")
    public String update(Order order) {
        orderDAO.save(order);
        return "redirect:/admin/order/edit/" + order.getId();
    }
}
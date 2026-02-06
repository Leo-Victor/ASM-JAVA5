package com.poly.ASM.controller.admin;

import com.poly.ASM.dao.OrderDAO;
import com.poly.ASM.dao.OrderDetailDAO;
import com.poly.ASM.model.Order;
import com.poly.ASM.model.OrderDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/order")
public class OrderAController {

    @Autowired
    OrderDAO orderDAO;

    @Autowired
    OrderDetailDAO orderDetailDAO;

    // 1. HIỂN THỊ DANH SÁCH
    @RequestMapping("/index")
    public String index(Model model) {
        // Không chọn đơn nào thì form bên trái rỗng
        model.addAttribute("order", null);

        // Lấy danh sách, sắp xếp đơn mới nhất lên đầu (DESC theo ID)
        List<Order> items = orderDAO.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("items", items);

        return "admin/orders";
    }

    // 2. XEM CHI TIẾT & SỬA TRẠNG THÁI
    @RequestMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model) {
        // Lấy thông tin đơn hàng
        Order order = orderDAO.findById(id).orElse(null);
        model.addAttribute("order", order);

        // Lấy chi tiết sản phẩm trong đơn hàng đó (Để hiển thị Admin xem khách mua gì)
        if(order != null) {
            List<OrderDetail> details = orderDetailDAO.findByOrder(order);
            model.addAttribute("details", details);
        }

        // Vẫn load danh sách bên phải
        List<Order> items = orderDAO.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("items", items);

        return "admin/orders";
    }

    // 3. CẬP NHẬT TRẠNG THÁI
    @PostMapping("/update")
    public String update(@RequestParam("id") Long id,
                         @RequestParam("status") Integer status,
                         Model model) {
        try {
            // Tìm đơn hàng cũ
            Order order = orderDAO.findById(id).orElse(null);
            if (order != null) {
                // Chỉ thay đổi trạng thái, giữ nguyên các thông tin khác
                order.setStatus(status);
                orderDAO.save(order);
                model.addAttribute("message", "Cập nhật trạng thái đơn hàng thành công!");
            }
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi cập nhật: " + e.getMessage());
        }

        // Quay lại đúng trang edit của đơn hàng đó để Admin thấy kết quả ngay
        return "redirect:/admin/order/edit/" + id;
    }
}
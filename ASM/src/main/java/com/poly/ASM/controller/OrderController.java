package com.poly.ASM.controller;

import com.poly.ASM.dao.OrderDAO;
import com.poly.ASM.dao.OrderDetailDAO;
import com.poly.ASM.dao.ProductDAO;
// [QUAN TRỌNG] Đảm bảo dùng bộ 3 này của com.fasterxml
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.poly.ASM.model.*;
import com.poly.ASM.service.MailerService;
import com.poly.ASM.service.interfaces.OrderService;
import com.poly.ASM.service.interfaces.ShoppingCartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
//import tools.jackson.databind.ObjectMapper;
//import tools.jackson.databind.node.ObjectNode;


import java.net.URLEncoder; // [MỚI] Import để mã hóa tiếng Việt
import java.nio.charset.StandardCharsets; // [MỚI] Import bảng mã UTF-8
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class OrderController {

    @Autowired
    OrderService orderService; // Sử dụng Service để xử lý lưu DB (Chuẩn mới)

    @Autowired
    ShoppingCartService cartService; // Để lấy tổng tiền gửi mail

    @Autowired
    MailerService mailerService; // [QUAN TRỌNG] Để gửi mail xác nhận

    @Autowired
    HttpSession session;

    // ============================================================
    // 1. THANH TOÁN (CHECKOUT)
    // ============================================================
    @GetMapping("/order/checkout")
    public String checkout(Model model) {
        // Kiểm tra đăng nhập & Mã hóa thông báo tiếng Việt
        if (session.getAttribute("user") == null) {
            String msg = URLEncoder.encode("Vui lòng đăng nhập để thanh toán!", StandardCharsets.UTF_8);
            return "redirect:/account/login?message=" + msg;
        }

        // Đẩy giỏ hàng sang view để hiển thị lại lần cuối
        model.addAttribute("cart", cartService);
        return "user/check-out"; // [ĐÚNG] Trỏ vào file templates/user/check-out.html
    }

    // ============================================================
    // 2. XỬ LÝ MUA HÀNG (PURCHASE)
    // ============================================================
    @PostMapping("/order/purchase")
    public String purchase(
            @RequestParam("address") String address,
            @RequestParam("phone") String phone,
            HttpServletRequest request) {

        Account user = (Account) session.getAttribute("user");
        if (user == null) {
            return "redirect:/account/login";
        }

        try {
            // A. TẠO ĐƠN HÀNG
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode orderData = mapper.createObjectNode();
            orderData.put("address", address);

            // Lưu tổng tiền trước khi clear giỏ hàng
            double totalAmount = cartService.getAmount();

            // Gọi hàm create bên Service
            Order newOrder = orderService.create(orderData, user);

            // B. GỬI EMAIL XÁC NHẬN (SỬA LẠI CHỖ NÀY)
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                String subject = "Tech Store - Xác nhận đơn hàng #" + newOrder.getId();
                String body = "<h3>Cảm ơn " + user.getFullname() + " đã đặt hàng!</h3>" +
                        "<p><strong>Mã đơn hàng:</strong> " + newOrder.getId() + "</p>" +
                        "<p><strong>Ngày đặt:</strong> " + newOrder.getCreateDate() + "</p>" +
                        "<p><strong>Tổng tiền:</strong> " + String.format("%,.0f", totalAmount) + " VNĐ</p>" +
                        "<p><strong>Địa chỉ giao hàng:</strong> " + address + "</p>" +
                        "<p><strong>Số điện thoại:</strong> " + phone + "</p>" +
                        "<hr>" +
                        "<p>Vui lòng theo dõi trạng thái đơn hàng trong mục 'Lịch sử đơn hàng'.</p>";

                // [SỬA LỖI TẠI ĐÂY]: Đổi từ queue() thành send()
                mailerService.send(user.getEmail(), subject, body);
            }

            return "redirect:/order/list";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/order/checkout?error=true";
        }
    }

    // ============================================================
    // 3. DANH SÁCH LỊCH SỬ ĐƠN HÀNG
    // ============================================================
    @GetMapping("/order/list")
    public String list(Model model) {
        Account user = (Account) session.getAttribute("user");

        // Kiểm tra đăng nhập
        if (user == null) {
            session.setAttribute("security-uri", "/order/list");
            String msg = URLEncoder.encode("Bạn cần đăng nhập để xem lịch sử!", StandardCharsets.UTF_8);
            return "redirect:/account/login?message=" + msg;
        }

        // Lấy danh sách từ Service (đã sắp xếp ngày mới nhất lên đầu)
        List<Order> orders = orderService.findByUsername(user.getUsername());
        model.addAttribute("orders", orders);

        return "user/order-list"; // [ĐÚNG] Trỏ vào file templates/user/order-list.html
    }

    // ============================================================
    // 4. CHI TIẾT ĐƠN HÀNG
    // ============================================================
    @GetMapping("/order/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Account user = (Account) session.getAttribute("user");

        if (user == null) {
            session.setAttribute("security-uri", "/order/detail/" + id);
            return "redirect:/account/login";
        }

        Order order = orderService.findById(id);
        model.addAttribute("order", order);

        return "user/order-detail"; // [ĐÚNG] Trỏ vào file templates/user/order-detail.html
    }
}
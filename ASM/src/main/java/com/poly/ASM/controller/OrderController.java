package com.poly.ASM.controller;

import com.poly.ASM.dao.OrderDAO;
import com.poly.ASM.dao.OrderDetailDAO;
import com.poly.ASM.dao.ProductDAO;
import com.poly.ASM.model.*;
import com.poly.ASM.service.MailerService;
import com.poly.ASM.service.interfaces.ShoppingCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.net.URLEncoder; // [MỚI] Import để mã hóa tiếng Việt
import java.nio.charset.StandardCharsets; // [MỚI] Import bảng mã UTF-8
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class OrderController {

    @Autowired
    ShoppingCartService cartService;

    @Autowired
    OrderDAO orderDAO;

    @Autowired
    OrderDetailDAO orderDetailDAO;

    @Autowired
    ProductDAO productDAO;

    @Autowired
    MailerService mailerService;

    @Autowired
    HttpSession session;

    // ============================================================
    // 1. THANH TOÁN (CHECKOUT)
    // ============================================================
    @GetMapping("/order/checkout")
    public String checkout(Model model) {
        if(session.getAttribute("user") == null) {
            String msg = URLEncoder.encode("Vui lòng đăng nhập để thanh toán!", StandardCharsets.UTF_8);
            return "redirect:/account/login?message=" + msg;
        }

        model.addAttribute("cart", cartService);
        return "user/check-out";
    }

    @PostMapping("/order/purchase")
    public String purchase(
            @RequestParam("address") String address,
            @RequestParam("phone") String phone,
            Model model) {

        Account user = (Account) session.getAttribute("user");
        if(user == null) {
            return "redirect:/account/login";
        }

        // A. Tạo đơn hàng
        Order order = new Order();
        order.setAccount(user);
        order.setCreateDate(new Date());
        order.setAddress(address);

        Order newOrder = orderDAO.save(order);

        // B. Lưu chi tiết đơn hàng
        for(CartItem item : cartService.getItems()) {
            OrderDetail od = new OrderDetail();
            od.setOrder(newOrder);
            Product product = productDAO.findById(item.getId()).orElse(null);
            od.setProduct(product);
            od.setPrice(item.getPrice());
            od.setQuantity(item.getQty());
            orderDetailDAO.save(od);
        }

        // C. Gửi Email
        try {
            if(user.getEmail() != null && !user.getEmail().isEmpty()) {
                String subject = "Xác nhận đơn hàng #" + newOrder.getId();
                String body = "<h3>Cảm ơn bạn đã đặt hàng tại Tech Store!</h3>" +
                        "<p>Mã đơn hàng: <strong>" + newOrder.getId() + "</strong></p>" +
                        "<p>Tổng tiền: " + String.format("%,.0f", cartService.getAmount()) + " VNĐ</p>" +
                        "<p>Địa chỉ giao hàng: " + address + "</p>";
                mailerService.send(user.getEmail(), subject, body);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // D. Xóa giỏ hàng
        cartService.clear();

        return "redirect:/order/detail/" + newOrder.getId();
    }

    // ============================================================
    // 2. DANH SÁCH ĐƠN HÀNG (TRA CỨU)
    // ============================================================
    @GetMapping("/order/list")
    public String list(Model model) {
        Account user = (Account) session.getAttribute("user");

        // [CHECK LOGIN & ENCODE MESSAGE]
        if(user == null) {
            session.setAttribute("security-uri", "/order/list");
            String msg = URLEncoder.encode("Bạn cần đăng nhập để xem lịch sử đơn hàng!", StandardCharsets.UTF_8);
            return "redirect:/account/login?message=" + msg;
        }

        // Lấy danh sách đơn hàng của user đó
        List<Order> orders = orderDAO.findAll().stream()
                .filter(order -> order.getAccount().getUsername().equals(user.getUsername()))
                .collect(Collectors.toList());

        model.addAttribute("orders", orders);
        return "user/order-list";
    }

    // ============================================================
    // 3. CHI TIẾT ĐƠN HÀNG
    // ============================================================
    @GetMapping("/order/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        if(session.getAttribute("user") == null) {
            session.setAttribute("security-uri", "/order/detail/" + id);
            String msg = URLEncoder.encode("Vui lòng đăng nhập để xem chi tiết!", StandardCharsets.UTF_8);
            return "redirect:/account/login?message=" + msg;
        }

        Order order = orderDAO.findById(id).orElse(null);
        model.addAttribute("order", order);
        return "user/order-detail";
    }
}
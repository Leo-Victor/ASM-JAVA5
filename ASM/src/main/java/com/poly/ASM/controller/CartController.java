package com.poly.ASM.controller;

import com.poly.ASM.service.interfaces.ShoppingCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Controller
public class CartController {

    @Autowired
    ShoppingCartService cartService;

    @Autowired
    HttpSession session;

    // 1. XEM GIỎ HÀNG
    @GetMapping("/cart/view")
    public String view(Model model) {
        // [CHECK LOGIN] Nếu chưa đăng nhập thì bắt đăng nhập
        if(session.getAttribute("user") == null) {
            String msg = URLEncoder.encode("Vui lòng đăng nhập để xem giỏ hàng!", StandardCharsets.UTF_8);
            return "redirect:/account/login?message=" + msg;
        }

        model.addAttribute("cart", cartService);
        return "user/cart"; // Trả về trang giỏ hàng (cart.html)
    }

    // 2. NÚT "THÊM VÀO GIỎ"
    @GetMapping("/cart/add/{id}")
    public String add(@PathVariable("id") Integer id) {
        // [CHECK LOGIN]
        if(session.getAttribute("user") == null) {
            // Lưu lại hành động: Sau khi login xong thì tự động add vào giỏ (Nâng cao)
            // Nhưng hiện tại ta chỉ cần bắt login là đủ
            String msg = URLEncoder.encode("Vui lòng đăng nhập để mua hàng!", StandardCharsets.UTF_8);
            return "redirect:/account/login?message=" + msg;
        }

        cartService.add(id);
        return "redirect:/cart/view"; // Thêm xong thì xem giỏ hàng
    }

    // 3. NÚT "MUA NGAY" (Thêm vào giỏ -> Sang trang thanh toán luôn)
    @GetMapping("/cart/buy/{id}")
    public String buy(@PathVariable("id") Integer id) {
        // [CHECK LOGIN]
        if(session.getAttribute("user") == null) {
            String msg = URLEncoder.encode("Vui lòng đăng nhập để thanh toán!", StandardCharsets.UTF_8);
            return "redirect:/account/login?message=" + msg;
        }

        cartService.add(id);
        return "redirect:/order/checkout"; // Chuyển thẳng sang trang thanh toán
    }

    // 4. XÓA SẢN PHẨM KHỎI GIỎ
    @GetMapping("/cart/remove/{id}")
    public String remove(@PathVariable("id") Integer id) {
        cartService.remove(id);
        return "redirect:/cart/view";
    }

    // 5. CẬP NHẬT SỐ LƯỢNG (Dùng cho nút tăng giảm trong giỏ)
    @PostMapping("/cart/update")
    public String update(@RequestParam("id") Integer id, @RequestParam("qty") Integer qty) {
        cartService.update(id, qty);
        return "redirect:/cart/view";
    }

    // 6. XÓA HẾT GIỎ HÀNG
    @GetMapping("/cart/clear")
    public String clear() {
        cartService.clear();
        return "redirect:/cart/view";
    }

    // 7. API THÊM VÀO GIỎ (Dùng cho AJAX - Không chuyển trang)
    @GetMapping("/api/cart/add/{id}")
    @ResponseBody // Quan trọng: Trả về dữ liệu JSON chứ không phải giao diện
    public ResponseEntity<Map<String, Object>> addToCartApi(@PathVariable("id") Integer id) {
        Map<String, Object> response = new HashMap<>();

        // 1. Kiểm tra đăng nhập
        if (session.getAttribute("user") == null) {
            response.put("status", "error");
            response.put("message", "Vui lòng đăng nhập!");
            return ResponseEntity.ok(response);
        }

        // 2. Thêm vào giỏ
        cartService.add(id);

        // 3. Trả về thành công và số lượng mới
        response.put("status", "success");
        response.put("count", cartService.getCount()); // Lấy tổng số lượng
        return ResponseEntity.ok(response);
    }
}
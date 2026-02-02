package com.poly.ASM.controller;

import com.poly.ASM.dao.CategoryDAO;
import com.poly.ASM.dao.ProductDAO;
import com.poly.ASM.model.Product;
import com.poly.ASM.service.MailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    ProductDAO productDAO;

    @Autowired
    CategoryDAO categoryDAO;

    @Autowired
    MailerService mailerService; // Service gửi mail (dùng cho trang liên hệ)

    // ============================================================
    // 1. TRANG CHỦ (HOME)
    // ============================================================
    @RequestMapping(value = {"/", "/home/index"})
    public String home(Model model) {
        setupHomePageData(model);
        return "user/index";
    }

    // ============================================================
    // 2. TÌM KIẾM SẢN PHẨM (CÓ PHÂN TRANG)
    // ============================================================
    @GetMapping("/product/search")
    public String search(
            @RequestParam("keywords") Optional<String> kw,
            @RequestParam("categoryId") Optional<String> cid,
            @RequestParam("priceRange") Optional<Integer> price,
            @RequestParam("p") Optional<Integer> p, // Trang số mấy
            Model model) {

        // 1. Giữ lại giá trị filter để hiện lại trên giao diện
        String keywords = kw.orElse("");
        model.addAttribute("keywords", keywords);
        model.addAttribute("categoryId", cid.orElse(""));
        model.addAttribute("priceRange", price.orElse(0));

        // 2. Lấy tất cả và lọc (Stream API)
        List<Product> list = productDAO.findAll();

        // a. Lọc theo Tên
        if (!keywords.isBlank()) {
            list = list.stream()
                    .filter(prod -> prod.getName().toLowerCase().contains(keywords.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // b. Lọc theo Loại
        if (cid.isPresent() && !cid.get().isBlank()) {
            list = list.stream()
                    .filter(prod -> prod.getCategory().getId().equals(cid.get()))
                    .collect(Collectors.toList());
        }

        // c. Lọc theo Giá
        Integer range = price.orElse(0);
        if (range == 1) { // < 10 triệu
            list = list.stream().filter(prod -> prod.getPrice() < 10000000).collect(Collectors.toList());
        } else if (range == 2) { // 10 - 20 triệu
            list = list.stream().filter(prod -> prod.getPrice() >= 10000000 && prod.getPrice() <= 20000000).collect(Collectors.toList());
        } else if (range == 3) { // > 20 triệu
            list = list.stream().filter(prod -> prod.getPrice() > 20000000).collect(Collectors.toList());
        }

        // 3. Phân trang (8 sản phẩm/trang)
        Page<Product> page = toPage(list, p.orElse(0), 8);
        model.addAttribute("searchResult", page); // Gửi kết quả dạng Page

        // Gửi danh mục để đổ vào dropdown tìm kiếm
        model.addAttribute("categories", categoryDAO.findAll());

        return "user/index";
    }

    // ============================================================
    // 3. XEM THEO LOẠI (CÓ PHÂN TRANG)
    // ============================================================
    @GetMapping("/product/list-by-category/{id}")
    public String listByCat(@PathVariable("id") String id,
                            @RequestParam("p") Optional<Integer> p,
                            Model model) {
        List<Product> list = productDAO.findByCategoryId(id);
        Page<Product> page = toPage(list, p.orElse(0), 8);

        model.addAttribute("items", page);
        model.addAttribute("catId", id);
        return "user/product-list";
    }

    // ============================================================
    // 4. TRANG LIÊN HỆ (CONTACT) - MỚI BỔ SUNG
    // ============================================================

    // Hiển thị trang liên hệ
    @GetMapping("/home/contact")
    public String contact() {
        return "user/contact";
    }

    // Xử lý gửi tin nhắn liên hệ
    @PostMapping("/home/contact")
    public String sendContact(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email, // Email khách hàng (lấy từ form)
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            Model model) {
        try {
            // --- QUAN TRỌNG: SỬA DÒNG NÀY ---
            // Điền Email CỦA BẠN (Email Admin) vào đây để nhận thư
            // Ví dụ: khoaledang301@gmail.com (Email mình thấy trong ảnh bạn gửi)
            String to = "khoaledang301@gmail.com";
            // --------------------------------

            String mailSubject = "Liên hệ mới từ: " + fullName;
            String mailBody = "<h3>Khách hàng gửi liên hệ</h3>" +
                    "<p><strong>Họ tên:</strong> " + fullName + "</p>" +
                    "<p><strong>Email khách:</strong> " + email + "</p>" +
                    "<p><strong>Chủ đề:</strong> " + subject + "</p>" +
                    "<p><strong>Nội dung:</strong><br>" + message + "</p>";

            mailerService.send(to, mailSubject, mailBody);

            model.addAttribute("message", "Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi sớm nhất.");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi gửi tin nhắn: " + e.getMessage());
        }
        return "user/contact";
    }

    // ============================================================
    // 5. CÁC HÀM PHỤ TRỢ (HELPER)
    // ============================================================

    // Hàm cắt List thành Page để phân trang
    private Page<Product> toPage(List<Product> list, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        if(start > list.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, list.size());
        }

        List<Product> subList = list.subList(start, end);
        return new PageImpl<>(subList, pageable, list.size());
    }

    // Hàm chuẩn bị dữ liệu trang chủ (Hàng mới, Sale, Bán chạy)
    private void setupHomePageData(Model model) {
        List<Product> list = productDAO.findAll();

        // Hàng mới (Lấy 4 cái cuối)
        List<Product> newProducts = new ArrayList<>(list);
        Collections.reverse(newProducts);

        // Hàng Sale (Random)
        List<Product> saleProducts = new ArrayList<>(list);
        Collections.shuffle(saleProducts);

        // Đổ vào model
        int limit = Math.min(list.size(), 4); // Tránh lỗi nếu list ít hơn 4
        model.addAttribute("newProducts", newProducts.subList(0, limit));
        model.addAttribute("saleProducts", saleProducts.subList(0, limit));
        model.addAttribute("bestSellers", list.subList(0, limit));

        model.addAttribute("categories", categoryDAO.findAll());
    }

    // ============================================================
    // 6. TRANG CHI TIẾT SẢN PHẨM
    // ============================================================
    @GetMapping("/product/detail/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {
        Product p = productDAO.findById(id).orElse(null);
        model.addAttribute("item", p);
        return "user/product-detail";
    }
}
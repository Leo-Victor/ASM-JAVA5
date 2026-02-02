package com.poly.ASM.controller.admin;

import com.poly.ASM.dao.AccountDAO;
import com.poly.ASM.model.Account;
import com.poly.ASM.service.MailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin/marketing")
public class MarketingController {

    @Autowired
    AccountDAO accountDAO;

    @Autowired
    MailerService mailerService;

    @GetMapping("/index")
    public String index(Model model) {
        List<Account> users = accountDAO.findAll();
        model.addAttribute("users", users);
        return "admin/marketing";
    }

    @PostMapping("/send")
    public String send(
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam("file") MultipartFile file, // [MỚI] Nhận file ảnh
            Model model) {

        try {
            if (to.equals("all")) {
                // --- GỬI CHO TẤT CẢ (TRỪ ADMIN) ---
                List<Account> allUsers = accountDAO.findAll();
                int count = 0;

                for (Account user : allUsers) {
                    // Logic: Chỉ gửi nếu có Email VÀ KHÔNG PHẢI ADMIN (!user.isAdmin())
                    if (user.getEmail() != null && !user.getEmail().isEmpty() && !user.isAdmin()) {

                        // Gọi hàm gửi mail có đính kèm file
                        mailerService.send(user.getEmail(), subject, body, file);
                        count++;
                    }
                }
                model.addAttribute("message", "Đã gửi thành công cho " + count + " khách hàng (Đã bỏ qua Admin)!");
            } else {
                // --- GỬI CHO 1 NGƯỜI CỤ THỂ ---
                mailerService.send(to, subject, body, file);
                model.addAttribute("message", "Đã gửi mail thành công đến: " + to);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi gửi mail: " + e.getMessage());
        }

        model.addAttribute("users", accountDAO.findAll());
        return "admin/marketing";
    }
}
package com.poly.ASM.controller.admin;

import com.poly.ASM.dao.AccountDAO;
import com.poly.ASM.model.Account;
import com.poly.ASM.service.MailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/account")
public class AccountAController {

    @Autowired
    AccountDAO accountDAO;

    @Autowired
    MailerService mailerService; // Tiêm service gửi mail

    // ============================================================
    // 1. QUẢN LÝ TÀI KHOẢN (CRUD)
    // ============================================================

    // Hiển thị danh sách và Form nhập liệu
    @GetMapping("/index")
    public String index(Model model) {
        // Lấy danh sách tất cả user
        List<Account> items = accountDAO.findAll();
        model.addAttribute("items", items);

        // Tạo đối tượng Account rỗng cho form
        model.addAttribute("form", new Account());

        return "admin/accounts"; // Trả về giao diện quản lý accounts
    }

    // Chọn user để chỉnh sửa (Đổ dữ liệu lên form)
    @GetMapping("/edit/{username}")
    public String edit(@PathVariable("username") String username, Model model) {
        Account acc = accountDAO.findById(username).orElse(new Account());
        model.addAttribute("form", acc);

        // Vẫn phải lấy list để hiện bảng bên dưới
        model.addAttribute("items", accountDAO.findAll());

        return "admin/accounts";
    }

    // Thêm mới tài khoản
    @PostMapping("/create")
    public String create(Account account, Model model) {
        try {
            if(accountDAO.existsById(account.getUsername())) {
                model.addAttribute("message", "Username đã tồn tại!");
            } else {
                account.setPhoto("user.png"); // Ảnh mặc định
                accountDAO.save(account);
                model.addAttribute("message", "Thêm mới thành công!");
            }
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi thêm mới: " + e.getMessage());
        }

        // Reset form và load lại list
        return "forward:/admin/account/index";
    }

    // Cập nhật tài khoản
    @PostMapping("/update")
    public String update(Account account, Model model) {
        try {
            if(!accountDAO.existsById(account.getUsername())) {
                model.addAttribute("message", "Username không tồn tại!");
            } else {
                accountDAO.save(account);
                model.addAttribute("message", "Cập nhật thành công!");
            }
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi cập nhật: " + e.getMessage());
        }
        return "forward:/admin/account/index";
    }

    // Xóa tài khoản
    @GetMapping("/delete/{username}")
    public String delete(@PathVariable("username") String username, Model model) {
        try {
            accountDAO.deleteById(username);
            model.addAttribute("message", "Xóa thành công!");
        } catch (Exception e) {
            model.addAttribute("message", "Không thể xóa (Tài khoản đang có đơn hàng)!");
        }
        return "forward:/admin/account/index";
    }

    // Nút làm mới form
    @GetMapping("/reset")
    public String reset() {
        return "redirect:/admin/account/index";
    }

    // ============================================================
    // 2. GỬI EMAIL THÔNG BÁO/KHUYẾN MÃI
    // ============================================================

    // Hiện form gửi mail cho user cụ thể
    @GetMapping("/send-mail/{username}")
    public String sendMailForm(@PathVariable("username") String username, Model model) {
        Account user = accountDAO.findById(username).orElse(null);
        if (user != null) {
            model.addAttribute("user", user); // Gửi thông tin user sang form mail
            return "admin/mail-form"; // Bạn cần tạo file này (admin/mail-form.html)
        }
        return "forward:/admin/account/index";
    }

    // Xử lý gửi mail
    @PostMapping("/send-mail")
    public String sendMail(
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            Model model) {
        try {
            mailerService.send(to, subject, body);
            model.addAttribute("message", "Đã gửi mail thành công đến: " + to);
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi gửi mail: " + e.getMessage());
        }

        // Gửi xong quay lại trang danh sách
        return "forward:/admin/account/index";
    }
}
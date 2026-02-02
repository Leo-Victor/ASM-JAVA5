package com.poly.ASM.controller;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.poly.ASM.dao.AccountDAO;
import com.poly.ASM.model.Account;
import com.poly.ASM.service.MailerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountController {

    @Autowired
    AccountDAO accountDAO;

    @Autowired
    HttpSession session;

    @Autowired
    MailerService mailerService;

    // ============================================================
    // 1. ĐĂNG NHẬP & ĐĂNG XUẤT
    // ============================================================
    @GetMapping("/account/login")
    public String loginForm() { return "user/login"; }

    @PostMapping("/account/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        Model model) {
        try {
            Account user = accountDAO.findById(username).orElse(null);

            if(user == null) {
                model.addAttribute("message", "Tài khoản không tồn tại!");
            } else if(!user.getPassword().equals(password)) {
                model.addAttribute("message", "Sai mật khẩu!");
            } else if(!user.isActivated()) {
                model.addAttribute("message", "Tài khoản chưa được kích hoạt!");
            } else {
                // Đăng nhập thành công -> Lưu vào Session
                session.setAttribute("user", user);

                // --- [INTERCEPTOR] KIỂM TRA LINK CŨ ĐỂ CHUYỂN HƯỚNG ---
                Object uri = session.getAttribute("security-uri");
                if(uri != null) {
                    session.removeAttribute("security-uri"); // Xóa đi sau khi dùng
                    return "redirect:" + uri; // Quay lại trang bị chặn trước đó
                }

                // Nếu không có link cũ, chuyển hướng theo vai trò
                if(user.isAdmin()) return "redirect:/admin/dashboard";
                return "redirect:/home/index";
            }
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi đăng nhập!");
        }
        return "user/login";
    }

    @GetMapping("/account/logout")
    public String logout() {
        session.removeAttribute("user");
        session.removeAttribute("security-uri");
        return "redirect:/account/login";
    }

    // ============================================================
    // 2. ĐĂNG KÝ (Có gửi Mail)
    // ============================================================
    @GetMapping("/account/sign-up")
    public String signup(Model model) {
        model.addAttribute("user", new Account());
        return "user/sign-up";
    }

    @PostMapping("/account/sign-up")
    public String signup(@ModelAttribute("user") Account account, Model model) {
        try {
            if(accountDAO.existsById(account.getUsername())) {
                model.addAttribute("message", "Tên đăng nhập đã tồn tại!");
                return "user/sign-up";
            }

            account.setActivated(true);
            account.setAdmin(false);
            account.setPhoto("user.png");
            accountDAO.save(account);

            // Gửi mail thông báo
            try {
                if(account.getEmail() != null && !account.getEmail().isEmpty()) {
                    String subject = "Chào mừng bạn đến với Tech Store!";
                    String body = "<h3>Xin chào " + account.getFullname() + "!</h3>" +
                            "<p>Chúc mừng bạn đã đăng ký thành công.</p>" +
                            "<p>Tên đăng nhập: <strong>" + account.getUsername() + "</strong></p>" +
                            "<br>Trân trọng,<br>Tech Store Team";
                    mailerService.send(account.getEmail(), subject, body);
                }
            } catch (Exception ex) {
                System.out.println("Lỗi gửi mail: " + ex.getMessage());
            }

            model.addAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "user/login";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Lỗi đăng ký: " + e.getMessage());
            return "user/sign-up";
        }
    }

    // ============================================================
    // 3. ĐỔI MẬT KHẨU
    // ============================================================
    @GetMapping("/account/change-password")
    public String changePasswordForm() {
        return "user/change-password";
    }

    @PostMapping("/account/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        Account user = (Account) session.getAttribute("user");
        if(user == null) return "redirect:/account/login";

        if(!user.getPassword().equals(currentPassword)) {
            model.addAttribute("message", "Mật khẩu hiện tại không đúng!");
        } else if(!newPassword.equals(confirmPassword)) {
            model.addAttribute("message", "Xác nhận mật khẩu mới không trùng khớp!");
        } else {
            user.setPassword(newPassword);
            accountDAO.save(user);
            model.addAttribute("message", "Đổi mật khẩu thành công!");
        }
        return "user/change-password";
    }

    // ============================================================
    // 4. CẬP NHẬT HỒ SƠ (Đã sửa lỗi hiển thị sai thông tin)
    // ============================================================

    @GetMapping("/account/edit-profile")
    public String editProfile(Model model) {
        Account user = (Account) session.getAttribute("user");
        model.addAttribute("user", user);
        return "user/edit-profile";
    }

    @PostMapping("/account/edit-profile")
    public String updateProfile(
            @ModelAttribute("user") Account formData,
            @RequestParam("photoFile") MultipartFile photoFile, // Nhận file ảnh từ form
            Model model) {
        try {
            // 1. Lấy User cũ đang đăng nhập
            Account current = (Account) session.getAttribute("user");

            // 2. Cập nhật thông tin cơ bản
            current.setFullname(formData.getFullname());
            current.setEmail(formData.getEmail());

            // --- XỬ LÝ UPLOAD ẢNH ---
            if (!photoFile.isEmpty()) {
                // A. Xác định thư mục lưu ảnh (Đường dẫn tuyệt đối đến thư mục static/images trong dự án)
                // Lưu ý: Cách này chỉ dùng tốt khi chạy trong IDE (IntelliJ/Eclipse) để phát triển.
                String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/images/";

                // B. Tạo tên file mới (Dùng username + thời gian để tránh trùng tên)
                // Ví dụ: khoa_167899999.jpg
                String originalFilename = photoFile.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFilename = current.getUsername() + "_" + System.currentTimeMillis() + extension;

                // C. Lưu file vào ổ cứng
                Path path = Paths.get(uploadDir + newFilename);
                Files.copy(photoFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                // D. Cập nhật tên file mới vào đối tượng User
                current.setPhoto(newFilename);
            }
            // ------------------------

            // 3. Lưu xuống Database
            accountDAO.save(current);

            // 4. Cập nhật lại Session
            session.setAttribute("user", current);

            model.addAttribute("message", "Cập nhật hồ sơ thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "user/edit-profile";
    }

    @GetMapping("/account/forgot-password")
    public String forgot() { return "user/forgot-password"; }
}
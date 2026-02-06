package com.poly.ASM.controller;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
            account.setPhoto("user.png"); // Ảnh mặc định
            accountDAO.save(account);

            // Gửi mail thông báo
            try {
                if(account.getEmail() != null && !account.getEmail().isEmpty()) {
                    String subject = "Chào mừng bạn đến với Tech Store!";
                    String body = "<h3>Xin chào " + account.getFullname() + "!</h3>" +
                            "<p>Chúc mừng bạn đã đăng ký thành công tài khoản thành viên.</p>" +
                            "<p>Tên đăng nhập: <strong>" + account.getUsername() + "</strong></p>" +
                            "<p>Hãy đăng nhập ngay để trải nghiệm mua sắm.</p>" +
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
        if(session.getAttribute("user") == null) return "redirect:/account/login";
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
            // Cập nhật lại session
            session.setAttribute("user", user);
            model.addAttribute("message", "Đổi mật khẩu thành công!");
        }
        return "user/change-password";
    }

    // ============================================================
    // 4. QUÊN MẬT KHẨU (ĐÃ FIX LỖI 405)
    // ============================================================
    @GetMapping("/account/forgot-password")
    public String forgot() { return "user/forgot-password"; }

    @PostMapping("/account/forgot-password")
    public String processForgotPassword(Model model, @RequestParam("username") String username) {
        // Tìm user theo username hoặc email (Giả sử bạn chỉ tìm theo username trước)
        // Nếu muốn tìm cả email thì cần viết thêm hàm trong AccountDAO
        try {
            Account user = accountDAO.findById(username).orElse(null);

            if(user != null && user.getEmail() != null) {
                // Giả lập gửi mật khẩu qua mail (Bạn có thể random mật khẩu mới ở đây)
                String subject = "Tech Store - Quên mật khẩu";
                String body = "Mật khẩu hiện tại của bạn là: " + user.getPassword();
                mailerService.send(user.getEmail(), subject, body);

                model.addAttribute("message", "Mật khẩu đã được gửi vào email: " + user.getEmail());
            } else {
                model.addAttribute("message", "Không tìm thấy tài khoản hoặc email không hợp lệ!");
                return "user/forgot-password";
            }
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi gửi mail: " + e.getMessage());
            return "user/forgot-password";
        }

        // Chuyển về trang login với thông báo thành công
        return "user/login";
    }

    // ============================================================
    // 5. CẬP NHẬT HỒ SƠ & UPLOAD ẢNH (Logic chuẩn)
    // ============================================================
    @GetMapping("/account/edit-profile")
    public String editProfile(Model model) {
        Account user = (Account) session.getAttribute("user");
        if(user == null) return "redirect:/account/login";

        model.addAttribute("user", user);
        return "user/edit-profile";
    }

    @PostMapping("/account/edit-profile")
    public String updateProfile(
            @ModelAttribute("user") Account formData,
            @RequestParam("photoFile") MultipartFile photoFile,
            Model model) {
        try {
            // Lấy lại user từ DB để đảm bảo dữ liệu gốc
            String username = ((Account)session.getAttribute("user")).getUsername();
            Account dbAccount = accountDAO.findById(username).get();

            // Cập nhật thông tin text
            dbAccount.setFullname(formData.getFullname());
            dbAccount.setEmail(formData.getEmail());

            // Xử lý upload ảnh
            if (!photoFile.isEmpty()) {
                String originalFilename = photoFile.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                // Tên file duy nhất: username_timestamp.jpg
                String newFilename = dbAccount.getUsername() + "_" + System.currentTimeMillis() + extension;

                // 1. Lưu vào thư mục SOURCE (Code gốc)
                String srcDir = System.getProperty("user.dir") + "/src/main/resources/static/images/";
                Path srcPath = Paths.get(srcDir);
                if (!Files.exists(srcPath)) Files.createDirectories(srcPath);
                Files.copy(photoFile.getInputStream(), srcPath.resolve(newFilename), StandardCopyOption.REPLACE_EXISTING);

                // 2. Lưu vào thư mục TARGET (Code đang chạy) - Quan trọng để hiện ảnh ngay
                String targetDir = System.getProperty("user.dir") + "/target/classes/static/images/";
                Path targetPath = Paths.get(targetDir);
                if (!Files.exists(targetPath)) Files.createDirectories(targetPath);
                Files.copy(srcPath.resolve(newFilename), targetPath.resolve(newFilename), StandardCopyOption.REPLACE_EXISTING);

                // Cập nhật tên ảnh vào DB
                dbAccount.setPhoto(newFilename);
            }

            // Lưu xuống DB
            accountDAO.save(dbAccount);
            // Cập nhật lại session để menu hiển thị thông tin mới
            session.setAttribute("user", dbAccount);

            model.addAttribute("message", "Cập nhật hồ sơ thành công!");
            model.addAttribute("user", dbAccount);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "user/edit-profile";
    }
}
package com.poly.ASM.controller.admin;

import com.poly.ASM.dao.AccountDAO;
import com.poly.ASM.model.Account;
import com.poly.ASM.service.MailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Controller
@RequestMapping("/admin/account")
public class AccountAController {

    @Autowired
    AccountDAO accountDAO;

    @Autowired
    MailerService mailerService; // Khôi phục chức năng gửi mail

    // ============================================================
    // 1. QUẢN LÝ TÀI KHOẢN (HIỂN THỊ & FORM)
    // ============================================================

    @GetMapping("/index")
    public String index(Model model) {
        // 1. Form rỗng để thêm mới
        Account item = new Account();
        item.setActivated(true);
        item.setAdmin(false);
        model.addAttribute("item", item);

        // 2. Danh sách tài khoản (để hiện bảng bên phải)
        List<Account> items = accountDAO.findAll();
        model.addAttribute("items", items);

        return "admin/accounts";
    }

    @GetMapping("/edit/{username}")
    public String edit(Model model, @PathVariable("username") String username) {
        // 1. Tìm user đổ lên Form
        Account item = accountDAO.findById(username).orElse(new Account());
        model.addAttribute("item", item);

        // 2. Vẫn phải load danh sách bảng bên dưới
        model.addAttribute("items", accountDAO.findAll());

        return "admin/accounts";
    }

    // ============================================================
    // 2. LƯU DỮ LIỆU (GỘP CREATE & UPDATE)
    // Lý do gộp: Để xử lý upload ảnh chung 1 chỗ cho gọn
    // ============================================================
    @PostMapping("/save")
    public String save(@ModelAttribute("item") Account item,
                       @RequestParam("photoFile") MultipartFile file,
                       Model model) {
        try {
            // --- XỬ LÝ ẢNH AVATAR ---
            if (!file.isEmpty()) {
                String fileName = file.getOriginalFilename();
                File srcFile = new File(System.getProperty("user.dir") + "/src/main/resources/static/images/" + fileName);
                File targetFile = new File(System.getProperty("user.dir") + "/target/classes/static/images/" + fileName);

                if(!srcFile.getParentFile().exists()) srcFile.getParentFile().mkdirs();
                if(!targetFile.getParentFile().exists()) targetFile.getParentFile().mkdirs();

                FileCopyUtils.copy(file.getBytes(), srcFile);
                FileCopyUtils.copy(file.getBytes(), targetFile);

                item.setPhoto(fileName);
            } else {
                // Giữ ảnh cũ nếu không chọn ảnh mới
                if (accountDAO.existsById(item.getUsername())) {
                    Account oldItem = accountDAO.findById(item.getUsername()).get();
                    if (item.getPhoto() == null) item.setPhoto(oldItem.getPhoto());
                }
            }

            // --- XỬ LÝ MẬT KHẨU ---
            // Nếu bỏ trống mật khẩu thì giữ nguyên mật khẩu cũ
            if (item.getPassword() == null || item.getPassword().isEmpty()) {
                if (accountDAO.existsById(item.getUsername())) {
                    Account oldItem = accountDAO.findById(item.getUsername()).get();
                    item.setPassword(oldItem.getPassword());
                }
            }

            accountDAO.save(item);
            model.addAttribute("message", "Cập nhật dữ liệu thành công!");

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/account/index";
    }

    // ============================================================
    // 3. CÁC CHỨC NĂNG PHỤ (DELETE, RESET)
    // ============================================================

    @GetMapping("/delete/{username}")
    public String delete(@PathVariable("username") String username, Model model) {
        try {
            accountDAO.deleteById(username);
            model.addAttribute("message", "Xóa thành công!");
        } catch (Exception e) {
            model.addAttribute("error", "Không thể xóa (Tài khoản đang có dữ liệu liên quan)!");
        }
        return "redirect:/admin/account/index";
    }

    @GetMapping("/reset")
    public String reset() {
        return "redirect:/admin/account/index";
    }

    // ============================================================
    // 4. GỬI EMAIL (ĐÃ KHÔI PHỤC ĐẦY ĐỦ)
    // ============================================================

    // Hiển thị form gửi mail riêng cho user
    @GetMapping("/send-mail/{username}")
    public String sendMailForm(@PathVariable("username") String username, Model model) {
        Account user = accountDAO.findById(username).orElse(null);
        if (user != null) {
            model.addAttribute("user", user);
            // Bạn cần đảm bảo có file templates/admin/mail-form.html nhé
            return "admin/mail-form";
        }
        return "redirect:/admin/account/index";
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
            model.addAttribute("message", "Đã gửi mail thành công!");
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi gửi mail: " + e.getMessage());
        }

        // Gửi xong quay về trang danh sách
        return "forward:/admin/account/index";
    }
}
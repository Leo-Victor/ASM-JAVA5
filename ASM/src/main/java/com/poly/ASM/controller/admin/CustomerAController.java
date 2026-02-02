package com.poly.ASM.controller.admin;

import com.poly.ASM.dao.AccountDAO;
import com.poly.ASM.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/customer") // Định nghĩa đường dẫn gốc
public class CustomerAController {

    @Autowired
    AccountDAO accountDAO;

    // 1. Hiển thị danh sách và Form
    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("item", new Account()); // Form rỗng
        List<Account> items = accountDAO.findAll(); // Lấy tất cả user
        model.addAttribute("items", items);
        return "admin/customers"; // Trả về file html
    }

    // 2. Chức năng chọn Sửa (Load thông tin lên Form)
    @GetMapping("/edit/{username}")
    public String edit(@PathVariable("username") String username, Model model) {
        Account acc = accountDAO.findById(username).orElse(null);
        model.addAttribute("item", acc);
        model.addAttribute("items", accountDAO.findAll());
        return "admin/customers";
    }

    // 3. Chức năng Thêm mới
    @PostMapping("/create")
    public String create(Account item) {
        accountDAO.save(item);
        return "redirect:/admin/customer/index";
    }

    // 4. Chức năng Cập nhật
    @PostMapping("/update")
    public String update(Account item) {
        accountDAO.save(item);
        return "redirect:/admin/customer/edit/" + item.getUsername();
    }

    // 5. Chức năng Xóa
    @GetMapping("/delete/{username}")
    public String delete(@PathVariable("username") String username) {
        accountDAO.deleteById(username);
        return "redirect:/admin/customer/index";
    }
}

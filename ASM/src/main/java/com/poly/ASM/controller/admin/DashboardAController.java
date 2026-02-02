package com.poly.ASM.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class DashboardAController {

    // URL: /admin/dashboard
    @GetMapping("/dashboard")
    public String index() {
        return "admin/index"; // Trỏ đến file templates/admin/index.html
    }
}
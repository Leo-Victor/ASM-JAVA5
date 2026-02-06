package com.poly.ASM.controller.admin;

import com.poly.ASM.dao.CategoryDAO;
import com.poly.ASM.dao.ProductDAO;
import com.poly.ASM.model.Category;
import com.poly.ASM.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/product")
public class ProductAController {

    @Autowired
    ProductDAO productDAO;

    @Autowired
    CategoryDAO categoryDAO;

    // ============================================================
    // 1. HIỂN THỊ DANH SÁCH (CÓ PHÂN TRANG)
    // ============================================================
    @GetMapping("/index")
    public String index(Model model, @RequestParam("p") Optional<Integer> p) {
        // 1. Form rỗng để thêm mới
        Product item = new Product();
        item.setCreateDate(new Date()); // Ngày hiện tại
        item.setAvailable(true);        // Mặc định còn hàng
        model.addAttribute("item", item);

        // 2. Load danh sách CÓ PHÂN TRANG (5 sp/trang)
        // p.orElse(0): Nếu không có tham số p thì mặc định trang 0
        Pageable pageable = PageRequest.of(p.orElse(0), 5);
        Page<Product> page = productDAO.findAll(pageable);

        // Gửi đối tượng Page sang View (HTML sẽ dùng .content để lấy list, .totalPages để đếm trang)
        model.addAttribute("items", page);

        return "admin/products";
    }

    // ============================================================
    // 2. EDIT (GIỮ NGUYÊN TRANG HIỆN TẠI)
    // ============================================================
    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") Integer id, @RequestParam("p") Optional<Integer> p) {
        // 1. Tìm sản phẩm đổ dữ liệu lên form
        Product item = productDAO.findById(id).orElse(new Product());
        model.addAttribute("item", item);

        // 2. Vẫn load danh sách phân trang bên dưới (để bảng không bị mất)
        Pageable pageable = PageRequest.of(p.orElse(0), 5);
        Page<Product> page = productDAO.findAll(pageable);
        model.addAttribute("items", page);

        return "admin/products";
    }

    // ============================================================
    // 3. LƯU (SAVE - Xử lý cả Thêm mới & Cập nhật)
    // ============================================================
    @PostMapping("/save")
    public String save(@ModelAttribute("item") Product item,
                       @RequestParam("imageFile") MultipartFile file) {
        try {
            // --- XỬ LÝ UPLOAD ẢNH (Lưu 2 nơi: Src & Target) ---
            if (!file.isEmpty()) {
                String fileName = file.getOriginalFilename();

                File srcFile = new File(System.getProperty("user.dir") + "/src/main/resources/static/images/" + fileName);
                File targetFile = new File(System.getProperty("user.dir") + "/target/classes/static/images/" + fileName);

                if(!srcFile.getParentFile().exists()) srcFile.getParentFile().mkdirs();
                if(!targetFile.getParentFile().exists()) targetFile.getParentFile().mkdirs();

                FileCopyUtils.copy(file.getBytes(), srcFile);
                FileCopyUtils.copy(file.getBytes(), targetFile);

                item.setImage(fileName);
            } else {
                // Nếu không chọn ảnh mới -> Giữ ảnh cũ
                if(item.getId() != null) {
                    Product old = productDAO.findById(item.getId()).orElse(null);
                    if(old != null) {
                        if(item.getImage() == null) item.setImage(old.getImage());
                        // Giữ ngày tạo cũ nếu update
                        if(item.getCreateDate() == null) item.setCreateDate(old.getCreateDate());
                    }
                }
            }

            // Nếu thêm mới thì set ngày tạo
            if(item.getCreateDate() == null) item.setCreateDate(new Date());

            productDAO.save(item);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Lưu xong quay về trang đầu tiên
        return "redirect:/admin/product/index";
    }

    // ============================================================
    // 4. XÓA
    // ============================================================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        try {
            productDAO.deleteById(id);
        } catch (Exception e) {
            System.out.println("Lỗi xóa (có thể do khóa ngoại): " + e.getMessage());
        }
        return "redirect:/admin/product/index";
    }

    // ============================================================
    // Helper: Đổ danh mục vào Dropdown
    // ============================================================
    @ModelAttribute("categories")
    public List<Category> getCategories() {
        return categoryDAO.findAll();
    }
}
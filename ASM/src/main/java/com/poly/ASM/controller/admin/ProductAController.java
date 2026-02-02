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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
        // Tạo đối tượng Product rỗng cho form thêm mới
        Product item = new Product();
        model.addAttribute("item", item);

        // --- LOGIC PHÂN TRANG ---
        // p.orElse(0): Nếu không có tham số p trên URL thì mặc định là trang 0
        // 5: Số lượng sản phẩm trên mỗi trang
        Pageable pageable = PageRequest.of(p.orElse(0), 5);
        Page<Product> page = productDAO.findAll(pageable);

        model.addAttribute("items", page); // Gửi Page sang view (thay vì List)
        // ------------------------

        // Đổ danh sách loại hàng vào combobox
        model.addAttribute("categories", categoryDAO.findAll());

        return "admin/products"; // Trả về file products.html
    }

    // ============================================================
    // 2. CHỨC NĂNG CHỈNH SỬA (EDIT)
    // ============================================================
    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") Integer id, @RequestParam("p") Optional<Integer> p) {
        // Tìm sản phẩm theo ID, nếu không thấy thì tạo mới
        Product item = productDAO.findById(id).orElse(new Product());
        model.addAttribute("item", item);

        // Vẫn phải tải lại danh sách có phân trang để hiển thị bảng bên dưới
        Pageable pageable = PageRequest.of(p.orElse(0), 5);
        Page<Product> page = productDAO.findAll(pageable);
        model.addAttribute("items", page);

        model.addAttribute("categories", categoryDAO.findAll());

        return "admin/products";
    }

    // ============================================================
    // 3. THÊM MỚI (CREATE) - CÓ UPLOAD ẢNH
    // ============================================================
    @PostMapping("/create")
    public String create(Product item, @RequestParam("imageFile") MultipartFile file) throws IOException {
        // Xử lý lưu file ảnh
        if (!file.isEmpty()) {
            String filename = file.getOriginalFilename();
            // Lưu vào thư mục static/images trong project
            Path path = Paths.get(System.getProperty("user.dir") + "/src/main/resources/static/images/" + filename);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // Gán tên file vào đối tượng Product
            item.setImage(filename);
        } else {
            // Nếu người dùng không chọn ảnh, gán ảnh mặc định
            item.setImage("logo.png");
        }

        productDAO.save(item);
        return "redirect:/admin/product/index";
    }

    // ============================================================
    // 4. CẬP NHẬT (UPDATE) - CÓ UPLOAD ẢNH
    // ============================================================
    @PostMapping("/update")
    public String update(Product item, @RequestParam("imageFile") MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            // Nếu có chọn ảnh mới -> Lưu ảnh mới đè lên
            String filename = file.getOriginalFilename();
            Path path = Paths.get(System.getProperty("user.dir") + "/src/main/resources/static/images/" + filename);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            item.setImage(filename);
        } else {
            // Nếu không chọn ảnh mới -> Giữ nguyên ảnh cũ
            // Lấy thông tin sản phẩm cũ từ DB ra để xem ảnh cũ tên gì
            Product oldItem = productDAO.findById(item.getId()).orElse(null);
            if (oldItem != null) {
                item.setImage(oldItem.getImage());
            }
        }

        productDAO.save(item);
        // Sau khi update xong thì quay lại trang edit của sản phẩm đó
        return "redirect:/admin/product/edit/" + item.getId();
    }

    // ============================================================
    // 5. XÓA (DELETE)
    // ============================================================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        productDAO.deleteById(id);
        return "redirect:/admin/product/index";
    }

    // ============================================================
    // 6. LÀM MỚI (RESET)
    // ============================================================
    @GetMapping("/reset")
    public String reset() {
        return "redirect:/admin/product/index";
    }
}
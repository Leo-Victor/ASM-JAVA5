package com.poly.ASM.dao;

import com.poly.ASM.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryDAO extends JpaRepository<Category, String> {
    // [SỬA LẠI] Dùng String vì khóa chính là Char(4)
}
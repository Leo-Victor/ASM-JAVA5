package com.poly.ASM.dao;

import com.poly.ASM.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountDAO extends JpaRepository<Account, String> {
    // 1. Tìm kiếm người dùng theo Email

    // Tự động sinh câu lệnh: SELECT * FROM Accounts WHERE Email = ?
    Account findByEmail(String email);

    // 2. Lấy danh sách những người là Admin hoặc chỉ là User
    // (Dùng cho trang Quản lý tài khoản để lọc)
    // Tự động sinh câu lệnh: SELECT * FROM Accounts WHERE Admin = ?
    List<Account> findByAdmin(boolean admin);

    // 3. Tìm kiếm người dùng theo tên (Gõ tên vào ô tìm kiếm)

    // Câu lệnh JPQL tùy chỉnh: Tìm những người có họ tên chứa từ khóa (LIKE)
    @Query("SELECT a FROM Account a WHERE a.fullname LIKE ?1")
    List<Account> findByFullnameLike(String fullname);
}

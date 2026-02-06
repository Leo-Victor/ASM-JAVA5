package com.poly.ASM.dao;

import com.poly.ASM.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderDAO extends JpaRepository<Order, Long> {
    // =================================================================
    // 1. DÀNH CHO NGƯỜI DÙNG (USER)
    // =================================================================

    // Lấy danh sách đơn hàng của User, sắp xếp ngày mới nhất lên đầu
    @Query("SELECT o FROM Order o WHERE o.account.username = ?1 ORDER BY o.createDate DESC")
    List<Order> findByUsername(String username);

    // =================================================================
    // 2. DÀNH CHO ADMIN (THỐNG KÊ DASHBOARD)
    // =================================================================

    // Đếm số đơn hàng đang ở trạng thái "Chờ xác nhận" (Status = 0)
    // Hàm này dùng để hiện con số ở ô màu Vàng trên Dashboard
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 0")
    Long countPendingOrders();

    // Tính tổng tiền của tất cả đơn hàng "Đã giao thành công" (Status = 2)
    // Hàm này dùng để hiện Doanh thu ở ô màu Xanh lá
    // Dùng COALESCE để nếu không có đơn nào thì trả về 0 thay vì null
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 2")
    Double sumRevenue();
}
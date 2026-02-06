package com.poly.ASM.dao;

import com.poly.ASM.model.Order;
import com.poly.ASM.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailDAO extends JpaRepository<OrderDetail, Long> {
    // [THÊM DÒNG NÀY ĐỂ SỬA LỖI]
    // Hàm này giúp tìm tất cả sản phẩm thuộc về một đơn hàng (Order) cụ thể
    List<OrderDetail> findByOrder(Order order);
}
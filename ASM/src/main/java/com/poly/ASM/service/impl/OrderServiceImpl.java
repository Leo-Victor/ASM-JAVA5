package com.poly.ASM.service.impl;

// [SỬA LỖI QUAN TRỌNG] Phải dùng com.fasterxml... mới đúng thư viện Spring Boot
import com.fasterxml.jackson.databind.JsonNode;
import com.poly.ASM.dao.OrderDAO;
import com.poly.ASM.dao.OrderDetailDAO;
import com.poly.ASM.dao.ProductDAO;
import com.poly.ASM.model.Account;
import com.poly.ASM.model.Order;
import com.poly.ASM.model.OrderDetail;
import com.poly.ASM.model.Product;
import com.poly.ASM.service.interfaces.OrderService;
import com.poly.ASM.service.interfaces.ShoppingCartService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//import tools.jackson.databind.JsonNode;

import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderDAO orderDAO;

    @Autowired
    OrderDetailDAO orderDetailDAO;

    @Autowired
    ProductDAO productDAO;

    @Autowired
    ShoppingCartService cartService;

    @Override
    @Transactional // Đảm bảo toàn vẹn dữ liệu (Lỗi là rollback hết)
    public Order create(JsonNode orderData, Account user) {
        // 1. Tạo đơn hàng (Order)
        Order order = new Order();
        order.setCreateDate(new Date());
        order.setAccount(user);
        order.setAddress(orderData.get("address").asText());
        order.setStatus(0); // 0: Chờ duyệt

        // [QUAN TRỌNG] Lưu tổng tiền trước khi xóa giỏ hàng
        order.setTotalAmount(cartService.getAmount());

        // Lưu Order vào DB để lấy ID
        orderDAO.save(order);

        // 2. Lưu chi tiết đơn hàng (OrderDetails)
        // Duyệt qua từng sản phẩm trong giỏ hàng
        cartService.getItems().forEach(item -> {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);

            // Tìm sản phẩm từ DB dựa trên ID trong giỏ hàng
            // (Giả định CartItem có hàm getId() trả về mã sản phẩm)
            Product product = productDAO.findById(item.getId()).orElse(null);

            detail.setProduct(product);
            detail.setPrice(item.getPrice());
            detail.setQuantity(item.getQty());

            orderDetailDAO.save(detail);
        });

        // 3. Xóa sạch giỏ hàng sau khi mua xong
        cartService.clear();

        return order;
    }

    @Override
    public Order findById(Long id) {
        return orderDAO.findById(id).orElse(null);
    }

    @Override
    public List<Order> findByUsername(String username) {
        return orderDAO.findByUsername(username);
    }
}
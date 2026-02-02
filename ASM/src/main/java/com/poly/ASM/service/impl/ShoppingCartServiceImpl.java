package com.poly.ASM.service.impl;

import com.poly.ASM.dao.ProductDAO;
import com.poly.ASM.model.CartItem;
import com.poly.ASM.model.Product;
import com.poly.ASM.service.interfaces.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SessionScope // Quan trọng: Giỏ hàng đi theo phiên làm việc của từng user
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    ProductDAO productDAO;

    // Dùng Map để lưu: Key là ID sản phẩm, Value là CartItem
    Map<Integer, CartItem> map = new HashMap<>();

    @Override
    public void add(Integer id) {
        CartItem item = map.get(id);
        if (item == null) { // Chưa có thì thêm mới
            Product p = productDAO.findById(id).get(); // Lấy thông tin từ DB
            item = new CartItem(p.getId(), p.getName(), p.getPrice(), 1, p.getImage());
            map.put(id, item);
        } else { // Có rồi thì tăng số lượng
            item.setQty(item.getQty() + 1);
        }
    }

    @Override
    public void remove(Integer id) {
        map.remove(id);
    }

    @Override
    public CartItem update(Integer id, int qty) {
        CartItem item = map.get(id);
        item.setQty(qty);
        return item;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Collection<CartItem> getItems() {
        return map.values();
    }

    @Override
    public int getCount() {
        return map.values().stream().mapToInt(item -> item.getQty()).sum();
    }

    @Override
    public double getAmount() {
        return map.values().stream().mapToDouble(item -> item.getPrice() * item.getQty()).sum();
    }
}
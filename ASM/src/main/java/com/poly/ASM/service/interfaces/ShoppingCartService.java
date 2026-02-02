package com.poly.ASM.service.interfaces;

import com.poly.ASM.model.CartItem;

import java.util.Collection;

public interface ShoppingCartService {
    void add(Integer id);
    void remove(Integer id);
    CartItem update(Integer id, int qty);
    void clear();
    Collection<CartItem> getItems();
    int getCount();
    double getAmount();
}
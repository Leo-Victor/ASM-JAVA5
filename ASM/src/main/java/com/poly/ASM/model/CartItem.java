package com.poly.ASM.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    Integer id;
    String name;
    double price;
    int qty = 1; // Số lượng mặc định là 1
    String image;
}

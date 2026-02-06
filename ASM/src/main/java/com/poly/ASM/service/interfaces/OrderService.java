package com.poly.ASM.service.interfaces;


// [QUAN TRỌNG] Phải import dòng này
import com.fasterxml.jackson.databind.JsonNode;

import com.poly.ASM.model.Account;
import com.poly.ASM.model.Order;
//import tools.jackson.databind.JsonNode;

import java.util.List;

public interface OrderService {
    Order create(JsonNode orderData, Account user);
    Order findById(Long id);
    List<Order> findByUsername(String username);
}
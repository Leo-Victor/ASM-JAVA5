package com.poly.ASM.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Entity
@Table(name = "Categories")
public class Category implements Serializable {
    @Id
    // @GeneratedValue... -> XÓA DÒNG NÀY (Vì Char(4) thường phải tự nhập, không tự tăng)
    String id; // [SỬA LẠI] Phải là String để khớp với char(4)

    String name;

    @OneToMany(mappedBy = "category")
    List<Product> products;
}
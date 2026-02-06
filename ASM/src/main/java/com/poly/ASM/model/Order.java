package com.poly.ASM.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "Orders")
public class Order implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String address;

    @Temporal(TemporalType.DATE)
    @Column(name = "CreateDate")
    Date createDate = new Date();

    @ManyToOne
    @JoinColumn(name = "Username")
    Account account;

    @Column(name = "Status")
    Integer status = 0;

    @Column(name = "TotalAmount")
    Double totalAmount;

    // ==========================================================
    // [QUAN TRỌNG] ĐÂY LÀ PHẦN BẠN BỊ THIẾU
    // ==========================================================
    @JsonIgnore // Ngăn lỗi vòng lặp vô tận khi chuyển sang JSON
    @OneToMany(mappedBy = "order")
    List<OrderDetail> orderDetails;
    // Tên biến là 'orderDetails' -> Khớp với file HTML mình gửi lúc nãy
}
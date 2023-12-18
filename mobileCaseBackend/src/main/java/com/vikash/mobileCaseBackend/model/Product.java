package com.vikash.mobileCaseBackend.model;


import java.time.LocalDateTime;
import java.util.ArrayList;
import com.vikash.mobileCaseBackend.model.enums.Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;
    private Type productType;
    private String productName;
    private String productDescription;
    //private  Integer quantity;
    private boolean productAvailable;

    @Column(name="price")
    private double productPrice;

    @Column(name = "reservation_time")
    private LocalDateTime reservationTime;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GuestCartItem> GuestCartItems = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "fk_order_id")
    private OrderEntity orderEntity;



}

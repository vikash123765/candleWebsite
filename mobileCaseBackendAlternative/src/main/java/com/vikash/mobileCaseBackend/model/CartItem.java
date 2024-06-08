package com.vikash.mobileCaseBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    @ManyToOne
    @JoinColumn(name = "fk_cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "fk_product_id")
    private Product product;

    private int quantity;

}

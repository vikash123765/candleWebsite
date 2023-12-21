package com.vikash.mobileCaseBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class GuestCartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer guestCartItemId;

    @ManyToOne
    @JoinColumn(name = "fk_guest_cart_id")
    private GuestCart guestCart;

    @ManyToOne
    @JoinColumn(name = "fk_product_id")
    private Product product;

    private int quantity;

}

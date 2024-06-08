package com.vikash.mobileCaseBackend.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class GuestCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer guestCartId;
    private String sessionToken;
    private boolean orderPlaced;

    @ManyToMany(mappedBy = "guestCarts")
    private List<User> users = new ArrayList<>();


    @OneToMany(mappedBy = "guestCart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GuestCartItem> guestCartItems = new ArrayList<>();


}

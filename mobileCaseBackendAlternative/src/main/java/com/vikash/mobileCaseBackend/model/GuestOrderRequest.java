package com.vikash.mobileCaseBackend.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestOrderRequest {

    private String userName;
    private String shippingAddress;
    private String email;

    private Long phoneNumber;

   // private List<GuestCartItem> cartItems;



}

package com.vikash.mobileCaseBackend.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestOrderRequest {

    private String FullName;
    private String shippingAddress;
    private String email;

    private Integer phoneNumber;


}

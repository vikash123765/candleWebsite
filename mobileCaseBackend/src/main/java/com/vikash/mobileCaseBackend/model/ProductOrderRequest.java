package com.vikash.mobileCaseBackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOrderRequest {
    private String productName;
    private int quantity;
}

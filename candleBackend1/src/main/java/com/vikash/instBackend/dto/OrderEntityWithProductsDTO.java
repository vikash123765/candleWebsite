package com.vikash.instBackend.dto;

import com.vikash.instBackend.model.OrderEntity;
import com.vikash.instBackend.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data

@AllArgsConstructor
public class OrderEntityWithProductsDTO {
    private final OrderEntity orderEntity;

    private final transient List<Product> products;

}

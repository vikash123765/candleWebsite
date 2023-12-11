package com.vikash.mobileCaseBackend.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
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


    @Column(name="price")
    private double productPrice;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    boolean productAvailable;

    @ManyToOne
    @JoinColumn(name = "fk_order_id")
    private OrderEntity orderEntity;



}

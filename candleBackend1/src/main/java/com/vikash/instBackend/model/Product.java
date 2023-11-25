package com.vikash.instBackend.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vikash.instBackend.model.enums.Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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


    @Column(name="price")
    private double productPrice;

    boolean productAvailable;

    @ManyToOne
    @JoinColumn(name = "fk_order_id")
    @JsonIgnore
    private OrderEntity orderEntity;



}

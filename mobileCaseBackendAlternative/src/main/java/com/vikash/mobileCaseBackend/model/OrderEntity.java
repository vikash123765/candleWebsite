package com.vikash.mobileCaseBackend.model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderNumber;

    private LocalDateTime creationTimeStamp;

   // @JsonFormat(shape = JsonFormat.Shape.STRING)
    private boolean markAsSent;

   // @JsonFormat(shape = JsonFormat.Shape.STRING)
    private boolean markAsDelivered;


    private Integer trackingNumber;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "product_order",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();

  // may need to delete
    @ManyToMany
    @JoinTable(
            name = "user_order",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users = new ArrayList<>();

}

package com.vikash.mobileCaseBackend.model;



import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderNumber;

    private LocalDateTime setCreatingTimeStamp;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private boolean markAsSent;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private boolean markAsDelivered;


    private Integer trackingNumber;


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="fk_user_id")
    User user;







}

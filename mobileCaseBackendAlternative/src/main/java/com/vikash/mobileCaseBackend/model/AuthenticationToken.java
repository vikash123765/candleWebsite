package com.vikash.mobileCaseBackend.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AuthenticationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    private String tokenValue;

    private LocalDateTime tokenCreationDateTime;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "fk_user_Id")
    private User user;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "fk_admin_Id")
    private Admin admin;

    // create constructor which takes user as an argument

    public AuthenticationToken(User user){
        this.user=user;
        this.tokenValue= UUID.randomUUID().toString();
        this.tokenCreationDateTime = LocalDateTime.now();
    }

    public AuthenticationToken(Admin admin){
        this.admin=admin;
        this.tokenValue= UUID.randomUUID().toString();
        this.tokenCreationDateTime = LocalDateTime.now();
    }

}

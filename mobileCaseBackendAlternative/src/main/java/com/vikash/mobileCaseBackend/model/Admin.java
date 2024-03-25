package com.vikash.mobileCaseBackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Admin {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;
    private String adminName;

    //@Pattern(regexp = "^.+@instaAdmin\\.com$")
    private String adminEmail;

    //@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$!%])[A-Za-z\\d@#$!%]{8,}$\n")
    private String adminPassword;

    @OneToMany
    @JoinColumn(name = "fk_admin_Id")
    private Set<AuthenticationToken> authenticationTokens;


}

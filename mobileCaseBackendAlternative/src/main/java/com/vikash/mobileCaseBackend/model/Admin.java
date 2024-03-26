package com.vikash.mobileCaseBackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "admin")
    @JsonIgnore
    private AuthenticationToken authenticationToken;


    //@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$!%])[A-Za-z\\d@#$!%]{8,}$\n")
    private String adminPassword;


}

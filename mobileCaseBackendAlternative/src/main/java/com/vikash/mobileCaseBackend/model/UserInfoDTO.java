package com.vikash.mobileCaseBackend.model;

import com.vikash.mobileCaseBackend.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private String userName;
    private String userEmail;
    private String phoneNumber;
    private String address;
    private String password;
    private LocalDateTime tokenCreationDateTime;
    private Gender gender;

}

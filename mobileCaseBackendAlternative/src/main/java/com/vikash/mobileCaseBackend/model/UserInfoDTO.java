package com.vikash.mobileCaseBackend.model;

import com.vikash.mobileCaseBackend.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private String userName;
    private String userEmail;
    private Long phoneNumber;
    private String address;
    private String password;
    private Gender gender;

}
